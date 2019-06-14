package com.zhongjh.cameraapp;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(this, "daa7c064ac", false);

        // 检测内存泄漏
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // 腾讯提交bug
        MultiDex.install(base);
    }

}
