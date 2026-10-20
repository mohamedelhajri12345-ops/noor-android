package com.elhajri.noor.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.elhajri.noor.audio.player.QuranPlayerManager

/**
 * SfxManager: App-wide sound effects engine over SoundPool for NOOR app.
 * Provides low-latency, gentle volume audio cues for button taps, game events,
 * celebrations, tasbih ticks, and page turns.
 *
 * Guarantees:
 * 1. Respects user mute toggle persisted in SharedPreferences ("noor_settings").
 * 2. Mutes instantly whenever Quran recitation is actively playing.
 */
object SfxManager {

    private const val PREFS_NAME = "noor_settings"
    private const val KEY_SFX_ENABLED = "nur_sound_enabled"
    private const val KEY_SFX_ENABLED_ALT = "nur_sfx_enabled"

    private var soundPool: SoundPool? = null
    private val soundMap = HashMap<SfxType, Int>()
    @Volatile
    private var isInitialized = false

    enum class SfxType {
        TAP,          // soft tap click
        BUTTON,       // subtle button press
        SUCCESS,      // gentle success chime
        WRONG,        // soft thud for wrong
        TASBIH,       // tasbih tick
        LEVEL_UP,     // short elegant level-up fanfare
        PAGE_TURN,    // page turn paper rustle
        CELEBRATION,  // completion celebratory tone
        WHOOSH,       // transition whoosh
        CARD_FLIP     // card flip / toggle
    }

    fun isEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SFX_ENABLED, true) && prefs.getBoolean(KEY_SFX_ENABLED_ALT, true)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_SFX_ENABLED, enabled)
            .putBoolean(KEY_SFX_ENABLED_ALT, enabled)
            .apply()
    }

    @Synchronized
    fun init(context: Context) {
        if (isInitialized) return
        val appContext = context.applicationContext
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(audioAttributes)
                .build()

            val rawNames = mapOf(
                SfxType.TAP to "sfx_tap",
                SfxType.BUTTON to "sfx_button",
                SfxType.SUCCESS to "sfx_success",
                SfxType.WRONG to "sfx_wrong",
                SfxType.TASBIH to "sfx_tasbih",
                SfxType.LEVEL_UP to "sfx_levelup",
                SfxType.PAGE_TURN to "sfx_pageturn",
                SfxType.CELEBRATION to "sfx_celebration",
                SfxType.WHOOSH to "sfx_whoosh",
                SfxType.CARD_FLIP to "sfx_cardflip"
            )

            for ((sfxType, rawName) in rawNames) {
                var resId = getRawResId(appContext, rawName)
                if (resId == 0) {
                    if (rawName == "sfx_tap") resId = getRawResId(appContext, "tap_tick")
                    if (rawName == "sfx_success") resId = getRawResId(appContext, "tap_success")
                }
                if (resId != 0) {
                    val soundId = pool.load(appContext, resId, 1)
                    if (soundId != 0) {
                        soundMap[sfxType] = soundId
                    }
                }
            }

            soundPool = pool
            isInitialized = true
        } catch (e: Exception) {
            Log.e("SfxManager", "Error initializing SoundPool", e)
        }
    }

    private fun getRawResId(context: Context, name: String): Int {
        return try {
            context.resources.getIdentifier(name, "raw", context.packageName)
        } catch (_: Exception) {
            0
        }
    }

    private fun isQuranPlaying(): Boolean {
        return try {
            QuranPlayerManager.isPlayingNow() || QuranPlayerManager.state.value.isPlaying
        } catch (_: Exception) {
            false
        }
    }

    fun play(context: Context, type: SfxType) {
        if (!isEnabled(context)) return
        if (isQuranPlaying()) return

        val appContext = context.applicationContext
        if (!isInitialized || soundPool == null) {
            init(appContext)
        }

        val pool = soundPool ?: return
        val soundId = soundMap[type] ?: return
        if (soundId == 0) return

        val volume = when (type) {
            SfxType.TAP -> 0.35f
            SfxType.BUTTON -> 0.30f
            SfxType.TASBIH -> 0.40f
            SfxType.PAGE_TURN -> 0.35f
            SfxType.WHOOSH -> 0.35f
            SfxType.CARD_FLIP -> 0.35f
            SfxType.SUCCESS -> 0.50f
            SfxType.WRONG -> 0.45f
            SfxType.LEVEL_UP -> 0.55f
            SfxType.CELEBRATION -> 0.60f
        }

        try {
            pool.play(soundId, volume, volume, 1, 0, 1.0f)
        } catch (e: Exception) {
            Log.e("SfxManager", "Error playing SFX $type", e)
        }
    }

    // Convenience functions
    fun playTap(context: Context) = play(context, SfxType.TAP)
    fun playButton(context: Context) = play(context, SfxType.BUTTON)
    fun playSuccess(context: Context) = play(context, SfxType.SUCCESS)
    fun playWrong(context: Context) = play(context, SfxType.WRONG)
    fun playTasbih(context: Context) = play(context, SfxType.TASBIH)
    fun playLevelUp(context: Context) = play(context, SfxType.LEVEL_UP)
    fun playPageTurn(context: Context) = play(context, SfxType.PAGE_TURN)
    fun playCelebration(context: Context) = play(context, SfxType.CELEBRATION)
    fun playWhoosh(context: Context) = play(context, SfxType.WHOOSH)
    fun playCardFlip(context: Context) = play(context, SfxType.CARD_FLIP)

    fun release() {
        try {
            soundPool?.release()
        } catch (_: Exception) {}
        soundPool = null
        soundMap.clear()
        isInitialized = false
    }
}
