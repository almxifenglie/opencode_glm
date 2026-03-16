# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class com.squareup.moshi.** { *; }
-keep class **$$JsonAdapter { *; }
-keep class retrofit2.** { *; }
-keepclassmembernames class * {
    @com.squareup.moshi.Json <fields>;
}
