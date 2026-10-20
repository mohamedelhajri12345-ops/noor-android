package com.elhajri.noor.tasbih

import android.content.Context
import com.elhajri.noor.audio.SfxManager

/**
 * مشغل أصوات التسبيح — متصل مع SfxManager الموحد للتطبيقات
 */
class TasbihSoundPlayer(private val context: Context) {

    fun playTick() {
        SfxManager.playTasbih(context)
    }

    fun playSuccess() {
        SfxManager.playSuccess(context)
    }

    fun release() {
        // Handled globally by SfxManager
    }
}
