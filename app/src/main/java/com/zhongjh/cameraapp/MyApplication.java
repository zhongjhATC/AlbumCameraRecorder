package com.zhongjh.cameraapp;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.tencent.bugly.crashreport.CrashReport;
import com.zhongjh.cameraapp.phone.ErrorActivity;
import com.zhongjh.cameraapp.phone.MainListActivity;

import cat.ereza.customactivityoncrash.config.CaocConfig;

/**
 * @author zhongjh
 */
public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(this, "daa7c064ac", false);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // 腾讯提交bug
        MultiDex.install(base);
        initCrash();
    }

    /**
     * 异常奔溃后自动打开新的Activity,还可以选择重新启动
     */
    private void initCrash() {
        CaocConfig.Builder.create()
                // 背景模式,开启沉浸式
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
                // 是否启动全局异常捕获
                .enabled(true)
                // 是否显示错误详细信息
                .showErrorDetails(true)
                // 是否显示重启按钮
                .showRestartButton(true)
                // 是否跟踪Activity
                .trackActivities(true)
                // 崩溃的间隔时间(毫秒)
                .minTimeBetweenCrashesMs(2000)
                // 错误图标
                .errorDrawable(R.mipmap.ic_launcher)
                // 重新启动后的activity
                .restartActivity(MainListActivity.class)
                // 崩溃后的错误监听
                // .eventListener(new YourCustomEventListener())
                // 崩溃后的错误activity
                .errorActivity(ErrorActivity.class)
                .apply();
    }

}
