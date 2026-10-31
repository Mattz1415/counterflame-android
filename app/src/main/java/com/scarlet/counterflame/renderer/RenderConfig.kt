package com.scarlet.counterflame.renderer

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

class RenderConfig {
    val colorT = Color.parseColor("#FF6D00") // Terrorist vibrant orange
    val colorCT = Color.parseColor("#00B0FF") // CT vibrant cyan
    val colorNeutral = Color.parseColor("#E0E0E0")
    val colorHealthGreen = Color.parseColor("#00E676")
    val colorHealthYellow = Color.parseColor("#FFD600")
    val colorHealthRed = Color.parseColor("#FF1744")
    val colorFovCircle = Color.parseColor("#40FFFFFF")
    val colorFovLocked = Color.parseColor("#7000E676")
    val colorCrosshair = Color.parseColor("#00E676")
    val colorCrosshairLocked = Color.parseColor("#FF1744")

    val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    val boxFilledPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        color = Color.WHITE
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }

    val textBadgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#99000000")
    }

    val healthBarBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#B0101010")
    }

    val healthBarFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    val fovPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.8f
    }

    val snaplinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.2f
    }

    val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 26f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        color = Color.parseColor("#B0FFFFFF")
        setShadowLayer(2f, 1f, 1f, Color.BLACK)
    }
}
