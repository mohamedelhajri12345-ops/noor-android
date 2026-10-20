package com.elhajri.noor.offline

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.elhajri.noor.library.Book
import com.elhajri.noor.library.BookDownloader
import com.elhajri.noor.library.LibraryStore
import com.elhajri.noor.settings.NoorLocale
import com.elhajri.noor.settings.t
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

enum class OfflineItemType {
    NASHEED,
    BOOK,
    VIDEO
}

data class OfflineItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: OfflineItemType,
    val sizeBytes: Long,
    val localPath: String,
    val url: String
)

data class OfflineDownloadStatus(
    val isDownloading: Boolean = false,
    val progress: Float = 0f,
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val error: String? = null
)

object OfflineManager {

    private const val PREFS_NAME = "noor_offline_manager_prefs"
    private const val KEY_NASHEEDS_META = "downloaded_nasheeds_meta"
    private const val KEY_VIDEOS_META = "downloaded_videos_meta"

    private val _downloadStatus = MutableStateFlow<Map<String, OfflineDownloadStatus>>(emptyMap())
    val downloadStatus: StateFlow<Map<String, OfflineDownloadStatus>> = _downloadStatus.asStateFlow()

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNet = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(activeNet) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    // --- Directory Helpers ---

    fun getNasheedsDir(context: Context): File {
        val dir = File(context.filesDir, "downloads/nasheeds")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getVideosDir(context: Context): File {
        val dir = File(context.filesDir, "downloads/videos")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    // --- Nasheeds Download & Local Check ---

    fun getNasheedFile(context: Context, nasheedId: String): File? {
        val file = File(getNasheedsDir(context), "$nasheedId.mp3")
        return if (file.exists() && file.length() > 0L) file else null
    }

    fun isNasheedDownloaded(context: Context, nasheedId: String): Boolean {
        return getNasheedFile(context, nasheedId) != null
    }

    suspend fun downloadNasheed(
        context: Context,
        id: String,
        title: String,
        artist: String,
        url: String,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val file = File(getNasheedsDir(context), "$id.mp3")
        if (file.exists() && file.length() > 0L) {
            saveNasheedMeta(context, id, title, artist, url, file.length(), file.absolutePath)
            updateStatus(id, OfflineDownloadStatus(isDownloading = false, progress = 1f, bytesDownloaded = file.length(), totalBytes = file.length()))
            withContext(Dispatchers.Main) { onComplete(true, file.absolutePath) }
            return@withContext
        }

        downloadFile(
            itemId = id,
            targetFile = file,
            urlStr = url,
            context = context,
            onSaved = { length ->
                saveNasheedMeta(context, id, title, artist, url, length, file.absolutePath)
            },
            onComplete = onComplete
        )
    }

    fun deleteNasheed(context: Context, nasheedId: String): Boolean {
        val file = File(getNasheedsDir(context), "$nasheedId.mp3")
        var deleted = false
        if (file.exists()) deleted = file.delete()

        val metaMap = getNasheedsMetaMap(context).toMutableMap()
        metaMap.remove(nasheedId)
        saveMetaMap(context, KEY_NASHEEDS_META, metaMap)
        return deleted
    }

    // --- Videos Download & Local Check ---

    fun getVideoFile(context: Context, videoId: String): File? {
        val file = File(getVideosDir(context), "$videoId.mp4")
        return if (file.exists() && file.length() > 0L) file else null
    }

    fun isVideoDownloaded(context: Context, videoId: String): Boolean {
        return getVideoFile(context, videoId) != null
    }

    suspend fun downloadVideo(
        context: Context,
        id: String,
        title: String,
        channelOrCategory: String,
        url: String,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val file = File(getVideosDir(context), "$id.mp4")
        if (file.exists() && file.length() > 0L) {
            saveVideoMeta(context, id, title, channelOrCategory, url, file.length(), file.absolutePath)
            updateStatus(id, OfflineDownloadStatus(isDownloading = false, progress = 1f, bytesDownloaded = file.length(), totalBytes = file.length()))
            withContext(Dispatchers.Main) { onComplete(true, file.absolutePath) }
            return@withContext
        }

        downloadFile(
            itemId = id,
            targetFile = file,
            urlStr = url,
            context = context,
            onSaved = { length ->
                saveVideoMeta(context, id, title, channelOrCategory, url, length, file.absolutePath)
            },
            onComplete = onComplete
        )
    }

    fun deleteVideo(context: Context, videoId: String): Boolean {
        val file = File(getVideosDir(context), "$videoId.mp4")
        var deleted = false
        if (file.exists()) deleted = file.delete()

        val metaMap = getVideosMetaMap(context).toMutableMap()
        metaMap.remove(videoId)
        saveMetaMap(context, KEY_VIDEOS_META, metaMap)
        return deleted
    }

    // --- Shared Downloader ---

    private suspend fun downloadFile(
        itemId: String,
        targetFile: File,
        urlStr: String,
        context: Context,
        onSaved: (Long) -> Unit,
        onComplete: (Boolean, String?) -> Unit
    ) {
        updateStatus(itemId, OfflineDownloadStatus(isDownloading = true, progress = 0f))

        try {
            var currentUrl = urlStr
            if (currentUrl.isBlank()) {
                val err = "رابط التحميل غير متوفر"
                updateStatus(itemId, OfflineDownloadStatus(isDownloading = false, error = err))
                withContext(Dispatchers.Main) { onComplete(false, err) }
                return
            }

            var connection: HttpURLConnection? = null
            var redirectCount = 0
            while (redirectCount < 5) {
                val url = URL(currentUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; NOOR-App)")
                connection.connectTimeout = 15000
                connection.readTimeout = 30000

                val responseCode = connection.responseCode
                if (responseCode in 300..399) {
                    val loc = connection.getHeaderField("Location")
                    if (loc != null) {
                        currentUrl = loc
                        redirectCount++
                        continue
                    }
                }
                break
            }

            if (connection == null || connection.responseCode !in 200..299) {
                val code = connection?.responseCode ?: -1
                val err = "تعذّر الاتصال بالسيرفر ($code)"
                updateStatus(itemId, OfflineDownloadStatus(isDownloading = false, error = err))
                withContext(Dispatchers.Main) { onComplete(false, err) }
                return
            }

            val fileLength = connection.contentLengthLong
            val tempFile = File(context.cacheDir, "temp_offline_$itemId")

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var total: Long = 0
                    var count: Int
                    while (input.read(buffer).also { count = it } != -1) {
                        output.write(buffer, 0, count)
                        total += count
                        if (fileLength > 0) {
                            val progress = (total.toFloat() / fileLength.toFloat()).coerceIn(0f, 1f)
                            updateStatus(itemId, OfflineDownloadStatus(isDownloading = true, progress = progress, bytesDownloaded = total, totalBytes = fileLength))
                        }
                    }
                }
            }

            if (tempFile.exists() && tempFile.length() > 0L) {
                if (targetFile.exists()) targetFile.delete()
                tempFile.renameTo(targetFile)
                val finalLength = targetFile.length()
                onSaved(finalLength)
                updateStatus(itemId, OfflineDownloadStatus(isDownloading = false, progress = 1f, bytesDownloaded = finalLength, totalBytes = finalLength))
                withContext(Dispatchers.Main) { onComplete(true, targetFile.absolutePath) }
            } else {
                val err = "الملف المنزّل فارغ"
                updateStatus(itemId, OfflineDownloadStatus(isDownloading = false, error = err))
                withContext(Dispatchers.Main) { onComplete(false, err) }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            val errMsg = e.localizedMessage ?: "حدث خطأ أثناء التحميل"
            updateStatus(itemId, OfflineDownloadStatus(isDownloading = false, error = errMsg))
            withContext(Dispatchers.Main) { onComplete(false, errMsg) }
        }
    }

    private fun updateStatus(id: String, status: OfflineDownloadStatus) {
        val map = _downloadStatus.value.toMutableMap()
        map[id] = status
        _downloadStatus.value = map
    }

    // --- Metadata Persistence ---

    private fun saveNasheedMeta(context: Context, id: String, title: String, artist: String, url: String, sizeBytes: Long, localPath: String) {
        val map = getNasheedsMetaMap(context).toMutableMap()
        val obj = JSONObject().apply {
            put("id", id)
            put("title", title)
            put("artist", artist)
            put("url", url)
            put("sizeBytes", sizeBytes)
            put("localPath", localPath)
        }
        map[id] = obj.toString()
        saveMetaMap(context, KEY_NASHEEDS_META, map)
    }

    private fun saveVideoMeta(context: Context, id: String, title: String, channel: String, url: String, sizeBytes: Long, localPath: String) {
        val map = getVideosMetaMap(context).toMutableMap()
        val obj = JSONObject().apply {
            put("id", id)
            put("title", title)
            put("channel", channel)
            put("url", url)
            put("sizeBytes", sizeBytes)
            put("localPath", localPath)
        }
        map[id] = obj.toString()
        saveMetaMap(context, KEY_VIDEOS_META, map)
    }

    private fun getNasheedsMetaMap(context: Context): Map<String, String> = getMetaMap(context, KEY_NASHEEDS_META)
    private fun getVideosMetaMap(context: Context): Map<String, String> = getMetaMap(context, KEY_VIDEOS_META)

    private fun getMetaMap(context: Context, key: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val str = getPrefs(context).getString(key, "{}") ?: "{}"
        try {
            val root = JSONObject(str)
            val keys = root.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = root.getString(k)
            }
        } catch (_: Exception) {}
        return map
    }

