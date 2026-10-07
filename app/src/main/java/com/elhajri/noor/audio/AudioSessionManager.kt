package com.elhajri.noor.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.elhajri.noor.audio.player.PlayerCore
import com.elhajri.noor.audio.player.QuranPlayerManager
import com.elhajri.noor.notification.AdhanPlaybackService
import com.elhajri.noor.notification.AdhanReceiver
import com.elhajri.noor.web.WebPlayerBus
import com.elhajri.noor.web.WebPlayerService

enum class AudioSource {
    QURAN,
    NASHEED,
    ADHAN,
    WEB,
    NONE
}

/**
 * AudioSessionManager: Central Audio Session Manager for NOOR App.
 * Resolves audio conflicts by pausing/stopping other audio playbacks
 * whenever a new playback (Quran, Nasheed, Adhan, or Web player) starts.
 * Ensures a single unified media notification ID (3000) across all services.
 */
object AudioSessionManager {
    const val MEDIA_NOTIFICATION_ID = 3000
    const val MEDIA_CHANNEL_ID = "noor_central_media_playback"
    const val MEDIA_CHANNEL_NAME = "مشغّل الوسائط الموحد"

    @Volatile
    var currentSource: AudioSource = AudioSource.NONE
        private set

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                MEDIA_CHANNEL_ID,
                MEDIA_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعار تشغيل الصوتيات لمشغّل نور الموحد"
                setShowBadge(false)
            }
            nm.createNotificationChannel(channel)
        }
    }

    @Synchronized
    fun onPlaybackStarted(context: Context, source: AudioSource) {
        currentSource = source
        ensureChannel(context)

        // Stop/pause all other sources
        if (source != AudioSource.QURAN) {
            try {
                if (QuranPlayerManager.isPlayingNow()) {
                    QuranPlayerManager.stop()
                } else if (QuranPlayerManager.state.value.currentId != null) {
                    QuranPlayerManager.pauseIfPlaying()
                }
            } catch (_: Exception) {}
        }

        if (source != AudioSource.NASHEED) {
            try {
                if (PlayerCore.isPlayingNow()) {
                    PlayerCore.stop()
                } else if (PlayerCore.state.value.currentId != null) {
                    PlayerCore.pauseIfPlaying()
                }
            } catch (_: Exception) {}
        }

        if (source != AudioSource.ADHAN) {
            try {
                AdhanPlaybackService.stop(context)
                AdhanReceiver.stopAdhanStatic()
            } catch (_: Exception) {}
        }

        if (source != AudioSource.WEB) {
            try {
                WebPlayerService.stop(context)
            } catch (_: Exception) {}
        }
    }

    @Synchronized
    fun onPlaybackStopped(context: Context, source: AudioSource) {
        if (currentSource == source) {
            currentSource = AudioSource.NONE
        }
        val quranPlaying = try { QuranPlayerManager.isPlayingNow() } catch (_: Exception) { false }
        val nasheedPlaying = try { PlayerCore.isPlayingNow() } catch (_: Exception) { false }
        val adhanPlaying = AdhanPlaybackService.isPlaying
        val webPlaying = WebPlayerBus.isPlaying

        if (!quranPlaying && !nasheedPlaying && !adhanPlaying && !webPlaying) {
            try {
                val nm = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                nm?.cancel(MEDIA_NOTIFICATION_ID)
            } catch (_: Exception) {}
        }
    }

    fun stopAll(context: Context) {
        currentSource = AudioSource.NONE
        try { QuranPlayerManager.stop() } catch (_: Exception) {}
        try { PlayerCore.stop() } catch (_: Exception) {}
        try { AdhanPlaybackService.stop(context) } catch (_: Exception) {}
        try { AdhanReceiver.stopAdhanStatic() } catch (_: Exception) {}
        try { WebPlayerService.stop(context) } catch (_: Exception) {}
        try {
            val nm = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.cancel(MEDIA_NOTIFICATION_ID)
        } catch (_: Exception) {}
    }
}
