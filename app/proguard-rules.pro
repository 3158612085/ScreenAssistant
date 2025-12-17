# Add project specific ProGuard rules here.
-keep class com.screenassistant.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
