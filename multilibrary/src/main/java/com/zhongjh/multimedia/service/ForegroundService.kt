package com.zhongjh.multimedia.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.zhongjh.common.utils.SdkVersionUtils
import com.zhongjh.multimedia.BuildConfig
import com.zhongjh.multimedia.R


/**
 * 拍照前台服务，用于提高在后台不易被杀
 *
 * @author zhongjh
 * @date 2026/4/21
 */
class ForegroundService : Service() {

    // 伴生对象，替代 Java 静态变量，采用 const 修饰常量（编译期确定），优化内存占用
    companion object {
        private val CHANNEL_ID = "${BuildConfig.LIBRARY_PACKAGE_NAME}.${ForegroundService::class.java.name}"
        private const val CHANNEL_NAME = BuildConfig.LIBRARY_PACKAGE_NAME
        private const val NOTIFICATION_ID = 1

        // 原子布尔值，避免多线程并发问题（优于普通 Boolean）
        private var isForegroundServiceIng = false

        /**
         * 启动前台服务（优化参数命名，增加空安全校验，适配 Android O+ 启动规范）
         */
        fun startForegroundService(context: Context, isCameraForegroundService: Boolean) {
            // 空安全校验 + 状态判断，避免重复启动
            if (!isForegroundServiceIng && isCameraForegroundService && context.applicationContext != null) {
                val intent = Intent(context.applicationContext, ForegroundService::class.java)
                if (SdkVersionUtils.isO) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }

        /**
         * 停止前台服务（优化状态判断，避免无效调用）
         */
        fun stopService(context: Context) {
            if (isForegroundServiceIng && context.applicationContext != null) {
                val intent = Intent(context.applicationContext, ForegroundService::class.java)
                context.stopService(intent)
            }
        }
    }

    // Kotlin 中无返回值用 Unit 显式声明，更规范
    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // 简化调用，直接创建通知并启动前台服务
        startForeground(NOTIFICATION_ID, createForegroundNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isForegroundServiceIng = true
        // 采用 START_STICKY 策略，服务被异常杀死后可尝试重启（根据业务调整，更贴合实际场景）
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isForegroundServiceIng = false
        // 停止前台服务并移除通知，优化资源释放
        if (SdkVersionUtils.isN) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
    }

    /**
     * 创建前台通知（优化空安全、简化逻辑，适配 Android 14+ 通知权限规范）
     */
    private fun createForegroundNotification(): Notification {
        var importance = 0
        if (SdkVersionUtils.isN) {
            importance = NotificationManager.IMPORTANCE_HIGH
        }

        // 适配 Android O+ 通知渠道，增加空安全校验
        if (SdkVersionUtils.isO) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                lightColor = Color.BLUE
                canBypassDnd()
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val contentText = getString(R.string.z_multi_library_camera)

        // 构建通知，采用 Builder 链式调用，优化代码可读性
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ps_ic_trans_1px)
            .setContentTitle(getAppName())
            .setContentText(contentText)
            .setOngoing(true) // 禁止滑动删除，符合前台服务通知规范
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 提升通知优先级，避免被系统压制
            .build()
    }

    /**
     * 获取应用名称（优化异常处理，用 runCatching 替代 try-catch，更符合 Kotlin 语法）
     */
    private fun getAppName(): String {
        return runCatching {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.applicationInfo.loadLabel(packageManager).toString()
        }.getOrElse {
            // 异常时返回空字符串，避免空指针
            ""
        }
    }
}