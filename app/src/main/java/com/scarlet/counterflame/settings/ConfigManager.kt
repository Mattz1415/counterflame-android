package com.scarlet.counterflame.settings

import android.content.Context
import android.content.SharedPreferences

class ConfigManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("counterflame_companion_prefs", Context.MODE_PRIVATE)

    fun loadSettings(): AppSettings {
        return AppSettings(
            espEnabled = prefs.getBoolean("esp_enabled", true),
            showBoundingBoxes = prefs.getBoolean("show_boxes", true),
            boxStyleCorners = prefs.getBoolean("box_corners", false),
            showPlayerNames = prefs.getBoolean("show_names", true),
            showDistance = prefs.getBoolean("show_distance", true),
            showHealthBar = prefs.getBoolean("show_health", true),
            teamColorsEnabled = prefs.getBoolean("team_colors", true),
            overlayOpacity = prefs.getFloat("overlay_opacity", 0.95f),
            showSnaplines = prefs.getBoolean("show_snaplines", false),
            aimAssistEnabled = prefs.getBoolean("aim_assist_enabled", true),
            aimFovRadius = prefs.getFloat("aim_fov_radius", 180f),
            aimSmoothness = prefs.getFloat("aim_smoothness", 6.0f),
            targetPriority = prefs.getInt("target_priority", 0),
            showFovCircle = prefs.getBoolean("show_fov_circle", true),
            entityCount = prefs.getInt("entity_count", 8),
            simulateMovement = prefs.getBoolean("simulate_movement", true),
            targetFps = prefs.getInt("target_fps", 60)
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit().apply {
            putBoolean("esp_enabled", settings.espEnabled)
            putBoolean("show_boxes", settings.showBoundingBoxes)
            putBoolean("box_corners", settings.boxStyleCorners)
            putBoolean("show_names", settings.showPlayerNames)
            putBoolean("show_distance", settings.showDistance)
            putBoolean("show_health", settings.showHealthBar)
            putBoolean("team_colors", settings.teamColorsEnabled)
            putFloat("overlay_opacity", settings.overlayOpacity)
            putBoolean("show_snaplines", settings.showSnaplines)
            putBoolean("aim_assist_enabled", settings.aimAssistEnabled)
            putFloat("aim_fov_radius", settings.aimFovRadius)
            putFloat("aim_smoothness", settings.aimSmoothness)
            putInt("target_priority", settings.targetPriority)
            putBoolean("show_fov_circle", settings.showFovCircle)
            putInt("entity_count", settings.entityCount)
            putBoolean("simulate_movement", settings.simulateMovement)
            putInt("target_fps", settings.targetFps)
            apply()
        }
    }
}
