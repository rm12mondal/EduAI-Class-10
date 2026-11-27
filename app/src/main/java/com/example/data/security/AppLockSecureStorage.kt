package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class AppLockStoredSnapshot(
    val isLockEnabled: Boolean,
    val hasPinConfigured: Boolean,
    val isBiometricEnabled: Boolean,
    val failedAttempts: Int,
    val lockoutUntilEpochMs: Long
)

/**
 * Hardware-backed secure storage for the 4-digit App Lock PIN & security flags.
 * - Uses AndroidX `EncryptedSharedPreferences` backed by an Android Keystore AES256-GCM `MasterKey`.
 * - Never stores plaintext PINs: hashes the 4-digit PIN with PBKDF2WithHmacSHA256 (100,000 iterations)
 *   and a per-installation 16-byte random salt, verified via constant-time `MessageDigest.isEqual`.
 * - Tracks consecutive failed attempts and enforces a 30-second cooldown after 5 wrong guesses.
 */
class AppLockSecureStorage(context: Context) {

    private val appContext = context.applicationContext

    private val prefsLock = Any()

    @Volatile
    private var cachedPrefs: SharedPreferences? = null

    @Volatile
    private var cachedSnapshot: AppLockStoredSnapshot? = null

    private fun getOrInitPrefs(): SharedPreferences {
        cachedPrefs?.let { return it }
        return synchronized(prefsLock) {
            cachedPrefs ?: createEncryptedPrefs(appContext).also { cachedPrefs = it }
        }
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "EncryptedSharedPreferences initialization failed; falling back to salted-hash private prefs", e)
            // Even in fallback on devices with broken Keystore, only salted PBKDF2 hash is stored—never plaintext PIN.
            context.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Initializes `EncryptedSharedPreferences` strictly on `Dispatchers.IO` and caches the
     * resulting snapshot in memory so the main thread is never blocked on cold start.
     */
    suspend fun readSnapshotAsync(): AppLockStoredSnapshot = withContext(Dispatchers.IO) {
        readSnapshot()
    }

    fun getCachedSnapshot(): AppLockStoredSnapshot? = cachedSnapshot

    fun readSnapshot(): AppLockStoredSnapshot {
        val prefs = getOrInitPrefs()
        val hash = prefs.getString(KEY_PIN_HASH, null)
        val salt = prefs.getString(KEY_PIN_SALT, null)
        val hasPin = !hash.isNullOrBlank() && !salt.isNullOrBlank()
        val enabled = hasPin && prefs.getBoolean(KEY_LOCK_ENABLED, false)
        val bioEnabled = enabled && prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        val failed = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        return AppLockStoredSnapshot(
            isLockEnabled = enabled,
            hasPinConfigured = hasPin,
            isBiometricEnabled = bioEnabled,
            failedAttempts = failed,
            lockoutUntilEpochMs = lockoutUntil
        ).also { cachedSnapshot = it }
    }

