package com.elhajri.noor.exams

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExamCertificateGenerator {

    /**
     * Renders a 2000x1414 landscape high-res NOOR certificate bitmap.
     */
    fun createCertificateBitmap(
        context: Context,
        userName: String,
        track: ExamTrack,
        difficulty: ExamDifficulty
    ): Bitmap {
        val width = 2000
        val height = 1414
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Dark Green Glass Background Gradient
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                Color.parseColor("#0A1F14"),
                Color.parseColor("#040C07"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Radial warm gold glow in center
        val centerGlow = Paint().apply {
            shader = LinearGradient(
                width / 2f - 400f, height / 2f - 400f, width / 2f + 400f, height / 2f + 400f,
                Color.parseColor("#33D4AF37"),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width / 2f, height / 2f, 700f, centerGlow)

        // 2. Gold Outer & Inner Frame
        val goldPrimary = Color.parseColor("#D4AF37")
        val goldSoft = Color.parseColor("#F3E5AB")
        val darkGreen = Color.parseColor("#0F3820")

        val borderOuterPaint = Paint().apply {
            color = goldPrimary
            style = Paint.Style.STROKE
            strokeWidth = 24f
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(40f, 40f, width - 40f, height - 40f), 24f, 24f, borderOuterPaint)

        val borderInnerPaint = Paint().apply {
            color = goldSoft
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(70f, 70f, width - 70f, height - 70f), 16f, 16f, borderInnerPaint)

        // Corner Ornate Stars
        val starPaint = Paint().apply {
            color = goldPrimary
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val corners = listOf(
            Pair(100f, 100f),
            Pair(width - 100f, 100f),
            Pair(100f, height - 100f),
            Pair(width - 100f, height - 100f)
        )
        for ((cx, cy) in corners) {
            canvas.drawCircle(cx, cy, 20f, starPaint)
            canvas.drawCircle(cx, cy, 32f, borderInnerPaint)
        }

        // 3. Header Text
        val headerPaint = Paint().apply {
            color = goldSoft
            textSize = 52f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText("تطبيق نُور - الاختبار الديني", width / 2f, 200f, headerPaint)

        val titleText = if (track == ExamTrack.KID) "شهادة الجدارة" else "شهادة التمكن"
        val titlePaint = Paint().apply {
            color = goldPrimary
            textSize = 100f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText(titleText, width / 2f, 330f, titlePaint)

        // Decorative Line under title
        val linePaint = Paint().apply {
            color = goldSoft
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawLine(width / 2f - 300f, 370f, width / 2f + 300f, 370f, linePaint)

        // 4. Certification Body Text
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("يَشْهَدُ إِدَارَةُ تَطْبِيقِ نُورٍ بِأَنَّ بَطَلَنَا / المُمَيَّزَ:", width / 2f, 480f, textPaint)

        // User Name in Gold Glass Box
        val nameBoxRect = RectF(width / 2f - 500f, 530f, width / 2f + 500f, 670f)
        val nameBoxPaint = Paint().apply {
            color = darkGreen
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(nameBoxRect, 20f, 20f, nameBoxPaint)
        val nameBoxBorder = Paint().apply {
            color = goldPrimary
            style = Paint.Style.STROKE
            strokeWidth = 8f
            isAntiAlias = true
        }
        canvas.drawRoundRect(nameBoxRect, 20f, 20f, nameBoxBorder)

        val namePaint = Paint().apply {
            color = goldSoft
            textSize = 72f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText(userName, width / 2f, 625f, namePaint)

        // Achievement Text
        val tierAr = difficulty.titleAr
        val trackAr = track.titleAr
        val bodyText = "قَدْ أَتَمَّ بِنَجَاحٍ وَتَفَوُّقٍ جَمِيعَ مُسْتَوَيَاتِ الِاخْتِبَارِ الدِّينِيِّ - المَسَارِ ($trackAr) - المَسْتَوَى ($tierAr)"
        canvas.drawText(bodyText, width / 2f, 770f, textPaint)

        val subText = "وَحَصَلَ عَلَى الاعْتِمَادِ المَعْرِفِيِّ فِي عُلُومِ القُرْآنِ وَالسِّيرَةِ وَالفِقْهِ وَالعَقِيدَةِ وَالأَذْكَارِ"
        val subPaint = Paint().apply {
            color = Color.LTGRAY
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(subText, width / 2f, 850f, subPaint)

        // 5. Islamic Gold Emblem / Seal
        val sealX = width / 2f
        val sealY = 1040f
        canvas.drawCircle(sealX, sealY, 110f, nameBoxPaint)
        val sealBorder = Paint().apply {
            color = goldPrimary
            style = Paint.Style.STROKE
            strokeWidth = 10f
            isAntiAlias = true
        }
        canvas.drawCircle(sealX, sealY, 110f, sealBorder)

        val sealTextPaint = Paint().apply {
            color = goldPrimary
            textSize = 34f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        canvas.drawText("نُور", sealX, sealY - 15f, sealTextPaint)
        canvas.drawText("مُعْتَمَد", sealX, sealY + 30f, sealTextPaint)

        // Date and Serial Number
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val serialCode = "NOOR-EXAM-" + track.id.uppercase() + "-" + difficulty.id.uppercase() + "-" + (userName.hashCode() and 0xFFFF)

        val metaPaint = Paint().apply {
            color = goldSoft
            textSize = 36f
            isAntiAlias = true
        }
        metaPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("تاريخ الإصدار: $currentDate", width - 150f, 1100f, metaPaint)

        metaPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("الرقم التسلسلي: $serialCode", 150f, 1100f, metaPaint)

        // Footer Branding
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("تطبيق نُور للإسلام والقرآن الكريم - www.noor.app", width / 2f, 1280f, footerPaint)

        return bitmap
    }

    /**
     * Save Certificate Bitmap to device gallery or MediaStore.
     */
    fun saveCertificateToDevice(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.png")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NOOR_Certificates")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    val stream = resolver.openOutputStream(uri)
                    if (stream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        stream.close()
                    }
                }
                uri
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val noorDir = File(imagesDir, "NOOR_Certificates")
                if (!noorDir.exists()) noorDir.mkdirs()
                val imageFile = File(noorDir, "$fileName.png")
                val stream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()
                Uri.fromFile(imageFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Export certificate bitmap as printable PDF Document using android.graphics.pdf.PdfDocument
     */
    fun exportCertificatePdf(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        return try {
            val pdfDoc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(2000, 1414, 1).create()
            val page = pdfDoc.startPage(pageInfo)

            val canvas = page.canvas
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDoc.finishPage(page)

            val pdfDir = File(context.cacheDir, "certificates")
            if (!pdfDir.exists()) pdfDir.mkdirs()
            val pdfFile = File(pdfDir, "$fileName.pdf")

            val outputStream = FileOutputStream(pdfFile)
            pdfDoc.writeTo(outputStream)
            outputStream.close()
            pdfDoc.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Share certificate image or PDF.
     */
    fun shareUri(context: Context, uri: Uri, mimeType: String, title: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    }
}
