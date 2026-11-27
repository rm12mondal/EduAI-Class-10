package com.example.data.security

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Helper for AndroidX BiometricPrompt (Fingerprint / Face Unlock) on FragmentActivity.
 */
object BiometricUnlockHelper {

    private const val ALLOWED_AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK

    fun isBiometricAvailable(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(ALLOWED_AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun findFragmentActivity(context: Context): FragmentActivity? {
        var current: Context? = context
        while (current is ContextWrapper) {
            if (current is FragmentActivity) return current
            current = current.baseContext
        }
        return current as? FragmentActivity
    }

    fun authenticate(
        activity: FragmentActivity,
        appTitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                    errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_CANCELED
                ) {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("ফিঙ্গারপ্রিন্ট/ফেস মেলেনি। আবার চেষ্টা করো বা ৪-ডিজিট PIN দাও।")
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("$appTitle আনলক করুন")
            .setSubtitle("অ্যাপ খুলতে ফিঙ্গারপ্রিন্ট বা ফেস লক ব্যবহার করুন")
            .setNegativeButtonText("৪-ডিজিট PIN ব্যবহার করুন")
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()

        runCatching {
            BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
        }.onFailure { err ->
            onError(err.localizedMessage ?: "বায়োমেট্রিক চালু করা যায়নি। PIN ব্যবহার করুন।")
        }
    }
}