    private fun saveMetaMap(context: Context, key: String, map: Map<String, String>) {
        val root = JSONObject()
        map.forEach { (k, v) -> root.put(k, v) }
        getPrefs(context).edit().putString(key, root.toString()).apply()
    }

    // --- Query All Downloaded Items ---

    fun getAllDownloadedItems(context: Context): List<OfflineItem> {
        val list = mutableListOf<OfflineItem>()

        // 1. Nasheeds
        getNasheedsMetaMap(context).forEach { (id, jsonStr) ->
            try {
                val obj = JSONObject(jsonStr)
                val path = obj.getString("localPath")
                val file = File(path)
                if (file.exists() && file.length() > 0L) {
                    list.add(
                        OfflineItem(
                            id = id,
                            title = obj.getString("title"),
                            subtitle = obj.optString("artist", t("أنشيد إسلامي", "Islamic Nasheed")),
                            type = OfflineItemType.NASHEED,
                            sizeBytes = file.length(),
                            localPath = path,
                            url = obj.optString("url", "")
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        // 2. Videos
        getVideosMetaMap(context).forEach { (id, jsonStr) ->
            try {
                val obj = JSONObject(jsonStr)
                val path = obj.getString("localPath")
                val file = File(path)
                if (file.exists() && file.length() > 0L) {
                    list.add(
                        OfflineItem(
                            id = id,
                            title = obj.getString("title"),
                            subtitle = obj.optString("channel", t("فيديو التطبيق", "App Video")),
                            type = OfflineItemType.VIDEO,
                            sizeBytes = file.length(),
                            localPath = path,
                            url = obj.optString("url", "")
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        // 3. Books
        val downloadedBooksMap = LibraryStore.getDownloadedBooksMap(context)
        downloadedBooksMap.forEach { (bookId, path) ->
            val file = File(path)
            if (file.exists() && file.length() > 0L) {
                list.add(
                    OfflineItem(
                        id = bookId,
                        title = t("كتاب: ", "Book: ") + bookId,
                        subtitle = t("مكتبة نور الإسلامية", "NOOR Islamic Library"),
                        type = OfflineItemType.BOOK,
                        sizeBytes = file.length(),
                        localPath = path,
                        url = ""
                    )
                )
            }
        }

        return list.sortedBy { it.title }
    }

    fun getTotalStorageUsageBytes(context: Context): Long {
        return getAllDownloadedItems(context).sumOf { it.sizeBytes }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0L) return "0 ${t("ميغابايت", "MB")}"
        val mb = bytes.toDouble() / (1024 * 1024)
        return if (mb >= 1024) {
            val gb = mb / 1024
            val formatted = String.format("%.2f", gb)
            NoorLocale.formatDigits(formatted) + " " + t("غيغابايت", "GB")
        } else {
            val formatted = String.format("%.1f", mb)
            NoorLocale.formatDigits(formatted) + " " + t("ميغابايت", "MB")
        }
    }
}
