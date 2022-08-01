##----------------------基本指令区-----------------
## 设置混淆的压缩比率 0 ~ 7
#-optimizationpasses 5
## 混淆后类名都为小写   Aa aA
#-dontusemixedcaseclassnames
## 指定不去忽略非公共库的类
#-dontskipnonpubliclibraryclasses
## 不优化输入的类文件
#-dontoptimize
## 不做预校验的操作
#-dontpreverify
## 混淆时不记录日志
#-verbose
# 保留代码行号，方便异常信息的追踪
-keepattributes SourceFile,LineNumberTable
## 混淆采用的算法.
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#
## dump文件列出apk包内所有class的内部结构
#-dump class_files.txt
## seeds.txt文件列出未混淆的类和成员
#-printseeds seeds.txt
## usage.txt文件列出从apk中删除的代码
#-printusage unused.txt
## mapping文件列出混淆前后的映射
#-printmapping mapping.txt
## 忽略警告 不加的话某些情况下打包会报错中断
#-ignorewarnings
#
#
#
##----------------------Android通用-----------------
#
## 避免混淆Android基本组件，下面是兼容性比较高的规则
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Fragment
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference
#-keep public class * extends android.view.View
#-keep public class com.android.vending.licensing.ILicensingService
#
## 保留support下的所有类及其内部类
#-keep class android.support.** {*;}
#-keep interface android.support.** {*;}
#-keep public class * extends android.support.v4.**
#-keep public class * extends android.support.v7.**
#-keep public class * extends android.support.annotation.**
#-dontwarn android.support.**
#
## 保留androidx下的所有类及其内部类
#-keep class androidx.** {*;}
#-keep public class * extends androidx.**
#-keep interface androidx.** {*;}
#-keep class com.google.android.material.** {*;}
#-dontwarn androidx.**
#-dontwarn com.google.android.material.**
#-dontnote com.google.android.material.**
#
## 保持Activity中与View相关方法不被混淆
#-keepclassmembers class * extends android.app.Activity{
#        public void *(android.view.View);
#}
#
## 避免混淆所有native的方法,涉及到C、C++
#-keepclasseswithmembernames class * {
#        native <methods>;
#}
#
## 避免混淆自定义控件类的get/set方法和构造函数
#-keep public class * extends android.view.View{
#        *** get*();
#        void set*(***);
#        public <init>(android.content.Context);
#        public <init>(android.content.Context,android.util.AttributeSet);
#        public <init>(android.content.Context,android.util.AttributeSet,int);
#}
#-keepclasseswithmembers class * {
#        public <init>(android.content.Context, android.util.AttributeSet);
#        public <init>(android.content.Context, android.util.AttributeSet, int);
#}
#
## 避免混淆枚举类
#-keepclassmembers enum * {
#        public static **[] values();
#        public static ** valueOf(java.lang.String);
#}
#
## 避免混淆序列化类
## 不混淆Parcelable和它的实现子类，还有Creator成员变量
#-keep class * implements android.os.Parcelable {
#        public static final android.os.Parcelable$Creator *;
#}
#
## 不混淆Serializable和它的实现子类、其成员变量
#-keep public class * implements java.io.Serializable {*;}
#-keepclassmembers class * implements java.io.Serializable {
#        static final long serialVersionUID;
#        private static final java.io.ObjectStreamField[] serialPersistentFields;
#        private void writeObject(java.io.ObjectOutputStream);
#        private void readObject(java.io.ObjectInputStream);
#        java.lang.Object writeReplace();
#        java.lang.Object readResolve();
#}
#
## 资源ID不被混淆
#-keep class **.R$* {*;}
#
## 回调函数事件不能混淆
#-keepclassmembers class * {
#        void *(**On*Event);
#        void *(**On*Listener);
#}
#
## Webview 相关不混淆
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#-keepclassmembers class * extends android.webkit.WebViewClient {
#        public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
#        public boolean *(android.webkit.WebView, java.lang.String);
#}
#-keepclassmembers class * extends android.webkit.WebViewClient {
#        public void *(android.webkit.WebView, java.lang.String);
# }
#
## 使用GSON、fastjson等框架时，所写的JSON对象类不混淆，否则无法将JSON解析成对应的对象
#-keepclassmembers class * {
#         public <init>(org.json.JSONObject);
#}
#
##不混淆泛型
#-keepattributes Signature
#
##避免混淆注解类
#-dontwarn android.annotation
#-keepattributes *Annotation*
#
##避免混淆内部类
#-keepattributes InnerClasses
#
##（可选）避免Log打印输出
#-assumenosideeffects class android.util.Log {
#        public static *** v(...);
#        public static *** d(...);
#        public static *** i(...);
#        public static *** w(...);
#        public static *** e(...);
#}
#
##kotlin 相关
#-dontwarn kotlin.**
#-keep class kotlin.** { *; }
#-keep interface kotlin.** { *; }
#-keepclassmembers class kotlin.Metadata {
#    public <methods>;
#}
#-keepclasseswithmembers @kotlin.Metadata class * { *; }
#-keepclassmembers class **.WhenMappings {
#    <fields>;
#}
#-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
#    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
#}
#
#-keep class kotlinx.** { *; }
#-keep interface kotlinx.** { *; }
#-dontwarn kotlinx.**
#-dontnote kotlinx.serialization.SerializationKt
#
#-keep class org.jetbrains.** { *; }
#-keep interface org.jetbrains.** { *; }
#-dontwarn org.jetbrains.**
#
#-keep class org.xmlpull.** {*;}
#-keep public class * extends org.xmlpull.**
#-keep interface org.xmlpull.** {*;}
#
#
## ------------------------------------第三方库--------------------------------------------------------- #
#
#
