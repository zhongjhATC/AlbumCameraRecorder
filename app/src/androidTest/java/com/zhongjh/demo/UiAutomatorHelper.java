package com.zhongjh.demo;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiSelector;

public class UiAutomatorHelper {

    // 私有构造，不能实例化
    private UiAutomatorHelper() {
    }

    /**
     * 将R.id.xxx的int资源id转为UiAutomator需要的完整resourceId字符串
     *
     * @param resId R.id.xxx
     * @return 例如 "com.zhongjh.demo:id/btnClickOrLong"
     */
    public static String getResourceName(int resId) {
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return targetContext.getResources().getResourceName(resId);
    }

    /**
     * 根据R.id生成UiSelector
     */
    public static UiSelector getSelectorById(int resId) {
        String resName = getResourceName(resId);
        return new UiSelector().resourceId(resName);
    }


}



