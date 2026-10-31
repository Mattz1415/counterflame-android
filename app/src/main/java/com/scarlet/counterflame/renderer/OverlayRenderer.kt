package com.scarlet.counterflame.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.scarlet.counterflame.settings.AppSettings
import com.scarlet.counterflame.simulation.AimAssistState
import com.scarlet.counterflame.simulation.SimulatedEntity
import com.scarlet.counterflame.simulation.Team
import java.util.Locale

class OverlayRenderer(private val config: RenderConfig = RenderConfig()) {

    private val tempRect = RectF()

    fun render(
        canvas: Canvas,
        entities: List<SimulatedEntity>,
        settings: AppSettings,
        aimState: AimAssistState,
        fps: Float,
        nativeEngineVersion: String
    ) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        val cx = width * 0.5f
        val cy = height * 0.5f

        // Draw Simulated Environment Background (Grid / Floor / Horizon)
        drawEnvironment(canvas, width, height)

        // Draw FOV Circle
        if (settings.aimAssistEnabled && settings.showFovCircle) {
            drawFovCircle(canvas, cx, cy, settings.aimFovRadius, aimState.isLocked)
        }

        // Draw ESP Elements
        if (settings.espEnabled) {
            val alphaInt = (settings.overlayOpacity * 255).toInt().coerceIn(20, 255)

            for (entity in entities) {
                if (!entity.isVisibleOnScreen) continue

                val teamColor = if (settings.teamColorsEnabled) {
                    if (entity.team == Team.TERRORIST) config.colorT else config.colorCT
                } else {
                    config.colorNeutral
                }

                val left = entity.screenX - (entity.screenWidth * 0.5f)
                val top = entity.screenY - (entity.screenHeight * 0.5f)
                val right = entity.screenX + (entity.screenWidth * 0.5f)
                val bottom = entity.screenY + (entity.screenHeight * 0.5f)

                // Snaplines
                if (settings.showSnaplines) {
                    config.snaplinePaint.color = teamColor
                    config.snaplinePaint.alpha = (alphaInt * 0.4f).toInt()
                    canvas.drawLine(cx, height, entity.screenX, bottom, config.snaplinePaint)
                }

                // 2D Bounding Box
                if (settings.showBoundingBoxes) {
                    config.boxPaint.color = teamColor
                    config.boxPaint.alpha = alphaInt

                    if (settings.boxStyleCorners) {
                        drawCornerBox(canvas, left, top, right, bottom, config.boxPaint)
                    } else {
                        canvas.drawRect(left, top, right, bottom, config.boxPaint)
                    }
                }

                // Health Bar (Left side of bounding box)
                if (settings.showHealthBar) {
                    drawHealthBar(canvas, left, top, bottom, entity.health, entity.maxHealth, alphaInt)
                }

                // Top Labels: Player Name & Team tag
                if (settings.showPlayerNames) {
                    val teamTag = if (entity.team == Team.TERRORIST) "[T]" else "[CT]"
                    val labelText = "$teamTag ${entity.name}"
                    drawTopBadge(canvas, entity.screenX, top - 8f, labelText, teamColor, alphaInt)
                }

                // Bottom Labels: Distance & Weapon
                if (settings.showDistance) {
                    val distText = String.format(Locale.US, "%.1fm | %s", entity.distance, entity.weapon)
                    drawBottomBadge(canvas, entity.screenX, bottom + 24f, distText, alphaInt)
                }
            }
        }

        // Draw Crosshair & Aim Status
        drawCrosshair(canvas, cx, cy, aimState)

