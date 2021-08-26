package com.zhongjh.cameraapp;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * @author zhongjh
 */
public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(this, "daa7c064ac", false);

//        // 内存泄漏检测
//        if (!LeakCanary.isInAnalyzerProcess(this)) {
//            LeakCanary.install(this);
//        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // 腾讯提交bug
        MultiDex.install(base);
    }

}
