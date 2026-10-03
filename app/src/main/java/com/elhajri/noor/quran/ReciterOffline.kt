package com.elhajri.noor.quran

import com.elhajri.noor.data.Reciter

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * تنزيل التلاوات كاملة للاستخدام بدون إنترنت:
 * ينزّل ملفات 114 سورة mp3 لقارئ معيّن إلى ذاكرة التطبيق،
 * مع استئناف تلقائي (يتخطى الملفات المكتملة) وإمكانية الإلغاء،
 * والمشغل يفضّل الملف المحلي عند وجوده.
 */
object ReciterOffline {

    /** تقدم كل قارئ: المنزّل من 114 */
    val progress = mutableStateMapOf<String, Int>()
    /** القارئ قيد التنزيل حالياً */
    var downloading by mutableStateOf<String?>(null)
        private set
    @Volatile private var cancelRequested = false
    @Volatile private var worker: Thread? = null

    fun dir(ctx: Context, reciterId: String): File =
        File(ctx.filesDir, "quran_audio/$reciterId").apply { mkdirs() }

    fun localFile(ctx: Context, reciterId: String, surah: Int): File =
        File(dir(ctx, reciterId), String.format("%03d.mp3", surah))

    fun hasLocal(ctx: Context, reciterId: String, surah: Int): Boolean =
        localFile(ctx, reciterId, surah).let { it.exists() && it.length() > 1024 }

    /** عدد السور المنزّلة محلياً لهذا القارئ */
    fun downloadedCount(ctx: Context, reciterId: String): Int =
        dir(ctx, reciterId).listFiles()?.count { it.name.endsWith(".mp3") && it.length() > 1024 } ?: 0

    /** مسار محلي إن وُجد وإلا null — يستخدمه المشغل قبل الرجوع للبث */
    fun localPathIfAny(ctx: Context, reciterId: String, surah: Int): String? =
        if (hasLocal(ctx, reciterId, surah)) localFile(ctx, reciterId, surah).absolutePath else null

    fun isDownloading(reciterId: String): Boolean = downloading == reciterId

    /** بدء تنزيل كامل التلاوة (114 سورة) — يتخطى المكتمل، قابل للإلغاء والاستئناف */
    fun downloadAll(ctx: Context, reciter: Reciter, servers: List<String>) {
        if (downloading != null) return
        val appCtx = ctx.applicationContext
        cancelRequested = false
        downloading = reciter.id
        val done = downloadedCount(appCtx, reciter.id)
        progress[reciter.id] = done
        worker = Thread {
            try {
                for (n in 1..114) {
                    if (cancelRequested) break
                    val f = localFile(appCtx, reciter.id, n)
                    if (f.exists() && f.length() > 1024) {
                        progress[reciter.id] = n
                        continue
                    }
                    val urls = servers.map { if (it.endsWith("/")) it else "$it/" }
                    var ok = false
                    for (base in urls) {
                        val url = base + String.format("%03d", n) + ".mp3"
                        if (downloadOne(url, f)) { ok = true; break }
                        if (cancelRequested) break
                    }
                    if (!ok && cancelRequested) break
                    progress[reciter.id] = n
                }
            } catch (_: Exception) {
            } finally {
                downloading = null
            }
        }.also { it.start() }
    }

    private fun downloadOne(url: String, dest: File): Boolean {
        if (url.isBlank()) return false
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 20000
            conn.readTimeout = 25000
            conn.instanceFollowRedirects = true
            if (conn.responseCode !in 200..299) { conn.disconnect(); return false }
            val tmp = File(dest.parentFile, dest.name + ".part")
            conn.inputStream.use { input ->
                FileOutputStream(tmp).use { out ->
                    val buf = ByteArray(16 * 1024)
                    while (true) {
                        if (cancelRequested) {
                            tmp.delete()
                            conn.disconnect()
                            return false
                        }
                        val read = input.read(buf)
                        if (read < 0) break
                        out.write(buf, 0, read)
                    }
                }
            }
            conn.disconnect()
            if (tmp.length() > 1024) tmp.renameTo(dest) else tmp.delete()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun cancel() {
        cancelRequested = true
    }

    /** حذف التلاوة المحلية لهذا القارئ */
    fun deleteAll(ctx: Context, reciterId: String) {
        if (downloading == reciterId) {
            cancelRequested = true
        }
        dir(ctx, reciterId).deleteRecursively()
        progress.remove(reciterId)
    }
}

/**
 * شريط "تلاوة بدون إنترنت" — تنزيل كامل تلاوة القارئ (114 سورة) إلى الجهاز،
 * مع تقدم مباشر وإمكانية الإلغاء والاستئناف والحذف.
 */
@Composable
fun OfflineReciterRow(
    reciter: Reciter,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val reciterId = reciter.id
    var localCount by remember(reciterId) { mutableIntStateOf(ReciterOffline.downloadedCount(ctx, reciterId)) }

    // تحديث تفاعلي للتقدم أثناء التنزيل
    LaunchedEffect(reciterId) {
        while (true) {
            val p = ReciterOffline.progress[reciterId]
            if (p != null && p != localCount) localCount = p
            delay(300)
        }
    }

    val busy = ReciterOffline.downloading == reciterId
    val anyBusy = ReciterOffline.downloading != null
    val complete = localCount >= 114

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            busy -> {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "جارٍ تنزيل تلاوة ${reciter.name}…",
                        color = Color(0xFFD4AF37),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { localCount / 114f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = Color(0xFFD4AF37),
                        trackColor = Color.White.copy(alpha = 0.10f)
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "$localCount / 114 سورة",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 10.sp
                    )
                }
                TextButton(onClick = onCancel) {
                    Text("إلغاء", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
            complete -> {
                Icon(
                    Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = Color(0xFF6EE7A0),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "✓ التلاوة كاملة متوفرة بدون إنترنت",
                    color = Color(0xFF6EE7A0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onDelete) {
                    Text("حذف", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
            else -> {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (localCount > 0) "منزّل $localCount/114 — استئناف التنزيل؟"
                    else "تنزيل تلاوة ${reciter.name} للعمل بدون إنترنت",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onDownload, enabled = !anyBusy) {
                    Text(
                        if (localCount > 0) "استئناف" else "تنزيل",
                        color = Color(0xFFD4AF37),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