        // Draw HUD Diagnostic Overlay
        drawDiagnosticHud(canvas, fps, entities.count { it.isVisibleOnScreen }, entities.size, nativeEngineVersion, aimState)
    }

    private fun drawEnvironment(canvas: Canvas, width: Float, height: Float) {
        // Subtle dark tactical backdrop grid
        val gridPaint = Paint().apply {
            color = Color.parseColor("#15222E")
            strokeWidth = 1f
        }
        val horizonY = height * 0.5f

        // Draw horizon
        gridPaint.color = Color.parseColor("#1C2C3D")
        canvas.drawLine(0f, horizonY, width, horizonY, gridPaint)

        // Perspective grid lines
        gridPaint.color = Color.parseColor("#0F1A24")
        val numLines = 14
        for (i in 0..numLines) {
            val startX = (width / numLines) * i
            canvas.drawLine(width * 0.5f, horizonY, startX, height, gridPaint)
        }
    }

    private fun drawCornerBox(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, paint: Paint) {
        val w = r - l
        val h = b - t
        val cornerLen = (w * 0.25f).coerceAtLeast(8f).coerceAtMost(h * 0.25f)

        // Top-Left
        canvas.drawLine(l, t, l + cornerLen, t, paint)
        canvas.drawLine(l, t, l, t + cornerLen, paint)

        // Top-Right
        canvas.drawLine(r, t, r - cornerLen, t, paint)
        canvas.drawLine(r, t, r, t + cornerLen, paint)

        // Bottom-Left
        canvas.drawLine(l, b, l + cornerLen, b, paint)
        canvas.drawLine(l, b, l, b - cornerLen, paint)

        // Bottom-Right
        canvas.drawLine(r, b, r - cornerLen, b, paint)
        canvas.drawLine(r, b, r, b - cornerLen, paint)
    }

    private fun drawHealthBar(
        canvas: Canvas,
        boxLeft: Float,
        boxTop: Float,
        boxBottom: Float,
        health: Float,
        maxHealth: Float,
        alphaInt: Int
    ) {
        val barWidth = 4.5f
        val padding = 4f
        val barLeft = boxLeft - padding - barWidth
        val barRight = boxLeft - padding
        val totalHeight = boxBottom - boxTop

        // Background track
        tempRect.set(barLeft, boxTop, barRight, boxBottom)
        config.healthBarBgPaint.alpha = alphaInt
        canvas.drawRect(tempRect, config.healthBarBgPaint)

        // Fill based on HP percentage
        val hpPercent = (health / maxHealth).coerceIn(0f, 1f)
        val fillTop = boxBottom - (totalHeight * hpPercent)

        val healthColor = when {
            hpPercent > 0.6f -> config.colorHealthGreen
            hpPercent > 0.3f -> config.colorHealthYellow
            else -> config.colorHealthRed
        }

        config.healthBarFillPaint.color = healthColor
        config.healthBarFillPaint.alpha = alphaInt
        tempRect.set(barLeft, fillTop, barRight, boxBottom)
        canvas.drawRect(tempRect, config.healthBarFillPaint)
    }

    private fun drawTopBadge(
        canvas: Canvas,
        centerX: Float,
        baselineY: Float,
        text: String,
        badgeColor: Int,
        alphaInt: Int
    ) {
        config.textPaint.textSize = 24f
        val textWidth = config.textPaint.measureText(text)
        val halfW = textWidth * 0.5f
        val padH = 6f
        val padV = 3f

        tempRect.set(centerX - halfW - padH, baselineY - 22f - padV, centerX + halfW + padH, baselineY + padV)
        config.textBadgeBgPaint.alpha = (alphaInt * 0.75f).toInt()
        canvas.drawRoundRect(tempRect, 6f, 6f, config.textBadgeBgPaint)

        config.textPaint.color = badgeColor
        config.textPaint.alpha = alphaInt
        canvas.drawText(text, centerX - halfW, baselineY - 4f, config.textPaint)
    }

    private fun drawBottomBadge(
        canvas: Canvas,
        centerX: Float,
        baselineY: Float,
        text: String,
        alphaInt: Int
    ) {
        config.textPaint.textSize = 21f
        val textWidth = config.textPaint.measureText(text)
        val halfW = textWidth * 0.5f

        tempRect.set(centerX - halfW - 6f, baselineY - 18f, centerX + halfW + 6f, baselineY + 4f)
        config.textBadgeBgPaint.alpha = (alphaInt * 0.65f).toInt()
        canvas.drawRoundRect(tempRect, 4f, 4f, config.textBadgeBgPaint)

        config.textPaint.color = Color.WHITE
        config.textPaint.alpha = (alphaInt * 0.9f).toInt()
        canvas.drawText(text, centerX - halfW, baselineY - 3f, config.textPaint)
    }

    private fun drawFovCircle(canvas: Canvas, cx: Float, cy: Float, radius: Float, isLocked: Boolean) {
        config.fovPaint.color = if (isLocked) config.colorFovLocked else config.colorFovCircle
        canvas.drawCircle(cx, cy, radius, config.fovPaint)
    }

    private fun drawCrosshair(canvas: Canvas, cx: Float, cy: Float, aimState: AimAssistState) {
        val crossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 2.2f
            color = if (aimState.isLocked) config.colorCrosshairLocked else config.colorCrosshair
            style = Paint.Style.STROKE
        }

        val len = 12f
        val gap = 4f

        // Crosshair reticle
        canvas.drawLine(cx - len - gap, cy, cx - gap, cy, crossPaint)
        canvas.drawLine(cx + gap, cy, cx + len + gap, cy, crossPaint)
        canvas.drawLine(cx, cy - len - gap, cx, cy - gap, crossPaint)
        canvas.drawLine(cx, cy + gap, cx, cy + len + gap, crossPaint)
        canvas.drawCircle(cx, cy, 1.5f, crossPaint)
    }

    private fun drawDiagnosticHud(
        canvas: Canvas,
        fps: Float,
        visibleCount: Int,
        totalCount: Int,
        nativeEngineVersion: String,
        aimState: AimAssistState
    ) {
        val hud = config.hudPaint
        var y = 48f
        val x = 32f

        canvas.drawText("COUNTERFLAME COMPANION PROTOTYPE [SIMULATION]", x, y, hud)
        y += 32f

        val fpsColor = if (fps >= 55f) Color.GREEN else if (fps >= 30f) Color.YELLOW else Color.RED
        hud.color = fpsColor
        canvas.drawText(String.format(Locale.US, "TARGET: 60 FPS | CURRENT: %.1f FPS", fps), x, y, hud)
        y += 32f

        hud.color = Color.parseColor("#B0FFFFFF")
        canvas.drawText("ENTITIES: $visibleCount/$totalCount VISIBLE | ENGINE: $nativeEngineVersion", x, y, hud)
        y += 32f

        if (aimState.isLocked) {
            hud.color = Color.parseColor("#FF5252")
            canvas.drawText(String.format(Locale.US, "AIM-ASSIST ACTIVE: LOCKED ON [%s] (dx=%.1f, dy=%.1f)", aimState.targetName, aimState.deltaX, aimState.deltaY), x, y, hud)
        } else {
            hud.color = Color.parseColor("#80FFFFFF")
            canvas.drawText("AIM-ASSIST SIMULATOR: SCANNING FOV CIRCLE", x, y, hud)
        }
    }
}
