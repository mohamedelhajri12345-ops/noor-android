package com.elhajri.noor.audio.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.elhajri.noor.MainActivity

/**
 * Background playback services (Media3). Each player owns its own service,
 * its own MediaSession and its own MediaStyle notification with lock-screen
 * controls — playback continues with the app closed and the screen locked.
 * 100% native, zero WebView.
 */
class QuranPlayerService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = QuranPlayerManager.exposedPlayer()
        if (player == null) {
            // engine not initialised yet — initialise it with our context
            QuranPlayerManager.ensure(this)
        }
        QuranPlayerManager.exposedPlayer()?.let { p ->
            mediaSession = buildSession(p)
        }
    }

    private fun buildSession(p: ExoPlayer): MediaSession {
        val activityIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return MediaSession.Builder(this, p)
            .setSessionActivity(activityIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = QuranPlayerManager.exposedPlayer()
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}

class NasheedPlayerService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = NasheedPlayerManager.exposedPlayer()
        if (player == null) {
            NasheedPlayerManager.ensure(this)
        }
        NasheedPlayerManager.exposedPlayer()?.let { p ->
            mediaSession = buildSession(p)
        }
    }

    private fun buildSession(p: ExoPlayer): MediaSession {
        val activityIntent = PendingIntent.getActivity(
            this, 1,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return MediaSession.Builder(this, p)
            .setSessionActivity(activityIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = NasheedPlayerManager.exposedPlayer()
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
