# ------------------------------------该库混淆--------------------------------------------------------- #
# 不混淆指定包名下的类名，及类里的内容
-keep class com.zhongjh.** {*;}
-dontwarn com.zhongjh.**



# 禁止混淆核心原则：只要运行时不是直接代码调用，而是靠「字符串名字去找到类 / 函数」的，全部需要保护；普通代码直接调用的业务类，允许 R8 混淆、瘦身。以下是不混淆的名单

# 1. 序列化实体类（JSON / 数据传递）
# 1.1 Gson / Moshi / Fastjson 解析的 Model、data class
# 1.2 Parcelable / Serializable 跨页面传递的数据类
# 原因：反射根据字段名解析 JSON，一旦字段名被混淆，解析直接失败

# 2. XML 布局中直接使用的 自定义 View
# 2.1 继承 View / ViewGroup / AppCompatXXX 的控件（在 xml 写全类名）
# 原因：LayoutInflater 通过完整类名字符串反射构造控件，类名改了就 inflate 崩溃

# 3. JNI 互调用的类与方法
# 3.1 带有 external fun 的 Kotlin /native 方法的 Java 类
# 3.2 C/C++ 代码里写死包名类名去调用的 Java/Kotlin 类
# 原因：Native 层硬编码类名、方法名，混淆后找不到符号

# 4. 反射动态加载调用的类
# 4.1 Class.forName("完整类名") 硬编码字符串反射
# 4.2 配置文件、服务端下发类名动态创建对象
# 4.3 变量拼接字符串的动态反射（插件化、动态表单）

# 5. 框架自动反射的组件
# 5.1 Activity / Service / BroadcastReceiver（AndroidManifest 注册的四大组件）
# 注：AGP 新版自带规则默认保护四大组件，一般不用手动写 Keep
# 5.2 注解驱动框架用到的类（部分第三方 SDK 的实体、回调）

# 6. 给 H5 JSBridge 暴露的 Native 方法
# 6.1 @JavascriptInterface 标记的方法所在的类
# 6.2 JS 通过字符串调用 Java 方法，名字不能变

# 7. 保留给外部 SDK / 业务模块调用的公开 API 类
# 7.1 作为 AAR 对外提供，外部 App 会反射 / 直接调用的类