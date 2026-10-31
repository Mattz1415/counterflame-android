package com.scarlet.counterflame.settings

data class AppSettings(
    // ESP Features
    var espEnabled: Boolean = true,
    var showBoundingBoxes: Boolean = true,
    var boxStyleCorners: Boolean = false, // false = full box, true = corner brackets
    var showPlayerNames: Boolean = true,
    var showDistance: Boolean = true,
    var showHealthBar: Boolean = true,
    var teamColorsEnabled: Boolean = true,
    var overlayOpacity: Float = 0.95f,
    var showSnaplines: Boolean = false,

    // Aim Assist Simulator
    var aimAssistEnabled: Boolean = true,
    var aimFovRadius: Float = 180f,
    var aimSmoothness: Float = 6.0f,
    var targetPriority: Int = 0, // 0 = Closest Crosshair, 1 = Lowest HP, 2 = Closest Distance
    var showFovCircle: Boolean = true,

    // Simulation Controls
    var entityCount: Int = 8,
    var simulateMovement: Boolean = true,
    var targetFps: Int = 60
)
