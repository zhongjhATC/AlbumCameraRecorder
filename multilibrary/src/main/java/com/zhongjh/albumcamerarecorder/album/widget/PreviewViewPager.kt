package com.zhongjh.albumcamerarecorder.album.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * 预览的ViewPager
 * 主要为了解决 viewpager中放一个可缩放的imageview时，不停缩放会导致闪退的问题: java.lang.IllegalArgumentException: pointerIndex out of range
 *
 * @author zhongjh
 */
class PreviewViewPager(context: Context, attrs: AttributeSet?) : ViewPager(
    context, attrs
) {
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        try {
            return super.onTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}