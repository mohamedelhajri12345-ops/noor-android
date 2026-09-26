package com.elhajri.noor.audio.player

/**
 * TWO COMPLETELY SEPARATE players, exactly as the user requires:
 *  - QuranPlayerManager : the mushaf player (QuranReaderScreen)
 *  - NasheedPlayerManager : the nasheed player (LibraryScreen)
 * Both are faithful ports of the web audioManager.js. Starting one
 * pauses the other — they never play simultaneously.
 */
object PlayerInterop {
    private val engines = mutableListOf<PlayerEngine>()

    fun register(engine: PlayerEngine) {
        if (!engines.contains(engine)) engines.add(engine)
    }

    fun pauseOthers(engine: PlayerEngine) {
        engines.forEach {
            if (it !== engine) {
                val p = it.exposedPlayer()
                if (p != null && p.isPlaying) p.pause()
            }
        }
    }
}

object QuranPlayerManager {
    val engine = PlayerEngine(QuranPlayerService::class.java)

    init { PlayerInterop.register(engine) }

    val state get() = engine.state

    fun ensure(context: android.content.Context) { engine.ensurePlayer(context) }
    fun exposedPlayer() = engine.exposedPlayer()

    fun playQueue(tracks: List<PlayerTrack>, startIndex: Int = 0) = engine.playQueue(tracks, startIndex)
    fun play(track: PlayerTrack) = engine.play(track)
    fun toggle() = engine.toggle()
    fun stop() = engine.stop()
    fun next() = engine.next()
    fun prev() = engine.prev()
    fun seek(seconds: Float) = engine.seek(seconds)
    fun setRate(rate: Float) = engine.setRate(rate)
    fun toggleShuffle() = engine.toggleShuffle()
    fun toggleRepeat() = engine.toggleRepeat()
    fun setSleepTimer(minutes: Int) = engine.setSleepTimer(minutes)
    fun clearSleepTimer() = engine.clearSleepTimer()
    fun release() = engine.releasePlayer()
}

object NasheedPlayerManager {
    val engine = PlayerEngine(NasheedPlayerService::class.java)

    init { PlayerInterop.register(engine) }

    val state get() = engine.state

    fun ensure(context: android.content.Context) { engine.ensurePlayer(context) }
    fun exposedPlayer() = engine.exposedPlayer()

    fun playQueue(tracks: List<PlayerTrack>, startIndex: Int = 0) = engine.playQueue(tracks, startIndex)
    fun play(track: PlayerTrack) = engine.play(track)
    fun toggle() = engine.toggle()
    fun stop() = engine.stop()
    fun next() = engine.next()
    fun prev() = engine.prev()
    fun seek(seconds: Float) = engine.seek(seconds)
    fun setRate(rate: Float) = engine.setRate(rate)
    fun toggleShuffle() = engine.toggleShuffle()
    fun toggleRepeat() = engine.toggleRepeat()
    fun setSleepTimer(minutes: Int) = engine.setSleepTimer(minutes)
    fun clearSleepTimer() = engine.clearSleepTimer()
    fun release() = engine.releasePlayer()
}
