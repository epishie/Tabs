# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/josephiturralde/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Gson
-keep class sun.misc.Unsafe { *; }
-keep class com.epishie.ripley.feature.shared.model.** { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Rxjava
-dontwarn rx.internal.util.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}

# Picasso/okhttp
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**

# Others
-dontwarn okio.**
-keepattributes Signature
-keepattributes Exceptions
