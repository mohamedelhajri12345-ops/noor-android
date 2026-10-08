package com.elhajri.noor.videos

import android.content.Context
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

data class KidsVideo(
    val id: String,
    val title: String,
    val category: String,
    val channel: String
) {
    val thumbnailUrl: String
        get() = "https://i.ytimg.com/vi/$id/hqdefault.jpg"
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
                    val channel = obj.optString("channel", "نور للأطفال").trim()

                    if (id.isNotEmpty() && title.isNotEmpty()) {
                        list.add(KidsVideo(id = id, title = title, category = category, channel = channel))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            cachedVideos = list
            return list
        }
    }

    /**
     * Returns "الكل" followed by all unique sorted category names derived from the data.
     */
    fun getCategories(context: Context): List<String> {
        val videos = loadVideos(context)
        val categories = videos.map { it.category }.distinct().sorted()
        return listOf("الكل") + categories
    }

    /**
     * Filters videos by category and/or title search query.
     */
    fun filterVideos(context: Context, category: String? = null, query: String? = null): List<KidsVideo> {
        var result = loadVideos(context)

        if (!category.isNullOrEmpty() && category != "الكل") {
            result = result.filter { it.category == category }
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
            // Loop or fallback to first of category or next in global list
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
