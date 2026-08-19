# LiveKit Android proguard rules

# LiveKit
-keep class io.livekit.** { *; }
-keep class org.webrtc.** { *; }
-dontwarn org.webrtc.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.voxai.app.data.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