    fun savePinAndEnable(pin: String, enableBiometric: Boolean? = null): Boolean {
        if (!isValidFourDigitPin(pin)) return false
        val prefs = getOrInitPrefs()
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val hash = derivePinHash(pin, salt)
        prefs.edit().apply {
            putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            putString(KEY_PIN_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
            putBoolean(KEY_LOCK_ENABLED, true)
            if (enableBiometric != null) {
                putBoolean(KEY_BIOMETRIC_ENABLED, enableBiometric)
            }
            putInt(KEY_FAILED_ATTEMPTS, 0)
            putLong(KEY_LOCKOUT_UNTIL, 0L)
        }.apply()
        readSnapshot()
        return true
    }

    fun verifyPin(candidatePin: String): PinVerifyOutcome {
        val prefs = getOrInitPrefs()
        val now = System.currentTimeMillis()
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        if (lockoutUntil > now) {
            val remainingSec = ((lockoutUntil - now) / 1000L).coerceAtLeast(1L)
            return PinVerifyOutcome.LockedOut(
                remainingSeconds = remainingSec,
                lockoutUntilEpochMs = lockoutUntil
            )
        }

        if (!isValidFourDigitPin(candidatePin)) {
            return PinVerifyOutcome.Invalid(
                failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0),
                lockoutUntilEpochMs = 0L
            )
        }

        val saltB64 = prefs.getString(KEY_PIN_SALT, null)
        val hashB64 = prefs.getString(KEY_PIN_HASH, null)
        if (saltB64.isNullOrBlank() || hashB64.isNullOrBlank()) {
            return PinVerifyOutcome.Invalid(0, 0L)
        }

        val salt = runCatching { Base64.decode(saltB64, Base64.NO_WRAP) }.getOrNull()
            ?: return PinVerifyOutcome.Invalid(0, 0L)
        val expectedHash = runCatching { Base64.decode(hashB64, Base64.NO_WRAP) }.getOrNull()
            ?: return PinVerifyOutcome.Invalid(0, 0L)

        val candidateHash = derivePinHash(candidatePin, salt)
        val matches = MessageDigest.isEqual(expectedHash, candidateHash)

        return if (matches) {
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0L)
                .apply()
            readSnapshot()
            PinVerifyOutcome.Success
        } else {
            val nextAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            val nextLockout = if (nextAttempts >= MAX_FAILED_ATTEMPTS) {
                now + LOCKOUT_DURATION_MS
            } else {
                0L
            }
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, if (nextLockout > 0L) 0 else nextAttempts)
                .putLong(KEY_LOCKOUT_UNTIL, nextLockout)
                .apply()
            readSnapshot()
            if (nextLockout > 0L) {
                PinVerifyOutcome.LockedOut(
                    remainingSeconds = LOCKOUT_DURATION_MS / 1000L,
                    lockoutUntilEpochMs = nextLockout
                )
            } else {
                PinVerifyOutcome.Invalid(
                    failedAttempts = nextAttempts,
                    lockoutUntilEpochMs = 0L
                )
            }
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        val prefs = getOrInitPrefs()
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        readSnapshot()
    }

    fun disableLock() {
        val prefs = getOrInitPrefs()
        prefs.edit()
            .putBoolean(KEY_LOCK_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
        readSnapshot()
    }

    fun enableLockIfPinExists(): Boolean {
        val snap = readSnapshot()
        if (!snap.hasPinConfigured) return false
        getOrInitPrefs().edit().putBoolean(KEY_LOCK_ENABLED, true).apply()
        readSnapshot()
        return true
    }

    private fun derivePinHash(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BITS)
        return try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    companion object {
        private const val TAG = "AppLockSecureStorage"
        private const val PREFS_NAME = "eduai_secure_app_lock_prefs"
        private const val FALLBACK_PREFS_NAME = "eduai_secure_app_lock_hash_prefs"
        private const val KEY_PIN_HASH = "pin_pbkdf2_hash"
        private const val KEY_PIN_SALT = "pin_pbkdf2_salt"
        private const val KEY_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "app_lock_biometric_enabled"
        private const val KEY_FAILED_ATTEMPTS = "app_lock_failed_attempts"
        private const val KEY_LOCKOUT_UNTIL = "app_lock_lockout_until_ms"

        private const val SALT_BYTES = 16
        private const val PBKDF2_ITERATIONS = 100_000
        private const val HASH_BITS = 256
        const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30_000L

        fun isValidFourDigitPin(pin: String): Boolean =
            pin.length == 4 && pin.all { it in '0'..'9' }
    }
}

sealed interface PinVerifyOutcome {
    data object Success : PinVerifyOutcome
    data class Invalid(val failedAttempts: Int, val lockoutUntilEpochMs: Long) : PinVerifyOutcome
    data class LockedOut(val remainingSeconds: Long, val lockoutUntilEpochMs: Long) : PinVerifyOutcome
}
