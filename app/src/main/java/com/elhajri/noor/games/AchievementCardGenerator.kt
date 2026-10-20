package com.elhajri.noor.games

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.elhajri.noor.profile.UserProfileStore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AchievementCardGenerator: Generates high-resolution branded achievement cards
 * (dark green + gold glass design with NOOR branding, stars, game name, user rank)
 * and shares them to WhatsApp / Socials via ShareCompat + MediaStore.
 */
object AchievementCardGenerator {

    /**
     * Generates a 1080x1350 high-res Bitmap card for game victory.
     */
    fun createAchievementBitmap(
        context: Context,
        gameTitle: String,
        levelNumber: Int,
        starsEarned: Int,
        scoreGained: Int,
        xpGained: Int
    ): Bitmap {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Deep Night Green Background with Radial Glow
        val bgPaint = Paint().apply { isAntiAlias = true }
        val bgGradient = LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            intArrayOf(Color.parseColor("#091A14"), Color.parseColor("#0E2A1E"), Color.parseColor("#05110D")),
            null, Shader.TileMode.CLAMP
        )
        bgPaint.shader = bgGradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Center Gold Glow
        val glowPaint = Paint().apply { isAntiAlias = true }
        val glowShader = RadialGradient(
            width / 2f, height / 2f, 500f,
            Color.parseColor("#26D4AF37"), Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        glowPaint.shader = glowShader
        canvas.drawCircle(width / 2f, height / 2f, 500f, glowPaint)

        // 2. Gold Glass Border Frame
        val framePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 12f
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(Color.parseColor("#FFD700"), Color.parseColor("#50C878"), Color.parseColor("#D4AF37")),
                null, Shader.TileMode.CLAMP
            )
        }
        val rectMargin = 40f
        canvas.drawRoundRect(
            rectMargin, rectMargin, width - rectMargin, height - rectMargin,
            36f, 36f, framePaint
        )

        // Inner subtle border
        framePaint.strokeWidth = 3f
        framePaint.alpha = 120
        canvas.drawRoundRect(
            rectMargin + 20f, rectMargin + 20f, width - rectMargin - 20f, height - rectMargin - 20f,
            28f, 28f, framePaint
        )

        // 3. NOOR Header Branding
        val textPaint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        // App Emblem / Moon Symbol
        textPaint.color = Color.parseColor("#FFD700")
        textPaint.textSize = 64f
        canvas.drawText("🌙 تطبيق نُور", width / 2f, 150f, textPaint)

        textPaint.textSize = 32f
        textPaint.color = Color.parseColor("#D4E6D8")
        canvas.drawText("وسام الإنجاز والتميز الإيماني", width / 2f, 205f, textPaint)

        // Divider Line
        val linePaint = Paint().apply {
            isAntiAlias = true
            strokeWidth = 4f
            color = Color.parseColor("#66D4AF37")
        }
        canvas.drawLine(width / 4f, 250f, (width * 3 / 4f), 250f, linePaint)

        // 4. Golden Stars Burst
        val starPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#FFD700")
            textSize = 90f
            textAlign = Paint.Align.CENTER
        }

        val starsText = when (starsEarned.coerceIn(1, 3)) {
            1 -> "⭐"
            2 -> "⭐ ⭐"
            else -> "⭐ ⭐ ⭐"
        }
        canvas.drawText(starsText, width / 2f, 380f, starPaint)

        // Victory Title
        textPaint.textSize = 54f
        textPaint.color = Color.parseColor("#FFD700")
        canvas.drawText("إنجاز جديد مبارك! 🎉", width / 2f, 470f, textPaint)

        // 5. Game Name & Level Box
        val boxPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#1AFFFFFF")
            style = Paint.Style.FILL
        }
        val boxStroke = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#40D4AF37")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        val boxRect = RectF(120f, 530f, width - 120f, 700f)
        canvas.drawRoundRect(boxRect, 24f, 24f, boxPaint)
        canvas.drawRoundRect(boxRect, 24f, 24f, boxStroke)

        textPaint.textSize = 42f
        textPaint.color = Color.WHITE
        canvas.drawText(gameTitle, width / 2f, 600f, textPaint)

        textPaint.textSize = 34f
        textPaint.color = Color.parseColor("#50C878")
        val levelText = if (levelNumber > 0) "المرحلة رقم ${toArDigits(levelNumber)} — اكتملت بنجاح" else "مرحلة جديدة مكتملة"
        canvas.drawText(levelText, width / 2f, 660f, textPaint)

        // 6. User Stats Container
        val statsBox = RectF(100f, 740f, width - 100f, 1020f)
        canvas.drawRoundRect(statsBox, 28f, 28f, boxPaint)
        canvas.drawRoundRect(statsBox, 28f, 28f, boxStroke)

        val userName = UserProfileStore.getUserName(context)
        textPaint.textSize = 36f
        textPaint.color = Color.parseColor("#FFD700")
        canvas.drawText("البطل: $userName", width / 2f, 810f, textPaint)

        textPaint.textSize = 32f
        textPaint.color = Color.parseColor("#E0E0E0")
        canvas.drawText("النجوم المكتسبة: +${toArDigits(starsEarned)} 🌟", width / 2f, 880f, textPaint)
        canvas.drawText("نقاط الخبرة: +${toArDigits(xpGained)} XP  |  النقاط: +${toArDigits(scoreGained)}", width / 2f, 950f, textPaint)

        // 7. Quran Quote Footer
        textPaint.textSize = 30f
        textPaint.color = Color.parseColor("#D4AF37")
        canvas.drawText("﴿وَفِي ذَٰلِكَ فَلْيَتَنَافَسِ الْمُتَنَافِسُونَ﴾", width / 2f, 1120f, textPaint)

        textPaint.textSize = 26f
        textPaint.color = Color.parseColor("#80FFFFFF")
        val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale("ar")).format(Date())
        canvas.drawText("حُفِظ في تطبيق نور  •  $dateStr", width / 2f, 1220f, textPaint)

        return bitmap
    }

    private fun toArDigits(value: Any): String {
        val map = mapOf('0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤', '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩')
        return value.toString().map { map[it] ?: it }.joinToString("")
    }

    /**
     * Saves card to MediaStore / File cache and triggers 1-tap WhatsApp share.
     */
    fun shareAchievementCard(
        context: Context,
        gameTitle: String,
        levelNumber: Int,
        starsEarned: Int,
        scoreGained: Int,
        xpGained: Int
    ) {
        try {
            val bitmap = createAchievementBitmap(context, gameTitle, levelNumber, starsEarned, scoreGained, xpGained)
            val imageUri = saveBitmapToUri(context, bitmap, gameTitle)

            if (imageUri != null) {
                val shareText = "حققت إنجازاً جديداً في لعبة \"$gameTitle\" بتطبيق نور الإيماني! 🌟\nحمل التطبيق واستمتع بألعاب الذكر والقرآن 🌙"

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // Try targeting WhatsApp directly
                val whatsappIntent = Intent(intent).apply {
                    setPackage("com.whatsapp")
                }

                if (whatsappIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(whatsappIntent)
                } else {
                    val chooser = Intent.createChooser(intent, "مشاركة وسام الإنجاز")
                    context.startActivity(chooser)
                }
            } else {
                Toast.makeText(context, "تعذر إنشاء بطاقة الإنجاز", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "خطأ في المشاركة: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToUri(context: Context, bitmap: Bitmap, title: String): Uri? {
        val filename = "NOOR_Achievement_${System.currentTimeMillis()}.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NOOR_Achievements")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                return uri
            }
        }

        // Fallback to cache directory FileProvider
        return try {
            val imagesFolder = File(context.cacheDir, "images")
            imagesFolder.mkdirs()
            val file = File(imagesFolder, filename)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) {
            null
        }
    }
}
