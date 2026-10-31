package com.scarlet.counterflame.simulation

enum class Team {
    COUNTER_TERRORIST,
    TERRORIST
}

data class SimulatedEntity(
    val id: Int,
    var name: String,
    var team: Team,
    var worldX: Float,
    var worldY: Float,
    var worldZ: Float,
    var velocityX: Float = 0f,
    var velocityZ: Float = 0f,
    var health: Float = 100f,
    val maxHealth: Float = 100f,
    var weapon: String = "AK-47",
    var isDormant: Boolean = false,
    
    // Projected Screen Coordinates (computed per frame)
    var screenX: Float = 0f,
    var screenY: Float = 0f,
    var screenWidth: Float = 0f,
    var screenHeight: Float = 0f,
    var distance: Float = 0f,
    var isVisibleOnScreen: Boolean = false
) {
    fun updatePosition(deltaSeconds: Float) {
        worldX += velocityX * deltaSeconds
        worldZ += velocityZ * deltaSeconds

        // Bounce within arena bounds [-25m to 25m, Z from 5m to 45m]
        if (worldX < -22f) {
            worldX = -22f
            velocityX = -velocityX
        } else if (worldX > 22f) {
            worldX = 22f
            velocityX = -velocityX
        }

        if (worldZ < 6f) {
            worldZ = 6f
            velocityZ = -velocityZ
        } else if (worldZ > 42f) {
            worldZ = 42f
            velocityZ = -velocityZ
        }
    }
}
