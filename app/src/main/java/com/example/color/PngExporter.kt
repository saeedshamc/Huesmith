package com.example.color

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PngExporter {

    suspend fun generatePaletteCardPng(
        context: Context,
        paletteTitle: String,
        domainTitle: String,
        colors: List<HslColor>
    ): Uri? = withContext(Dispatchers.IO) {
        val width = 1200
        val height = 675
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#121318")
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Header Title
        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("HUESMITH CRAFTSMAN PALETTE", 60f, 90f, titlePaint)

        // Subtitle / Domain Badge
        val subPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#90CAF9")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("$paletteTitle • $domainTitle Profile", 60f, 135f, subPaint)

        // Draw Swatches
        val count = colors.size.coerceAtLeast(1)
        val startX = 60f
        val startY = 180f
        val totalAvailableWidth = width - 120f
        val spacing = 16f
        val swatchWidth = (totalAvailableWidth - (spacing * (count - 1))) / count
        val swatchHeight = 320f

        val swatchPaint = Paint().apply { isAntiAlias = true }
        val textPaintHex = Paint().apply {
            isAntiAlias = true
            textSize = 22f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val textPaintLabel = Paint().apply {
            isAntiAlias = true
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val badgePaint = Paint().apply { isAntiAlias = true }

        colors.forEachIndexed { i, hsl ->
            val left = startX + i * (swatchWidth + spacing)
            val rect = RectF(left, startY, left + swatchWidth, startY + swatchHeight)

            val rgb = hsl.toRgb()
            val argb = Color.rgb(rgb.r, rgb.g, rgb.b)
            swatchPaint.color = argb

            // Draw rounded rect swatch
            canvas.drawRoundRect(rect, 20f, 20f, swatchPaint)

            // Calculate WCAG contrast against white vs black text
            val contrastWhite = WcagUtils.contrastRatio(rgb, RgbColor(255, 255, 255))
            val isLight = contrastWhite < 4.5f
            val contentColor = if (isLight) Color.BLACK else Color.WHITE

            textPaintHex.color = contentColor
            textPaintLabel.color = if (isLight) Color.argb(180, 0, 0, 0) else Color.argb(200, 255, 255, 255)

            // Draw HEX
            val centerX = left + (swatchWidth / 2f)
            canvas.drawText(hsl.toHex(), centerX, startY + (swatchHeight / 2f) - 10f, textPaintHex)

            // Draw HSL values
            val hslText = "H:${hsl.hue.toInt()}° S:${(hsl.saturation * 100).toInt()}% L:${(hsl.lightness * 100).toInt()}%"
            canvas.drawText(hslText, centerX, startY + (swatchHeight / 2f) + 24f, textPaintLabel)

            // Draw WCAG Badge
            val wcagBadgeText = if (contrastWhite >= 7.0f) "AAA ✓" else if (contrastWhite >= 4.5f) "AA ✓" else "AA ⚡"
            badgePaint.color = if (isLight) Color.argb(40, 0, 0, 0) else Color.argb(50, 255, 255, 255)
            val badgeRect = RectF(centerX - 45f, startY + swatchHeight - 45f, centerX + 45f, startY + swatchHeight - 15f)
            canvas.drawRoundRect(badgeRect, 10f, 10f, badgePaint)

            canvas.drawText(wcagBadgeText, centerX, startY + swatchHeight - 24f, textPaintHex.apply { textSize = 15f })
            textPaintHex.textSize = 22f // reset
        }

        // Draw Gradient Strip
        val gradientY = 530f
        val gradientHeight = 40f
        val gradientColors = PaletteGenerator.generate5StepGradient(colors.firstOrNull() ?: HslColor(200f, 0.8f, 0.5f), colors.lastOrNull() ?: HslColor(200f, 0.8f, 0.5f))
            .map { Color.rgb(it.toRgb().r, it.toRgb().g, it.toRgb().b) }
            .toIntArray()

        val shader = LinearGradient(
            startX, gradientY, width - 60f, gradientY,
            gradientColors, null, Shader.TileMode.CLAMP
        )
        val gradPaint = Paint().apply {
            isAntiAlias = true
            this.shader = shader
        }
        val gradRect = RectF(startX, gradientY, width - 60f, gradientY + gradientHeight)
        canvas.drawRoundRect(gradRect, 12f, 12f, gradPaint)

        // Draw Footer Branding
        val footerPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#78909C")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("HUESMITH • 100% Offline Domain Color Craftsman", startX, 620f, footerPaint)

        val datePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#546E7A")
            textSize = 16f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("Generated on-device without network calls", width - 60f, 620f, datePaint)

        // Save Bitmap to Cache Directory
        val imagesFolder = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(imagesFolder, "huesmith_palette_export.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun sharePngImage(context: Context, uri: Uri, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "$title\nForged with Huesmith — 100% Offline Domain Color Craftsman")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Palette Image Card"))
    }
}
