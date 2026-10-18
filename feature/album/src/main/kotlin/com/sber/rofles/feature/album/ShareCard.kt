package com.sber.rofles.feature.album

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import androidx.core.graphics.ColorUtils
import java.io.File

internal data class ShareContent(
    val lead: String,
    val title: String,
    val brand: String,
    val colorA: Int,
    val colorB: Int,
    val art: Int,
    val fileName: String,
)

internal object ShareCard {

    private const val SIZE = 1080
    private const val PADDING = 96f
    private const val TEXT_COLOR = 0xFF000000.toInt()
    private const val SECONDARY_TEXT_COLOR = 0xFF7D868C.toInt()
    private const val BRAND_COLOR = 0xFF19D13B.toInt()
    private const val TITLE_SIZE = 104f
    private const val TITLE_SIZE_SMALL = 84f

    fun createFile(context: Context, content: ShareContent): File {
        val directory = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(directory, content.fileName)
        val bitmap = render(context, content)
        try {
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } finally {
            bitmap.recycle()
        }
        return file
    }

    fun launch(context: Context, file: File, chooserTitle: String) {
        val uri: Uri = FileProvider.getUriForFile(context, context.packageName + ".prank.share", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, chooserTitle).apply {
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun render(context: Context, content: ShareContent): Bitmap {
        val bitmap = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val size = SIZE.toFloat()
        val softA = ColorUtils.blendARGB(content.colorA, 0xFFFFFFFF.toInt(), 0.7f)
        val softB = ColorUtils.blendARGB(content.colorB, 0xFFFFFFFF.toInt(), 0.7f)

        canvas.drawRect(
            0f, 0f, size, size,
            Paint().apply { shader = LinearGradient(0f, 0f, size, size, softA, softB, Shader.TileMode.CLAMP) },
        )
        drawGlow(canvas, size * 0.2f, size * 0.18f, size * 0.5f, content.colorA)
        drawGlow(canvas, size * 0.85f, size * 0.85f, size * 0.55f, content.colorB)

        val card = RectF(PADDING / 2, PADDING / 2, size - PADDING / 2, size - PADDING / 2)
        canvas.drawRoundRect(card, 72f, 72f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xF2FFFFFF.toInt() })

        val leadPaint = textPaint(52f, Typeface.NORMAL, SECONDARY_TEXT_COLOR)
        val brandPaint = textPaint(48f, Typeface.BOLD, BRAND_COLOR)
        val width = (size - PADDING * 2).toInt()

        val brand = layout(content.brand, brandPaint, width)
        val brandTop = size - PADDING - brand.height
        canvas.drawLayout(brand, PADDING, brandTop)

        val titleText = content.title.replace("-", "-\u200B")
        val title = layout(titleText, textPaint(TITLE_SIZE, Typeface.BOLD, TEXT_COLOR), width)
            .takeIf { it.lineCount == 1 }
            ?: layout(titleText, textPaint(TITLE_SIZE_SMALL, Typeface.BOLD, TEXT_COLOR), width)
        val titleTop = brandTop - 32f - title.height
        canvas.drawLayout(title, PADDING, titleTop)

        val lead = layout(content.lead, leadPaint, width)
        val leadTop = titleTop - 8f - lead.height
        canvas.drawLayout(lead, PADDING, leadTop)

        BitmapFactory.decodeResource(context.resources, content.art)?.let { art ->
            val top = PADDING * 1.1f
            val artSize = minOf(size * 0.5f, leadTop - 24f - top)
            val left = (size - artSize) / 2f
            canvas.drawBitmap(art, null, RectF(left, top, left + artSize, top + artSize), Paint(Paint.FILTER_BITMAP_FLAG))
            art.recycle()
        }
        return bitmap
    }

    private fun drawGlow(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int) {
        val transparent = ColorUtils.setAlphaComponent(color, 0)
        val middle = ColorUtils.setAlphaComponent(color, 140)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(x, y, radius, intArrayOf(middle, transparent), null, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(x, y, radius, paint)
    }

    private fun textPaint(textSize: Float, style: Int, color: Int): TextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            this.color = color
            typeface = Typeface.create(Typeface.DEFAULT, style)
        }

    private fun layout(text: String, paint: TextPaint, width: Int, maxLines: Int = 2): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setIncludePad(false)
            .build()

    private fun Canvas.drawLayout(layout: StaticLayout, x: Float, y: Float) {
        save()
        translate(x, y)
        layout.draw(this)
        restore()
    }
}
