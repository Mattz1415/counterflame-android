package com.scarlet.counterflame.simulation

import com.scarlet.counterflame.native.NativeAimMath
import kotlin.random.Random

class WorldSimulation {
    val entities = mutableListOf<SimulatedEntity>()

    // Camera state (simulating player viewpoint in 3D world)
    var camX: Float = 0f
    var camY: Float = 1.65f // Eye level in meters
    var camZ: Float = 0f
    var camYaw: Float = 0f  // Degrees
    var camPitch: Float = 0f // Degrees
    var fovDeg: Float = 75f

    private val namesT = listOf("donk", "s1mple", "NiKo", "ZywOo", "m0NESY")
    private val namesCT = listOf("b1t", "ropz", "dev1ce", "broky", "EliGE")
    private val weaponsT = listOf("AK-47", "Galil AR", "AWP", "MAC-10", "Desert Eagle")
    private val weaponsCT = listOf("M4A1-S", "M4A4", "AWP", "MP9", "USP-S")

    fun initialize(count: Int = 8) {
        entities.clear()
        val random = Random(42)

        for (i in 0 until count) {
            val isTerrorist = (i % 2 == 0)
            val team = if (isTerrorist) Team.TERRORIST else Team.COUNTER_TERRORIST
            val name = if (isTerrorist) namesT[i / 2 % namesT.size] else namesCT[i / 2 % namesCT.size]
            val weapon = if (isTerrorist) weaponsT[i / 2 % weaponsT.size] else weaponsCT[i / 2 % weaponsCT.size]

            // Spread out in front of the camera: X in [-15, 15], Z in [8, 35]
            val wx = (random.nextFloat() - 0.5f) * 28f
            val wy = 0f // Floor level
            val wz = 8f + random.nextFloat() * 26f

            val vx = (random.nextFloat() - 0.5f) * 4.5f
            val vz = (random.nextFloat() - 0.5f) * 3.5f
            val hp = 40f + random.nextFloat() * 60f

            entities.add(
                SimulatedEntity(
                    id = i + 1,
                    name = name,
                    team = team,
                    worldX = wx,
                    worldY = wy,
                    worldZ = wz,
                    velocityX = vx,
                    velocityZ = vz,
                    health = hp,
                    weapon = weapon
                )
            )
        }
    }

    fun update(deltaSeconds: Float, screenW: Float, screenH: Float, movementEnabled: Boolean) {
        if (entities.isEmpty()) {
            initialize()
        }

        for (entity in entities) {
            if (movementEnabled) {
                entity.updatePosition(deltaSeconds)
            }

            // Project 3D position to 2D screen using JNI C++ module
            val proj = NativeAimMath.projectWorldToScreen(
                wx = entity.worldX,
                wy = entity.worldY + 0.9f, // Project center of body
                wz = entity.worldZ,
                camX = camX,
                camY = camY,
                camZ = camZ,
                camYaw = camYaw,
                camPitch = camPitch,
                fovDeg = fovDeg,
                screenW = screenW,
                screenH = screenH
            )

            entity.screenX = proj[0]
            entity.screenY = proj[1]
            entity.screenWidth = proj[2]
            entity.screenHeight = proj[3]
            entity.distance = proj[4]
            entity.isVisibleOnScreen = (proj[5] > 0.5f) &&
                    (entity.screenX + entity.screenWidth > 0) &&
                    (entity.screenX - entity.screenWidth < screenW) &&
                    (entity.screenY + entity.screenHeight > 0) &&
                    (entity.screenY - entity.screenHeight < screenH)
        }
    }

    fun rotateCamera(deltaYaw: Float, deltaPitch: Float) {
        camYaw += deltaYaw
        camPitch = (camPitch + deltaPitch).coerceIn(-60f, 60f)
    }
}
