package com.elhajri.noor.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

/**
 * محرك أصوات التفاعل (TapSound Engine) — يوفر أصوات لمس وتحديد وإنجاز هادئة وفاخرة
 * تعتمد على SoundPool لقليلي زمن الاستجابة (Low Latency).
 */
object NoorTapSound {

    private const val PREFS_NAME = "noor_settings"
    private const val KEY_SOUND_ENABLED = "nur_sound_enabled"

    private var soundPool: SoundPool? = null
    private var tickSoundId: Int = 0
    private var selectSoundId: Int = 0
    private var successSoundId: Int = 0

    fun isSoundEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    @Synchronized
    private fun initSoundPool(context: Context) {
        if (soundPool != null) return
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build()

            val tickResId = getRawResId(context, "tap_tick")
            val selectResId = getRawResId(context, "tap_select")
            val successResId = getRawResId(context, "tap_success")

            if (tickResId != 0) {
                tickSoundId = pool.load(context, tickResId, 1)
            } else {
                try {
                    context.assets.openFd("audio/tap_tick.wav").use { fd ->
                        tickSoundId = pool.load(fd, 1)
                    }
                } catch (_: Exception) {}
            }

            if (selectResId != 0) {
                selectSoundId = pool.load(context, selectResId, 1)
            } else {
                try {
                    context.assets.openFd("audio/tap_select.wav").use { fd ->
                        selectSoundId = pool.load(fd, 1)
                    }
                } catch (_: Exception) {}
            }

            if (successResId != 0) {
                successSoundId = pool.load(context, successResId, 1)
            } else {
                try {
                    context.assets.openFd("audio/tap_success.wav").use { fd ->
                        successSoundId = pool.load(fd, 1)
                    }
                } catch (_: Exception) {}
            }

            soundPool = pool
        } catch (e: Exception) {
            Log.e("NoorTapSound", "Error initializing SoundPool", e)
        }
    }

    private fun getRawResId(context: Context, name: String): Int {
        return try {
            context.resources.getIdentifier(name, "raw", context.packageName)
        } catch (e: Exception) {
            0
        }
    }

    /** نقرة أزرار خفيفة ولطيفة (~40ms) */
    fun tap(context: Context) {
        playSound(context) { tickSoundId }
    }

    /** نغمة تحديد دافئة عند اختيار عنصر أو تبويب (~55ms) */
    fun select(context: Context) {
        playSound(context) { selectSoundId }
    }

    /** جرس نجاح خفيف وهادئ عند إتمام عمل أو شارة (~80ms) */
    fun success(context: Context) {
        playSound(context) { successSoundId }
    }

    private fun playSound(context: Context, soundIdProvider: () -> Int) {
        if (!isSoundEnabled(context)) return
        val appContext = context.applicationContext
        if (soundPool == null) {
            initSoundPool(appContext)
        }
        // Read the id AFTER init so the very first tap is not silent
        val soundId = soundIdProvider()
        try {
            val pool = soundPool ?: return
            if (soundId != 0) {
                pool.play(soundId, 0.65f, 0.7f, 1, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.e("NoorTapSound", "Error playing sound ID $soundId", e)
        }
    }

    fun release() {
        try {
            soundPool?.release()
        } catch (_: Exception) {}
        soundPool = null
        tickSoundId = 0
        selectSoundId = 0
        successSoundId = 0
    }
}

/** اسم مستعار لتسهيل الاستدعاء (e.g. TapSound.tap(context)) */
typealias TapSound = NoorTapSound
