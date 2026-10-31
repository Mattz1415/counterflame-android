# Keep native JNI bindings
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.scarlet.counterflame.native.** { *; }
