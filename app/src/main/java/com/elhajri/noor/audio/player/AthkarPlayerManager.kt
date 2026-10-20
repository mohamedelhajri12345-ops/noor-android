package com.elhajri.noor.audio.player

import android.content.Context
import com.elhajri.noor.data.Dhikr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AthkarPlayerState(
    val categoryId: String? = null,
    val categoryTitle: String = "",
    val activeIndex: Int = 0,
    val currentRepeat: Int = 1,
    val targetRepeat: Int = 1,
    val isPlaying: Boolean = false,
    val isAudioModeActive: Boolean = false,
    val autoAdvance: Boolean = true,
    val currentDhikrText: String = "",
    val currentRef: String = "",
    val currentId: String? = null,
    val currentTime: Float = 0f,
    val duration: Float = 0f
)

/**
 * Dedicated player engine for حصن المسلم الصوتي (Audio Hisn al-Muslim).
 * Handles sequential audio recitation with auto-advance and repeat tracking.
 */
object AthkarPlayerManager : BasePlayerEngine("حصن المسلم الصوتي") {

    override fun startForegroundService(context: Context) = NasheedPlaybackService.start(context)
    override fun stopForegroundService(context: Context) = NasheedPlaybackService.stop(context)

    private val _state = MutableStateFlow(AthkarPlayerState())
    val athkarState: StateFlow<AthkarPlayerState> = _state.asStateFlow()

    private var currentDhikrs: List<Dhikr> = emptyList()
    private var observeScope = CoroutineScope(Dispatchers.Main + Job())
    private var isObserving = false

    fun initPlayer(context: Context) {
        ensure(context)
        if (!isObserving) {
            isObserving = true
            observeScope.launch {
                state.collect { pState ->
                    val curr = _state.value
                    var idx = curr.activeIndex
                    pState.currentId?.let { cid ->
                        val parts = cid.split("_")
                        if (parts.size >= 3) {
                            parts.lastOrNull()?.toIntOrNull()?.let { parsedIdx ->
                                if (parsedIdx in currentDhikrs.indices) {
                                    idx = parsedIdx
                                }
                            }
                        }
                    }
                    val currentDhikr = currentDhikrs.getOrNull(idx)

                    _state.value = curr.copy(
                        isPlaying = pState.isPlaying,
                        currentId = pState.currentId,
                        currentTime = pState.currentTime,
                        duration = pState.duration,
                        activeIndex = idx,
                        targetRepeat = currentDhikr?.count ?: curr.targetRepeat,
                        currentDhikrText = currentDhikr?.text ?: curr.currentDhikrText,
                        currentRef = currentDhikr?.ref ?: curr.currentRef
                    )
                }
            }
        }
    }

    fun startCategory(
        context: Context,
        categoryId: String,
        categoryTitle: String,
        dhikrs: List<Dhikr>,
        startIndex: Int = 0
    ) {
        initPlayer(context)
        if (dhikrs.isEmpty()) return

        currentDhikrs = dhikrs
        val idx = startIndex.coerceIn(0, dhikrs.size - 1)
        val d = dhikrs[idx]

        _state.value = AthkarPlayerState(
            categoryId = categoryId,
            categoryTitle = categoryTitle,
            activeIndex = idx,
            currentRepeat = 1,
            targetRepeat = d.count,
            isPlaying = true,
            isAudioModeActive = true,
            autoAdvance = true,
            currentDhikrText = d.text,
            currentRef = d.ref
        )

        val tracks = dhikrs.mapIndexed { i, item ->
            PlayerTrack(
                id = "athkar_${categoryId}_$i",
                url = item.audioUrl,
                title = item.ref.ifBlank { "ذكر ${i + 1}" },
                artist = categoryTitle
            )
        }

        playQueue(tracks, idx)
    }

    fun playDhikrAt(context: Context, index: Int) {
        initPlayer(context)
        if (currentDhikrs.isEmpty() || index !in currentDhikrs.indices) return
        val d = currentDhikrs[index]
        val catId = _state.value.categoryId ?: "default"
        val catTitle = _state.value.categoryTitle

        _state.value = _state.value.copy(
            activeIndex = index,
            currentRepeat = 1,
            targetRepeat = d.count,
            currentDhikrText = d.text,
            currentRef = d.ref,
            isAudioModeActive = true
        )

        val tracks = currentDhikrs.mapIndexed { i, item ->
            PlayerTrack(
                id = "athkar_${catId}_$i",
                url = item.audioUrl,
                title = item.ref.ifBlank { "ذكر ${i + 1}" },
                artist = catTitle
            )
        }

        playQueue(tracks, index)
    }

    fun togglePlayPause(context: Context) {
        initPlayer(context)
        toggle()
    }

    fun nextDhikr(context: Context) {
        initPlayer(context)
        if (currentDhikrs.isEmpty()) return
        val nextIdx = (_state.value.activeIndex + 1) % currentDhikrs.size
        playDhikrAt(context, nextIdx)
    }

    fun previousDhikr(context: Context) {
        initPlayer(context)
        if (currentDhikrs.isEmpty()) return
        val prevIdx = if (_state.value.activeIndex > 0) _state.value.activeIndex - 1 else currentDhikrs.size - 1
        playDhikrAt(context, prevIdx)
    }

    fun stopAudioMode(context: Context) {
        stop()
        _state.value = _state.value.copy(
            isPlaying = false,
            isAudioModeActive = false
        )
    }

    fun incrementRepeat(context: Context) {
        val curr = _state.value
        if (curr.currentRepeat < curr.targetRepeat) {
            val nextRep = curr.currentRepeat + 1
            _state.value = curr.copy(currentRepeat = nextRep)
            curr.categoryId?.let { catId ->
                val sp = context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE)
                val key = "athkar_count_${catId}_${curr.activeIndex}"
                val saved = sp.getInt(key, 0)
                sp.edit().putInt(key, saved + 1).apply()
            }
        } else {
            nextDhikr(context)
        }
    }
}
