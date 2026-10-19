package com.elhajri.noor.tasbih

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build

class TasbihSoundPlayer(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var tickSoundId: Int = 0
    private var successSoundId: Int = 0

    init {
        try {
            soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .build()
            } else {
                @Suppress("DEPRECATION")
                SoundPool(5, android.media.AudioManager.STREAM_MUSIC, 0)
            }

            try {
                val tickAfdf = context.assets.openFd("audio/tap_tick.wav")
                tickSoundId = soundPool?.load(tickAfdf, 1) ?: 0
            } catch (_: Exception) {}

            try {
                val successAfdf = context.assets.openFd("audio/tap_success.wav")
                successSoundId = soundPool?.load(successAfdf, 1) ?: 0
            } catch (_: Exception) {}

        } catch (_: Exception) {}
    }

    fun playTick() {
        if (tickSoundId != 0) {
            soundPool?.play(tickSoundId, 0.5f, 0.5f, 1, 0, 1.0f)
        }
    }

    fun playSuccess() {
        if (successSoundId != 0) {
            soundPool?.play(successSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
        }
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
        } catch (_: Exception) {}
    }
}
