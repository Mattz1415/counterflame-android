package com.scarlet.counterflame

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.scarlet.counterflame.databinding.ActivityMainBinding
import com.scarlet.counterflame.settings.ConfigManager
import com.scarlet.counterflame.ui.FloatingControlPanel
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var configManager: ConfigManager
    private lateinit var floatingPanel: FloatingControlPanel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableImmersiveMode()

        configManager = ConfigManager(this)
        val settings = configManager.loadSettings()
        binding.simulationSurfaceView.appSettings = settings

        floatingPanel = FloatingControlPanel(binding.cardFloatingSettings)

        setupUIControls()
    }

    private fun enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setupUIControls() {
        val settings = binding.simulationSurfaceView.appSettings

        // Menu Toggle Buttons
        binding.btnToggleSettings.setOnClickListener {
            floatingPanel.toggleVisibility()
        }
        binding.btnClosePanel.setOnClickListener {
            floatingPanel.hide()
        }

        // ESP Master & Toggles
        binding.switchEsp.isChecked = settings.espEnabled
        binding.switchEsp.setOnCheckedChangeListener { _, isChecked ->
            settings.espEnabled = isChecked
            configManager.saveSettings(settings)
        }

        binding.switchBoxes.isChecked = settings.showBoundingBoxes
        binding.switchBoxes.setOnCheckedChangeListener { _, isChecked ->
            settings.showBoundingBoxes = isChecked
            configManager.saveSettings(settings)
        }

        binding.switchCorners.isChecked = settings.boxStyleCorners
        binding.switchCorners.setOnCheckedChangeListener { _, isChecked ->
            settings.boxStyleCorners = isChecked
            configManager.saveSettings(settings)
        }

        binding.switchNames.isChecked = settings.showPlayerNames
        binding.switchNames.setOnCheckedChangeListener { _, isChecked ->
            settings.showPlayerNames = isChecked
            configManager.saveSettings(settings)
        }

        binding.switchDistance.isChecked = settings.showDistance
        binding.switchDistance.setOnCheckedChangeListener { _, isChecked ->
            settings.showDistance = isChecked
            configManager.saveSettings(settings)
        }

        binding.switchHealth.isChecked = settings.showHealthBar
        binding.switchHealth.setOnCheckedChangeListener { _, isChecked ->
            settings.showHealthBar = isChecked
            configManager.saveSettings(settings)
        }

        binding.switchTeamColors.isChecked = settings.teamColorsEnabled
        binding.switchTeamColors.setOnCheckedChangeListener { _, isChecked ->
            settings.teamColorsEnabled = isChecked
            configManager.saveSettings(settings)
        }

        binding.switchSnaplines.isChecked = settings.showSnaplines
        binding.switchSnaplines.setOnCheckedChangeListener { _, isChecked ->
            settings.showSnaplines = isChecked
            configManager.saveSettings(settings)
        }

        // Opacity Slider
        val opacityPercent = (settings.overlayOpacity * 100).toInt()
        binding.sliderOpacity.value = opacityPercent.toFloat()
        binding.tvOpacityLabel.text = String.format(Locale.US, "Overlay Opacity: %d%%", opacityPercent)
        binding.sliderOpacity.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                settings.overlayOpacity = value / 100f
                binding.tvOpacityLabel.text = String.format(Locale.US, "Overlay Opacity: %d%%", value.toInt())
                configManager.saveSettings(settings)
            }
        }

        // Aim Assist Simulator
        binding.switchAimAssist.isChecked = settings.aimAssistEnabled
        binding.switchAimAssist.setOnCheckedChangeListener { _, isChecked ->
            settings.aimAssistEnabled = isChecked
            configManager.saveSettings(settings)
        }

        binding.switchFovCircle.isChecked = settings.showFovCircle
        binding.switchFovCircle.setOnCheckedChangeListener { _, isChecked ->
            settings.showFovCircle = isChecked
            configManager.saveSettings(settings)
        }

        // FOV Slider
        binding.sliderFov.value = settings.aimFovRadius
        binding.tvFovLabel.text = String.format(Locale.US, "FOV Radius: %d px", settings.aimFovRadius.toInt())
        binding.sliderFov.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                settings.aimFovRadius = value
                binding.tvFovLabel.text = String.format(Locale.US, "FOV Radius: %d px", value.toInt())
                configManager.saveSettings(settings)
            }
        }

        // Smoothness Slider
        binding.sliderSmoothness.value = settings.aimSmoothness
        binding.tvSmoothnessLabel.text = String.format(Locale.US, "Smoothness / Damping: %.1f", settings.aimSmoothness)
        binding.sliderSmoothness.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                settings.aimSmoothness = value
                binding.tvSmoothnessLabel.text = String.format(Locale.US, "Smoothness / Damping: %.1f", value)
                configManager.saveSettings(settings)
            }
        }

        // Target Priority RadioGroup
        when (settings.targetPriority) {
            0 -> binding.rbPriorityCrosshair.isChecked = true
            1 -> binding.rbPriorityHealth.isChecked = true
            2 -> binding.rbPriorityDistance.isChecked = true
        }
        binding.radioGroupPriority.setOnCheckedChangeListener { _, checkedId ->
            settings.targetPriority = when (checkedId) {
                R.id.rbPriorityCrosshair -> 0
                R.id.rbPriorityHealth -> 1
                R.id.rbPriorityDistance -> 2
                else -> 0
            }
            configManager.saveSettings(settings)
        }

        // Movement Simulation
        binding.switchMovement.isChecked = settings.simulateMovement
        binding.switchMovement.setOnCheckedChangeListener { _, isChecked ->
            settings.simulateMovement = isChecked
            configManager.saveSettings(settings)
        }

        // Entity Count Slider
        binding.sliderEntities.value = settings.entityCount.toFloat()
        binding.tvEntityCountLabel.text = String.format(Locale.US, "Entity Count: %d", settings.entityCount)
        binding.sliderEntities.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val newCount = value.toInt()
                settings.entityCount = newCount
                binding.tvEntityCountLabel.text = String.format(Locale.US, "Entity Count: %d", newCount)
                binding.simulationSurfaceView.worldSimulation.initialize(newCount)
                configManager.saveSettings(settings)
            }
        }

        // Respawn Button
        binding.btnRespawnEntities.setOnClickListener {
            binding.simulationSurfaceView.worldSimulation.initialize(settings.entityCount)
        }
    }

    override fun onResume() {
        super.onResume()
        enableImmersiveMode()
        binding.simulationSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.simulationSurfaceView.onPause()
    }
}
