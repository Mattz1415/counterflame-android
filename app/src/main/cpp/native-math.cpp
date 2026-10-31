#include <jni.h>
#include <cmath>
#include <algorithm>
#include <vector>
#include <cfloat>

#define PI 3.14159265358979323846f
#define DEG2RAD(x) ((x) * (PI / 180.0f))

extern "C" {

JNIEXPORT jstring JNICALL
Java_com_scarlet_counterflame_native_NativeAimMath_getNativeVersion(
        JNIEnv* env,
        jobject /* this */) {
    return env->NewStringUTF("CounterFlame Native Engine v1.0.0 (ARM64/x86_64 Optimized)");
}

// World to Screen Projection
// Projects 3D world point (wx, wy, wz) relative to camera (camX, camY, camZ, camYaw, camPitch)
// Returns float array: [screenX, screenY, width, height, distance, isVisible (1.0 or 0.0)]
JNIEXPORT jfloatArray JNICALL
Java_com_scarlet_counterflame_native_NativeAimMath_projectWorldToScreen(
        JNIEnv* env,
        jobject /* this */,
        jfloat wx, jfloat wy, jfloat wz,
        jfloat camX, jfloat camY, jfloat camZ,
        jfloat camYaw, jfloat camPitch,
        jfloat fovDeg,
        jfloat screenW, jfloat screenH) {

    // Relative position
    float dx = wx - camX;
    float dy = wy - camY;
    float dz = wz - camZ;

    float dist = std::sqrt(dx * dx + dy * dy + dz * dz);

    // Transform by camera yaw (horizontal rotation around Y)
    float yawRad = DEG2RAD(camYaw);
    float cosYaw = std::cos(yawRad);
    float sinYaw = std::sin(yawRad);

    float rx = dx * cosYaw - dz * sinYaw;
    float rz = dx * sinYaw + dz * cosYaw;
    float ry = dy;

    // Transform by camera pitch (vertical rotation around X)
    float pitchRad = DEG2RAD(camPitch);
    float cosPitch = std::cos(pitchRad);
    float sinPitch = std::sin(pitchRad);

    float forward = rz * cosPitch + ry * sinPitch;
    float up = ry * cosPitch - rz * sinPitch;
    float right = rx;

    jfloat result[6] = {0.0f, 0.0f, 0.0f, 0.0f, dist, 0.0f};

    // Forward clip: entity is behind camera
    if (forward <= 0.1f) {
        jfloatArray out = env->NewFloatArray(6);
        env->SetFloatArrayRegion(out, 0, 6, result);
        return out;
    }

    // Perspective projection
    float aspect = screenW / screenH;
    float fovRad = DEG2RAD(fovDeg);
    float tanHalfFov = std::tan(fovRad * 0.5f);

    float ndcX = (right / forward) / (tanHalfFov * aspect);
    float ndcY = (up / forward) / tanHalfFov;

    float screenX = (ndcX + 1.0f) * 0.5f * screenW;
    float screenY = (1.0f - ndcY) * 0.5f * screenH;

    // Estimate box dimensions based on distance and human height (~1.8m)
    float boxHeight = (1.8f / forward) * (screenH / (2.0f * tanHalfFov));
    float boxWidth = boxHeight * 0.45f;

    result[0] = screenX;
    result[1] = screenY;
    result[2] = boxWidth;
    result[3] = boxHeight;
    result[4] = dist;
    result[5] = 1.0f; // Visible

    jfloatArray out = env->NewFloatArray(6);
    env->SetFloatArrayRegion(out, 0, 6, result);
    return out;
}

// Calculate Aim Assist
// entities: flat array of [screenX, screenY, distance, health, isVisible, id]
// count: number of entities
// Returns float array: [aimDeltaX, aimDeltaY, targetId, locked (1.0 or 0.0)]
JNIEXPORT jfloatArray JNICALL
Java_com_scarlet_counterflame_native_NativeAimMath_calculateAimAssist(
        JNIEnv* env,
        jobject /* this */,
        jfloat crosshairX, jfloat crosshairY,
        jfloatArray entitiesArray,
        jint count,
        jfloat fovRadius,
        jfloat smoothness,
        jint priority) {

    jfloat out[4] = {0.0f, 0.0f, -1.0f, 0.0f};

    if (count <= 0 || entitiesArray == nullptr) {
        jfloatArray res = env->NewFloatArray(4);
        env->SetFloatArrayRegion(res, 0, 4, out);
        return res;
    }

    jfloat* data = env->GetFloatArrayElements(entitiesArray, nullptr);
    if (!data) {
        jfloatArray res = env->NewFloatArray(4);
        env->SetFloatArrayRegion(res, 0, 4, out);
        return res;
    }

    int bestTargetIndex = -1;
    float bestScore = FLT_MAX;

    for (int i = 0; i < count; ++i) {
        int base = i * 6;
        float sx = data[base + 0];
        float sy = data[base + 1];
        float dist = data[base + 2];
        float hp = data[base + 3];
        float isVis = data[base + 4];
        float id = data[base + 5];

        if (isVis < 0.5f || hp <= 0.0f) continue;

        // Aim point target (head/chest): upper 25% of box or screen point
        float dx = sx - crosshairX;
        float dy = sy - crosshairY;
        float screenDist = std::sqrt(dx * dx + dy * dy);

        // Check if within FOV circle
        if (screenDist > fovRadius) continue;

        float score = FLT_MAX;
        if (priority == 0) {
            // Closest to crosshair
            score = screenDist;
        } else if (priority == 1) {
            // Lowest health
            score = hp * 10.0f + screenDist;
        } else if (priority == 2) {
            // Closest distance
            score = dist * 20.0f + screenDist;
        }

        if (score < bestScore) {
            bestScore = score;
            bestTargetIndex = i;
        }
    }

    if (bestTargetIndex != -1) {
        int base = bestTargetIndex * 6;
        float targetX = data[base + 0];
        float targetY = data[base + 1];
        float targetId = data[base + 5];

        float deltaX = targetX - crosshairX;
        float deltaY = targetY - crosshairY;

        // Smooth interpolation step
        // Smoothness range: 1.0 (instant snap) to 20.0+ (very smooth)
        float smoothFactor = std::max(1.0f, smoothness);
        float stepX = deltaX / smoothFactor;
        float stepY = deltaY / smoothFactor;

        out[0] = stepX;
        out[1] = stepY;
        out[2] = targetId;
        out[3] = 1.0f; // Locked
    }

    env->ReleaseFloatArrayElements(entitiesArray, data, JNI_ABORT);

    jfloatArray res = env->NewFloatArray(4);
    env->SetFloatArrayRegion(res, 0, 4, out);
    return res;
}

} // extern "C"
