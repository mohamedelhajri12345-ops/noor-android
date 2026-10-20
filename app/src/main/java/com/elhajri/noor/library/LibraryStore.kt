package com.elhajri.noor.library

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object LibraryStore {

    private const val PREFS_NAME = "noor_library_store_prefs"
    private const val KEY_PROGRESS = "reading_progress_map"
    private const val KEY_BOOKMARKS = "bookmarks_map"
    private const val KEY_DOWNLOADS = "downloaded_files_map"

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDownloadsDir(context: Context): File {
        val dir = File(context.filesDir, "library_downloads")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getLocalFile(context: Context, book: Book): File? {
        // Check if bundled in assets first
        book.bundledAsset?.let { assetPath ->
            try {
                // Copy asset to cache if needed so PdfRenderer can open a File descriptor
                val cacheFile = File(context.cacheDir, "bundled_${book.id}.pdf")
                if (!cacheFile.exists() || cacheFile.length() == 0L) {
                    context.assets.open(assetPath).use { input ->
                        cacheFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                if (cacheFile.exists() && cacheFile.length() > 0L) {
                    return cacheFile
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Check app-private downloaded file
        val downloadedPath = getDownloadedPath(context, book.id)
        if (downloadedPath != null) {
            val file = File(downloadedPath)
            if (file.exists() && file.length() > 0L) return file
        }

        val defaultFile = File(getDownloadsDir(context), "${book.id}.pdf")
        if (defaultFile.exists() && defaultFile.length() > 0L) {
            return defaultFile
        }

        return null
    }

    fun isBookAvailableLocally(context: Context, book: Book): Boolean {
        return getLocalFile(context, book) != null
    }

    fun markDownloaded(context: Context, bookId: String, filePath: String) {
        val downloads = getDownloadsMap(context).toMutableMap()
        downloads[bookId] = filePath

        val jsonObj = JSONObject()
        downloads.forEach { (id, path) -> jsonObj.put(id, path) }
        getPrefs(context).edit().putString(KEY_DOWNLOADS, jsonObj.toString()).apply()
    }

    fun getDownloadedPath(context: Context, bookId: String): String? {
        return getDownloadsMap(context)[bookId]
    }

    fun getDownloadedBooksMap(context: Context): Map<String, String> {
        return getDownloadsMap(context)
    }

    private fun getDownloadsMap(context: Context): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val str = getPrefs(context).getString(KEY_DOWNLOADS, "{}") ?: "{}"
        try {
            val jsonObj = JSONObject(str)
            val keys = jsonObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = jsonObj.getString(k)
            }
        } catch (_: Exception) {}
        return map
    }

    fun deleteDownloadedBook(context: Context, bookId: String): Boolean {
        val path = getDownloadedPath(context, bookId)
        var deleted = false
        if (path != null) {
            val f = File(path)
            if (f.exists()) deleted = f.delete()
        }
        val defaultFile = File(getDownloadsDir(context), "$bookId.pdf")
        if (defaultFile.exists()) deleted = defaultFile.delete() || deleted

        val downloads = getDownloadsMap(context).toMutableMap()
        downloads.remove(bookId)
        val jsonObj = JSONObject()
        downloads.forEach { (id, p) -> jsonObj.put(id, p) }
        getPrefs(context).edit().putString(KEY_DOWNLOADS, jsonObj.toString()).apply()

        return deleted
    }

    // --- Reading Progress ---

    fun saveProgress(context: Context, bookId: String, page: Int, totalPages: Int) {
        val safeTotal = totalPages.coerceAtLeast(1)
        val safePage = page.coerceIn(0, safeTotal - 1)
        val pct = ((safePage + 1) * 100) / safeTotal

        val map = getAllProgress(context).toMutableMap()
        val entry = JSONObject().apply {
            put("lastPage", safePage)
            put("totalPages", safeTotal)
            put("timestamp", System.currentTimeMillis())
            put("pct", pct)
        }
        map[bookId] = entry.toString()

        val rootObj = JSONObject()
        map.forEach { (id, valStr) -> rootObj.put(id, valStr) }
        getPrefs(context).edit().putString(KEY_PROGRESS, rootObj.toString()).apply()
    }

    fun getProgress(context: Context, bookId: String): ReadingProgress? {
        val str = getAllProgress(context)[bookId] ?: return null
        return try {
            val obj = JSONObject(str)
            ReadingProgress(
                bookId = bookId,
                lastPage = obj.optInt("lastPage", 0),
                totalPages = obj.optInt("totalPages", 1),
                lastReadTimestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                progressPercentage = obj.optInt("pct", 0)
            )
        } catch (_: Exception) {
            null
        }
    }

    fun getAllReadingProgressObjects(context: Context): List<ReadingProgress> {
        val list = mutableListOf<ReadingProgress>()
        getAllProgress(context).forEach { (bookId, str) ->
            try {
                val obj = JSONObject(str)
                list.add(
                    ReadingProgress(
                        bookId = bookId,
                        lastPage = obj.optInt("lastPage", 0),
                        totalPages = obj.optInt("totalPages", 1),
                        lastReadTimestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        progressPercentage = obj.optInt("pct", 0)
                    )
                )
            } catch (_: Exception) {}
        }
        return list.sortedByDescending { it.lastReadTimestamp }
    }

    private fun getAllProgress(context: Context): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val str = getPrefs(context).getString(KEY_PROGRESS, "{}") ?: "{}"
        try {
            val jsonObj = JSONObject(str)
            val keys = jsonObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = jsonObj.getString(k)
            }
        } catch (_: Exception) {}
        return map
    }

    // --- Bookmarks ---

    fun toggleBookmark(context: Context, bookId: String, page: Int): Boolean {
        val bookmarks = getBookmarks(context, bookId).toMutableList()
        val isAdded: Boolean
        if (bookmarks.contains(page)) {
            bookmarks.remove(page)
            isAdded = false
        } else {
            bookmarks.add(page)
            bookmarks.sort()
            isAdded = true
        }

        val allBookmarks = getAllBookmarksMap(context).toMutableMap()
        val arr = JSONArray()
        bookmarks.forEach { arr.put(it) }
        allBookmarks[bookId] = arr.toString()

        val rootObj = JSONObject()
        allBookmarks.forEach { (id, valStr) -> rootObj.put(id, valStr) }
        getPrefs(context).edit().putString(KEY_BOOKMARKS, rootObj.toString()).apply()

        return isAdded
    }

    fun getBookmarks(context: Context, bookId: String): List<Int> {
        val list = mutableListOf<Int>()
        val jsonArrStr = getAllBookmarksMap(context)[bookId] ?: "[]"
        try {
            val arr = JSONArray(jsonArrStr)
            for (i in 0 until arr.length()) {
                list.add(arr.getInt(i))
            }
        } catch (_: Exception) {}
        return list
    }

    fun isBookmarked(context: Context, bookId: String, page: Int): Boolean {
        return getBookmarks(context, bookId).contains(page)
    }

    private fun getAllBookmarksMap(context: Context): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val str = getPrefs(context).getString(KEY_BOOKMARKS, "{}") ?: "{}"
        try {
            val jsonObj = JSONObject(str)
            val keys = jsonObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = jsonObj.getString(k)
            }
        } catch (_: Exception) {}
        return map
    }
}
