package com.elhajri.noor.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.elhajri.noor.R

/**
 * Lightweight, professional UI sound-effects player.
 * Synthesized in-house (clean, warm tones) to avoid any third-party
 * licensing risk, and kept tiny so they play instantly with no lag.
 */
object SoundEffects {
    private var pool: SoundPool? = null
    private var idClick = 0
    private var idSuccess = 0
    private var idNotification = 0
    private var idTasbih = 0
    private var enabled = true

    fun init(context: Context) {
        if (pool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val p = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build()
        pool = p
        idClick = p.load(context, R.raw.sfx_click, 1)
        idSuccess = p.load(context, R.raw.sfx_success, 1)
        idNotification = p.load(context, R.raw.sfx_notification, 1)
        idTasbih = p.load(context, R.raw.sfx_tasbih, 1)
    }

    fun setEnabled(value: Boolean) { enabled = value }
    fun isEnabled() = enabled

    fun click() { if (enabled) pool?.play(idClick, 0.6f, 0.6f, 0, 0, 1f) }
    fun success() { if (enabled) pool?.play(idSuccess, 0.8f, 0.8f, 0, 0, 1f) }
    fun notification() { if (enabled) pool?.play(idNotification, 0.8f, 0.8f, 0, 0, 1f) }
    fun tasbih() { if (enabled) pool?.play(idTasbih, 0.7f, 0.7f, 0, 0, 1f) }

    fun release() {
        pool?.release()
        pool = null
    }
}
