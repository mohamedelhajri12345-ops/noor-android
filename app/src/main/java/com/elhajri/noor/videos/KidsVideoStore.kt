package com.elhajri.noor.videos
import com.elhajri.noor.settings.t

import android.content.Context
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

data class KidsVideo(
    val id: String,
    val title: String,
    val category: String,
    val channel: String,
    /** Direct MP4 file URL (archive.org etc.) — plays natively with ZERO login. Null = YouTube mode. */
    val mp4Url: String? = null
) {
    val thumbnailUrl: String
        get() = if (id.startsWith("mp4_") || id.isBlank()) "" else "https://i.ytimg.com/vi/$id/hqdefault.jpg"
}

/**
 * Singleton repository for Kids Videos catalog.
 * Handles loading from assets once and provides efficient memory filtering and searches
 * for 500+ items.
 */
object KidsVideoStore {

    @Volatile
    private var cachedVideos: List<KidsVideo>? = null

    /**
     * Clears the in-memory cache so the next load re-reads assets
     * and re-applies the broken-videos filter.
     */
    fun invalidate() {
        synchronized(this) { cachedVideos = null }
    }

    /**
     * Loads all videos from assets/data/videos_kids.json.
     * Caches in memory on first call for zero IO overhead on subsequent queries.
     */
    fun loadVideos(context: Context): List<KidsVideo> {
        cachedVideos?.let { return it }

        synchronized(this) {
            cachedVideos?.let { return it }

            val list = mutableListOf<KidsVideo>()
            try {
                val inputStream = context.assets.open("data/videos_kids.json")
                val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                val jsonString = reader.use { it.readText() }
                val jsonArray = JSONArray(jsonString)

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.optString("id", "").trim()
                    val title = obj.optString("title", "").trim()
                    val category = obj.optString("category", "عام").trim()
                    val channel = obj.optString("channel", t("نور للأطفال", "NOOR Kids")).trim()

                    val mp4 = obj.optString("mp4", "").trim()
                    if (id.isNotEmpty() && title.isNotEmpty()) {
                        list.add(KidsVideo(id = id, title = title, category = category, channel = channel, mp4Url = mp4.ifBlank { null }))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Exclude videos known to be unavailable/broken at runtime
            val broken = try { com.elhajri.noor.data.Prefs.getBrokenVideos(context) } catch (e: Exception) { emptySet() }
            cachedVideos = if (broken.isEmpty()) list else list.filter { it.id !in broken }
            return cachedVideos!!
        }
    }

    /**
     * Returns "الكل", t("فيديوهات التطبيق", "App Videos") followed by all unique sorted category names derived from the data.
     */
    fun getCategories(context: Context): List<String> {
        val videos = loadVideos(context)
        val categories = videos.map { it.category }.filter { it != t("فيديوهات التطبيق", "App Videos") }.distinct().sorted()
        return listOf("الكل", t("فيديوهات التطبيق", "App Videos")) + categories
    }

    /**
     * Filters videos by category and/or title search query.
     */
    fun filterVideos(context: Context, category: String? = null, query: String? = null): List<KidsVideo> {
        var result = loadVideos(context)

        if (!category.isNullOrEmpty() && category != "الكل") {
            if (category == t("فيديوهات التطبيق", "App Videos")) {
                result = result.filter { it.mp4Url != null || it.id.startsWith("mp4_") }
            } else {
                result = result.filter { it.category == category }
            }
        }

        if (!query.isNullOrBlank()) {
            val q = query.trim().lowercase()
            result = result.filter { it.title.lowercase().contains(q) || it.channel.lowercase().contains(q) }
        }

        return result
    }

    /**
     * Gets a single video by its YouTube ID.
     */
    fun getVideoById(context: Context, videoId: String): KidsVideo? {
        return loadVideos(context).firstOrNull { it.id == videoId }
    }

    /**
     * Finds the next video in the same category (or next video in list if category ends).
     */
    fun getNextVideo(context: Context, currentVideoId: String): KidsVideo? {
        val all = loadVideos(context)
        val current = all.firstOrNull { it.id == currentVideoId } ?: return all.firstOrNull()

        val categoryVideos = all.filter { it.category == current.category }
        val currentIndex = categoryVideos.indexOfFirst { it.id == currentVideoId }

        return if (currentIndex in 0 until categoryVideos.size - 1) {
            categoryVideos[currentIndex + 1]
        } else {
            val globalIndex = all.indexOfFirst { it.id == currentVideoId }
            if (globalIndex in 0 until all.size - 1) all[globalIndex + 1] else categoryVideos.firstOrNull()
        }
    }

    /**
     * Finds the previous video in the same category.
     */
    fun getPrevVideo(context: Context, currentVideoId: String): KidsVideo? {
        val all = loadVideos(context)
        val current = all.firstOrNull { it.id == currentVideoId } ?: return null

        val categoryVideos = all.filter { it.category == current.category }
        val currentIndex = categoryVideos.indexOfFirst { it.id == currentVideoId }

        return if (currentIndex > 0) {
            categoryVideos[currentIndex - 1]
        } else {
            categoryVideos.lastOrNull()
        }
    }
}
