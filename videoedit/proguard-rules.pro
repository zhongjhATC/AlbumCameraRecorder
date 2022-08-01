# ------------------------------------该库混淆--------------------------------------------------------- #
# 不混淆指定包名下的类名，及类里的内容
-keep class com.zhongjh.** {*;}
-dontwarn com.zhongjh.**

# https://github.com/microshow/RxFFmpeg
-dontwarn io.microshow.rxffmpeg.**
-keep class io.microshow.rxffmpeg.**{*;}
