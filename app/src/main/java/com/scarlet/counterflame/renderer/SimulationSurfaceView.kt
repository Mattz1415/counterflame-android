package com.scarlet.counterflame.renderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.scarlet.counterflame.native.NativeAimMath
import com.scarlet.counterflame.settings.AppSettings
import com.scarlet.counterflame.simulation.AimAssistSimulator
import com.scarlet.counterflame.simulation.WorldSimulation

class SimulationSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {

    private val renderer = OverlayRenderer()
    val worldSimulation = WorldSimulation()
    val aimSimulator = AimAssistSimulator()
    var appSettings = AppSettings()

    private var renderThread: Thread? = null
    @Volatile
    private var isRunning = false

    private var lastFrameTime = System.nanoTime()
    private var currentFps = 60f
    private var frameCount = 0
    private var fpsTimer = System.currentTimeMillis()

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false

    private var nativeVersion = "Loading..."

    init {
        holder.addCallback(this)
        isFocusable = true
        try {
            nativeVersion = NativeAimMath.getNativeVersion()
        } catch (e: Throwable) {
            nativeVersion = "Native Lib Fallback: ${e.message}"
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        worldSimulation.initialize(appSettings.entityCount)
        renderThread = Thread(this, "SimulationRenderThread").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Handled dynamically via canvas dimensions
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false
        var retry = true
        while (retry) {
            try {
                renderThread?.join(500)
                retry = false
            } catch (e: InterruptedException) {
                // Thread interruption retry
            }
        }
        renderThread = null
    }

    override fun run() {
        while (isRunning) {
            val now = System.nanoTime()
            val deltaSec = ((now - lastFrameTime) / 1_000_000_000f).coerceIn(0.001f, 0.1f)
            lastFrameTime = now

            // FPS calculation
            frameCount++
            val currentTimeMs = System.currentTimeMillis()
            if (currentTimeMs - fpsTimer >= 500) {
                currentFps = (frameCount * 1000f) / (currentTimeMs - fpsTimer)
                frameCount = 0
                fpsTimer = currentTimeMs
            }

            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    val w = canvas.width.toFloat()
                    val h = canvas.height.toFloat()
                    val cx = w * 0.5f
                    val cy = h * 0.5f

                    // 1. Update 3D World Simulation
                    worldSimulation.update(deltaSec, w, h, appSettings.simulateMovement)

                    // 2. Update Aim Assist Simulator
                    aimSimulator.update(
                        entities = worldSimulation.entities,
                        crosshairX = cx,
                        crosshairY = cy,
                        settings = appSettings,
                        worldSim = worldSimulation
                    )

                    // 3. Clear Canvas
                    canvas.drawColor(Color.parseColor("#0A1118"))

                    // 4. Render All Visualizations
                    renderer.render(
                        canvas = canvas,
                        entities = worldSimulation.entities,
                        settings = appSettings,
                        aimState = aimSimulator.state,
                        fps = currentFps,
                        nativeEngineVersion = nativeVersion
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            // Target frame limiter (60 FPS ~16ms frame target)
            val elapsedMs = (System.nanoTime() - now) / 1_000_000
            val targetFrameMs = 1000 / appSettings.targetFps.coerceIn(30, 120)
            val sleepTime = targetFrameMs - elapsedMs
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime.toLong())
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                isDragging = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    lastTouchX = event.x
                    lastTouchY = event.y

                    // Sensitivity scaling
                    val sensitivity = 0.18f
                    worldSimulation.rotateCamera(dx * sensitivity, dy * sensitivity)
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun onResume() {
        if (!isRunning && holder.surface.isValid) {
            surfaceCreated(holder)
        }
    }

    fun onPause() {
        isRunning = false
    }
}
