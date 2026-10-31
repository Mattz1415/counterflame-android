package com.scarlet.counterflame.native

object NativeAimMath {
    init {
        System.loadLibrary("native-math")
    }

    external fun getNativeVersion(): String

    external fun projectWorldToScreen(
        wx: Float, wy: Float, wz: Float,
        camX: Float, camY: Float, camZ: Float,
        camYaw: Float, camPitch: Float,
        fovDeg: Float,
        screenW: Float, screenH: Float
    ): FloatArray

    external fun calculateAimAssist(
        crosshairX: Float,
        crosshairY: Float,
        entitiesArray: FloatArray,
        count: Int,
        fovRadius: Float,
        smoothness: Float,
        priority: Int
    ): FloatArray
}
