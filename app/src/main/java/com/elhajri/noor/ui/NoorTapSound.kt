package com.elhajri.noor.ui

import android.content.Context
import com.elhajri.noor.audio.SfxManager

/**
 * محرك أصوات التفاعل (TapSound Engine) — موجه مباشرة إلى SfxManager
 */
object NoorTapSound {

    fun isSoundEnabled(context: Context): Boolean = SfxManager.isEnabled(context)

    fun setSoundEnabled(context: Context, enabled: Boolean) = SfxManager.setEnabled(context, enabled)

    fun tap(context: Context) {
        SfxManager.playTap(context)
    }

    fun select(context: Context) {
        SfxManager.playButton(context)
    }

    fun success(context: Context) {
        SfxManager.playSuccess(context)
    }

    fun release() {
        SfxManager.release()
    }
}

/** اسم مستعار لتسهيل الاستدعاء (e.g. TapSound.tap(context)) */
typealias TapSound = NoorTapSound
