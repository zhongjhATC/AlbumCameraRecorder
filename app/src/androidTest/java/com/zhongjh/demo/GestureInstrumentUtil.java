package com.zhongjh.demo;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.MotionEvent;

import androidx.test.platform.app.InstrumentationRegistry;

public class GestureInstrumentUtil {

    private GestureInstrumentUtil() {
    }

    /**
     * 长按手势
     *
     * @param x      屏幕绝对X坐标
     * @param y      屏幕绝对Y坐标
     * @param holdMs 总按住时长(ms)，包含系统长按识别1500ms
     */
    public static void longPress(float x, float y, long holdMs) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        long downTime = SystemClock.uptimeMillis();
        long endTime = downTime + holdMs;

        // 按下
        instrumentation.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0));

        // 循环微小MOVE，维持触摸会话，驱动自定义控件环形动画
        while (SystemClock.uptimeMillis() < endTime) {
            long currentTs = SystemClock.uptimeMillis();
            instrumentation.sendPointerSync(MotionEvent.obtain(downTime, currentTs, MotionEvent.ACTION_MOVE, x + 0.02f, y + 0.02f, 0));
            SystemClock.sleep(50);
        }

        // 抬起
        instrumentation.sendPointerSync(MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, y, 0));
    }


}
