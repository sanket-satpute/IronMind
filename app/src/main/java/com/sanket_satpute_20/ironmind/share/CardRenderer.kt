package com.sanket_satpute_20.ironmind.share

import android.content.Context
import android.graphics.*
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

object CardRenderer {

    fun renderStreakCard(
        context: Context,
        milestoneData: MilestoneData,
        userName: String
    ): File {
        // Instagram story size — 1080x1920
        val width  = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val primaryColor   = milestoneData.tier.primaryColor.toInt()
        val secondaryColor = milestoneData.tier.secondaryColor.toInt()

        drawBackground(canvas, width, height)
        drawGradientBorder(canvas, width, height, primaryColor, secondaryColor)
        drawTopDecoration(canvas, width, primaryColor)
        drawStreakNumber(canvas, width, height, milestoneData, primaryColor)
        drawMilestoneTitle(canvas, width, height, milestoneData)
        drawUserName(canvas, width, height, userName)
        drawSubtitle(canvas, width, height, milestoneData)
        drawBottomBrand(canvas, width, height, primaryColor)
        drawCornerAccents(canvas, width, height, primaryColor)
        drawDate(canvas, width, height)

        return saveBitmap(context, bitmap, milestoneData.streakDays)
    }

    private fun drawBackground(canvas: Canvas, width: Int, height: Int) {
        // Deep black background with subtle gradient
        val paint = Paint()
        val gradient = RadialGradient(
            width / 2f, height / 3f,
            height * 0.8f,
            intArrayOf(Color.parseColor("#0A0A0A"), Color.BLACK),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun drawGradientBorder(
        canvas: Canvas, width: Int, height: Int,
        primaryColor: Int, secondaryColor: Int
    ) {
        val borderWidth = 6f
        val cornerRadius = 0f
        val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(primaryColor, secondaryColor, primaryColor),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            isAntiAlias = true
        }
        canvas.drawRect(
            borderWidth / 2, borderWidth / 2,
            width - borderWidth / 2, height - borderWidth / 2,
            paint
        )
    }

    private fun drawTopDecoration(canvas: Canvas, width: Int, primaryColor: Int) {
        // Horizontal accent line at top
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), 0f,
                intArrayOf(Color.TRANSPARENT, primaryColor, Color.TRANSPARENT),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(60f, 280f, width - 60f, 280f, paint)

        // "IRONMIND" text at top
        val brandPaint = Paint().apply {
            color = primaryColor
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.3f
        }
        canvas.drawText("⚡ IRONMIND", width / 2f, 220f, brandPaint)
    }

    private fun drawStreakNumber(
        canvas: Canvas, width: Int, height: Int,
        data: MilestoneData, primaryColor: Int
    ) {
        // Giant streak number — centrepiece
        val numberPaint = Paint().apply {
            shader = LinearGradient(
                0f, height * 0.3f, 0f, height * 0.6f,
                intArrayOf(Color.WHITE, primaryColor),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            textSize = 420f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            "${data.streakDays}",
            width / 2f,
            height * 0.52f,
            numberPaint
        )

        // "DAYS" label below the number
        val daysPaint = Paint().apply {
            color = Color.WHITE
            alpha = 180
            textSize = 72f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.5f
        }
        canvas.drawText("DAY STREAK", width / 2f, height * 0.59f, daysPaint)

        // Tier badge
        val badgePaint = Paint().apply {
            color = primaryColor
            alpha = 40
            isAntiAlias = true
        }
        val badgeRect = RectF(
            width / 2f - 200f,
            height * 0.61f,
            width / 2f + 200f,
            height * 0.655f
        )
        canvas.drawRoundRect(badgeRect, 40f, 40f, badgePaint)

        val badgeTextPaint = Paint().apply {
            color = primaryColor
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.2f
        }
        canvas.drawText(
            "⚡ ${data.tier.label.uppercase()}",
            width / 2f,
            height * 0.646f,
            badgeTextPaint
        )
    }

    private fun drawMilestoneTitle(
        canvas: Canvas, width: Int, height: Int, data: MilestoneData
    ) {
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 88f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.15f
        }
        canvas.drawText(data.title, width / 2f, height * 0.32f, paint)
    }

    private fun drawUserName(
        canvas: Canvas, width: Int, height: Int, userName: String
    ) {
        val paint = Paint().apply {
            color = Color.WHITE
            alpha = 200
            textSize = 58f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(userName, width / 2f, height * 0.72f, paint)
    }

    private fun drawSubtitle(
        canvas: Canvas, width: Int, height: Int, data: MilestoneData
    ) {
        // Draw subtitle — may need to wrap text manually
        val paint = Paint().apply {
            color = Color.WHITE
            alpha = 140
            textSize = 46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val words = data.subtitle.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        val maxWidth = width - 200f

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) < maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)

        val lineHeight = 60f
        val totalTextHeight = lines.size * lineHeight
        val startY = height * 0.78f - totalTextHeight / 2

        lines.forEachIndexed { index, line ->
            canvas.drawText(line, width / 2f, startY + index * lineHeight, paint)
        }
    }

    private fun drawBottomBrand(
        canvas: Canvas, width: Int, height: Int, primaryColor: Int
    ) {
        // Divider line
        val linePaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), 0f,
                intArrayOf(Color.TRANSPARENT, primaryColor, Color.TRANSPARENT),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(60f, height * 0.86f, width - 60f, height * 0.86f, linePaint)

        val taglinePaint = Paint().apply {
            color = Color.WHITE
            alpha = 100
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.1f
        }
        canvas.drawText("No excuses. Just execution.", width / 2f, height * 0.90f, taglinePaint)
    }

    private fun drawCornerAccents(
        canvas: Canvas, width: Int, height: Int, primaryColor: Int
    ) {
        val paint = Paint().apply {
            color = primaryColor
            alpha = 60
            strokeWidth = 4f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val size = 80f
        val margin = 40f

        // Top-left
        canvas.drawLine(margin, margin + size, margin, margin, paint)
        canvas.drawLine(margin, margin, margin + size, margin, paint)

        // Top-right
        canvas.drawLine(width - margin - size, margin, width - margin, margin, paint)
        canvas.drawLine(width - margin, margin, width - margin, margin + size, paint)

        // Bottom-left
        canvas.drawLine(margin, height - margin - size, margin, height - margin, paint)
        canvas.drawLine(margin, height - margin, margin + size, height - margin, paint)

        // Bottom-right
        canvas.drawLine(width - margin - size, height - margin,
            width - margin, height - margin, paint)
        canvas.drawLine(width - margin, height - margin,
            width - margin, height - margin - size, paint)
    }

    private fun drawDate(canvas: Canvas, width: Int, height: Int) {
        val datePaint = Paint().apply {
            color = Color.WHITE
            alpha = 80
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            LocalDate.now().toString(),
            width / 2f,
            height * 0.95f,
            datePaint
        )
    }

    private fun saveBitmap(context: Context, bitmap: Bitmap, streakDays: Int): File {
        val filename = "ironmind_streak_${streakDays}_days.png"
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            filename
        )
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
}