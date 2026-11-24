package com.example.data.speech

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

/**
 * Foreground Media Playback Service that keeps offline Android TextToSpeech Podcast Revision
 * playing smoothly in the background and when the device screen is locked.
 */
class PodcastAudioService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannelIfNeeded()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE_PAUSE -> {
                PodcastServiceBridge.onTogglePlayPauseRequested?.invoke()
                return START_NOT_STICKY
            }

            ACTION_STOP -> {
                PodcastServiceBridge.onStopRequested?.invoke()
                releaseWakeLock()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }

            ACTION_UPDATE, ACTION_START -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "EduAI পডকাস্ট রিভিশন"
                val subtitle = intent.getStringExtra(EXTRA_SUBTITLE) ?: "অফলাইন অডিও টিউটর চলছে"
                val isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, true)
                val speed = intent.getFloatExtra(EXTRA_SPEED, 1.0f)

                if (isPlaying) {
                    acquireWakeLock()
                } else {
                    releaseWakeLock()
                }

                val notification = buildNotification(title, subtitle, isPlaying, speed)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(
                            NOTIFICATION_ID,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                        )
                    } else {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Unable to enter foreground media playback cleanly", e)
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                val pm = getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = pm?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "EduAI:PodcastAudioWakeLock"
                )?.apply {
                    setReferenceCounted(false)
                }
            }
            if (wakeLock?.isHeld == false) {
                // Auto-expire after 45 minutes max for battery safety
                wakeLock?.acquire(45 * 60 * 1000L)
            }
        } catch (e: Exception) {
            Log.w(TAG, "WakeLock acquire skipped", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {
        }
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                CHANNEL_ID,
                "EduAI পডকাস্ট অডিও রিভিশন",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "স্ক্রিন লক অবস্থায় অফলাইন অডিও রিভিশন প্লেব্যাক নিয়ন্ত্রণ"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        title: String,
        subtitle: String,
        isPlaying: Boolean,
        speed: Float
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = PendingIntent.getActivity(
            this,
            101,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, PodcastAudioService::class.java).apply {
            action = ACTION_TOGGLE_PAUSE
        }
        val togglePending = PendingIntent.getService(
            this,
            102,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, PodcastAudioService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this,
            103,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val speedTag = if (speed == 1.25f) "1.25x" else "${speed}x"
        val statusPrefix = if (isPlaying) "▶ পডকাস্ট চলছে ($speedTag)" else "⏸ বিরতি ($speedTag)"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("$statusPrefix • $subtitle")
            .setContentIntent(contentPending)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "বিরতি" else "চালু",
                togglePending
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "বন্ধ",
                stopPending
            )
            .build()
    }

    companion object {
        private const val TAG = "PodcastAudioService"
        private const val CHANNEL_ID = "eduai_podcast_audio_channel"
        private const val NOTIFICATION_ID = 4108

        const val ACTION_START = "com.example.action.PODCAST_START"
        const val ACTION_UPDATE = "com.example.action.PODCAST_UPDATE"
        const val ACTION_TOGGLE_PAUSE = "com.example.action.PODCAST_TOGGLE_PAUSE"
        const val ACTION_STOP = "com.example.action.PODCAST_STOP"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_SUBTITLE = "extra_subtitle"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        const val EXTRA_SPEED = "extra_speed"

        fun syncState(
            context: Context,
            title: String,
            subtitle: String,
            isPlaying: Boolean,
            speed: Float
        ) {
            val intent = Intent(context.applicationContext, PodcastAudioService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_SUBTITLE, subtitle)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
                putExtra(EXTRA_SPEED, speed)
            }
            runCatching {
                ContextCompat.startForegroundService(context.applicationContext, intent)
            }.onFailure { err ->
                Log.w(TAG, "Foreground service start skipped (background restriction)", err)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context.applicationContext, PodcastAudioService::class.java)
            runCatching { context.applicationContext.stopService(intent) }
        }
    }
}

/**
 * Lightweight bridge allowing lock-screen notification buttons to toggle/stop EduTtsManager.
 */
object PodcastServiceBridge {
    @Volatile
    var onTogglePlayPauseRequested: (() -> Unit)? = null

    @Volatile
    var onStopRequested: (() -> Unit)? = null
}
