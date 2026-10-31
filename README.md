# CounterFlame Android Companion & Tactical Simulation Prototype

An independently implemented Android tactical shooter companion and visualization prototype inspired by CounterFlame. Built with **Kotlin**, **C++ (NDK/JNI)**, and **Material 3**.

---

## 1. Source APK Inspection Summary

Inspection of `CounterFlame-1.3.0.1-c5.apk`:
- **Package Name:** `com.ScarletWork.CounterFlame`
- **Version Code:** `5` (Version Name: `1.3.0.1`)
- **Min SDK:** `26` (Android 8.0 Oreo)
- **Target SDK:** `35` (Android 15)
- **Compile SDK:** `35`
- **Core Engine:** Unity 2022+ with IL2CPP 64-bit runtime and Burst compiler
- **Graphics API:** OpenGL ES 3.0 / Vulkan
- **Architectures:** `arm64-v8a`, `armeabi-v7a`
- **Native Libraries:** `libunity.so`, `libil2cpp.so`, `libmain.so`, `lib_burst_generated.so`, `libFirebaseCppApp-12_0_0.so`, `libFirebaseCppAuth.so`, `libFirebaseCppDatabase.so`, `libnative-googlesignin.so`
- **Primary Permissions:** `INTERNET`, `ACCESS_NETWORK_STATE`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` (maxSdk 28), `WAKE_LOCK`

---

## 2. Project Architecture

The independent project is structured into modular layers:

```
counterflame-android/
├── app/
│   ├── src/main/
│   │   ├── cpp/
│   │   │   ├── CMakeLists.txt        # Native C++ build script
│   │   │   └── native-math.cpp       # JNI World-to-Screen projection & aim calculation
│   │   ├── java/com/scarlet/counterflame/
│   │   │   ├── native/
│   │   │   │   └── NativeAimMath.kt  # JNI bindings
│   │   │   ├── renderer/
│   │   │   │   ├── OverlayRenderer.kt       # Canvas rendering (ESP boxes, badges, HUD)
│   │   │   │   ├── RenderConfig.kt          # Color tokens, paints, styling
│   │   │   │   └── SimulationSurfaceView.kt # 60 FPS dedicated render thread
│   │   │   ├── simulation/
│   │   │   │   ├── SimulatedEntity.kt       # 3D tactical entities (CT/T)
│   │   │   │   ├── WorldSimulation.kt       # 3D spatial simulation & camera projection
│   │   │   │   └── AimAssistSimulator.kt    # Native aim assist math & target tracking
│   │   │   ├── settings/
│   │   │   │   ├── AppSettings.kt           # Configuration data model
│   │   │   │   └── ConfigManager.kt         # SharedPreferences persistence
│   │   │   ├── ui/
│   │   │   │   └── FloatingControlPanel.kt  # Floating Material 3 configuration panel
│   │   │   └── MainActivity.kt              # Immersive fullscreen container
│   │   └── res/                             # Material 3 themes, layouts, vectors
```

---

## 3. Features Implemented

### ESP Visualization
- **2D Bounding Boxes:** Dynamic perspective-scaled rectangles or corner-bracket style.
- **Player Name Labels:** Top badges with team tags (`[T]`, `[CT]`) and names (`donk`, `s1mple`, `NiKo`, etc.).
- **Distance Indicators:** Bottom meters badge (`14.2m | AK-47`).
- **Health Indicators:** Color-coded dynamic vertical health bar (Green > Yellow > Red).
- **Team Identification:** Color coding (Orange for Terrorist, Cyan for Counter-Terrorist).
- **Adjustable Overlay Opacity:** Configurable from 20% to 100%.
- **Snaplines:** Tracers connecting screen baseline to targets.
- **Target Frame Rate:** 60 FPS SurfaceView rendering pipeline.

### Aim Assist Simulation
- **Native JNI Engine:** Highly optimized C++ vector math for projection and target acquisition.
- **FOV Circle Visualization:** Real-time visual targeting radius around reticle.
- **Configurable Sensitivity & Damping:** Smooth interpolation avoiding snapping artifacts.
- **Target Priority Modes:**
  1. Closest to Crosshair
  2. Lowest Health
  3. Closest 3D Distance
- **Lock Feedback:** Crosshair and HUD diagnostics indicate active target lock.

### Android User Interface
- **Material 3 Dark Theme:** Modern tactical aesthetics.
- **Floating Configuration Panel:** Collapsible menu with animated transitions.
- **Interactive Controls:** Toggle switches, continuous sliders, priority radio options.
- **Persistent Preferences:** Automatically saves and loads settings across sessions.
- **Touch Camera Control:** Touch and drag to look around the 3D simulated arena.

---

## 4. Build Instructions

### Prerequisites
- JDK 17
- Android SDK (API 34)
- Android NDK (`26.1.10909125`)
- CMake (`3.22.1+`)

### Build Release APK
```bash
./gradlew assembleRelease
```
The generated signed APK will be located at:
`app/build/outputs/apk/release/app-release.apk`

### Run Unit Tests
```bash
./gradlew test
```

---

## 5. Security & Boundary Notice

This project operates strictly on internally simulated entities within its own standalone Android activity. It does not manipulate external games, perform memory reading/writing, hook third-party processes, or bypass anti-cheat mechanisms.
