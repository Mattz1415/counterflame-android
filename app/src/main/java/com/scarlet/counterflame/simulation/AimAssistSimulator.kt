package com.scarlet.counterflame.simulation

import com.scarlet.counterflame.native.NativeAimMath
import com.scarlet.counterflame.settings.AppSettings

data class AimAssistState(
    var isLocked: Boolean = false,
    var targetId: Int = -1,
    var targetName: String = "",
    var deltaX: Float = 0f,
    var deltaY: Float = 0f
)

class AimAssistSimulator {
    val state = AimAssistState()

    fun update(
        entities: List<SimulatedEntity>,
        crosshairX: Float,
        crosshairY: Float,
        settings: AppSettings,
        worldSim: WorldSimulation
    ): Pair<Float, Float> {
        if (!settings.aimAssistEnabled || entities.isEmpty()) {
            state.isLocked = false
            state.targetId = -1
            state.targetName = ""
            return Pair(0f, 0f)
        }

        // Pack entity data for native JNI call
        // Format: [screenX, screenY, distance, health, isVisible, id]
        val data = FloatArray(entities.size * 6)
        for (i in entities.indices) {
            val e = entities[i]
            val base = i * 6
            // Aim at upper torso / head (subtract ~25% of height)
            data[base + 0] = e.screenX
            data[base + 1] = e.screenY - (e.screenHeight * 0.25f)
            data[base + 2] = e.distance
            data[base + 3] = e.health
            data[base + 4] = if (e.isVisibleOnScreen) 1f else 0f
            data[base + 5] = e.id.toFloat()
        }

        val result = NativeAimMath.calculateAimAssist(
            crosshairX = crosshairX,
            crosshairY = crosshairY,
            entitiesArray = data,
            count = entities.size,
            fovRadius = settings.aimFovRadius,
            smoothness = settings.aimSmoothness,
            priority = settings.targetPriority
        )

        val stepX = result[0]
        val stepY = result[1]
        val targetId = result[2].toInt()
        val isLocked = result[3] > 0.5f

        state.isLocked = isLocked
        state.targetId = targetId
        state.deltaX = stepX
        state.deltaY = stepY

        if (isLocked) {
            val target = entities.find { it.id == targetId }
            state.targetName = target?.name ?: "Unknown"

            // Convert screen step into camera yaw/pitch angular adjustment
            // for realistic aim assist tracking
            val angularStepYaw = (stepX / crosshairX) * (worldSim.fovDeg * 0.35f)
            val angularStepPitch = (stepY / crosshairY) * (worldSim.fovDeg * 0.35f)
            worldSim.rotateCamera(angularStepYaw, -angularStepPitch)
        } else {
            state.targetName = ""
        }

        return Pair(stepX, stepY)
    }
}
