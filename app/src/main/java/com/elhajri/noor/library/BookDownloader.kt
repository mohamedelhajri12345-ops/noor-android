package com.elhajri.noor.library

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Float, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
    data class Completed(val filePath: String) : DownloadState()
    data class Error(val message: String) : DownloadState()
}

object BookDownloader {

    private val _downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, DownloadState>> = _downloadStates.asStateFlow()

    fun getDownloadState(bookId: String): DownloadState {
        return _downloadStates.value[bookId] ?: DownloadState.Idle
    }

    suspend fun downloadBook(
        context: Context,
        book: Book,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.IO) {
        val bookId = book.id
        val targetFile = File(LibraryStore.getDownloadsDir(context), "$bookId.pdf")

        // If already downloaded and valid, return immediately
        if (targetFile.exists() && targetFile.length() > 0L) {
            LibraryStore.markDownloaded(context, bookId, targetFile.absolutePath)
            updateState(bookId, DownloadState.Completed(targetFile.absolutePath))
            withContext(Dispatchers.Main) {
                onComplete(true, targetFile.absolutePath)
            }
            return@withContext
        }

        updateState(bookId, DownloadState.Downloading(0f, 0, 0))

        try {
            var urlStr = book.url
            if (urlStr.isBlank()) {
                updateState(bookId, DownloadState.Error("رابط التحميل غير متوفر"))
                withContext(Dispatchers.Main) { onComplete(false, "رابط التحميل غير متوفر") }
                return@withContext
            }

            var connection: HttpURLConnection? = null
            var redirectCount = 0
            while (redirectCount < 5) {
                val url = URL(urlStr)
                connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; NOOR-App)")
                connection.connectTimeout = 15000
                connection.readTimeout = 30000

                val responseCode = connection.responseCode
                if (responseCode in 300..399) {
                    val loc = connection.getHeaderField("Location")
                    if (loc != null) {
                        urlStr = loc
                        redirectCount++
                        continue
                    }
                }
                break
            }

            if (connection == null || connection.responseCode !in 200..299) {
                val code = connection?.responseCode ?: -1
                val err = "فشل الاتصال بالسيرفر ($code)"
                updateState(bookId, DownloadState.Error(err))
                withContext(Dispatchers.Main) { onComplete(false, err) }
                return@withContext
            }

            val fileLength = connection.contentLengthLong
            val tempFile = File(context.cacheDir, "temp_$bookId.pdf")

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
                            updateState(bookId, DownloadState.Downloading(progress, total, fileLength))
                        }
                    }
                }
            }

            if (tempFile.exists() && tempFile.length() > 0L) {
                if (targetFile.exists()) targetFile.delete()
                tempFile.renameTo(targetFile)
                LibraryStore.markDownloaded(context, bookId, targetFile.absolutePath)
                updateState(bookId, DownloadState.Completed(targetFile.absolutePath))
                withContext(Dispatchers.Main) {
                    onComplete(true, targetFile.absolutePath)
                }
            } else {
                updateState(bookId, DownloadState.Error("الملف المنزّل فارغ"))
                withContext(Dispatchers.Main) { onComplete(false, "الملف المنزّل فارغ") }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            val errMsg = e.localizedMessage ?: "حدث خطأ أثناء التحميل"
            updateState(bookId, DownloadState.Error(errMsg))
            withContext(Dispatchers.Main) { onComplete(false, errMsg) }
        }
    }

    private fun updateState(bookId: String, state: DownloadState) {
        val map = _downloadStates.value.toMutableMap()
        map[bookId] = state
        _downloadStates.value = map
    }
}
