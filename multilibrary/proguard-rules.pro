# R8 missing classes 警告
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.concurrent.GuardedBy
-dontwarn kotlin.jvm.internal.SourceDebugExtension

# 通用基础
-keepattributes Signature
-keepattributes *Annotation*

# databinding
-dontwarn android.databinding.**
-dontwarn androidx.databinding.**

# 保留行号，Bugly上传mapping用于定位崩溃
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keep class com.zhongjh.multimedia.album.entity.** { *; }
-keep class com.zhongjh.multimedia.camera.entity.** { *; }

-keep class com.zhongjh.multimedia.widget.** { *; }
-dontwarn com.zhongjh.multimedia.widget.**

