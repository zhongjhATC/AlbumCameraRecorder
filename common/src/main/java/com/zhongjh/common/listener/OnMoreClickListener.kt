package com.zhongjh.common.listener

import android.util.Log
import android.view.View

/**
 * 防抖动点击
 *
 * @author zhongjh
 * @updateDate 2021/12/9
 * @date 2021/10/14
 */
abstract class OnMoreClickListener : View.OnClickListener {

    private var lastTime: Long = 0L
    private var btnId = 0

    abstract fun onListener(v: View)
    override fun onClick(v: View) {
        val currentTime = System.currentTimeMillis()
        if (btnId != v.id) {
            lastTime = 0
        }
        if (currentTime - lastTime > MIN_CLICK_DELAY_TIME) {
            btnId = v.id
            lastTime = currentTime
            Log.d("OnMoreClickListener", "" + currentTime)
            onListener(v)
        }
    }

    companion object {
        const val MIN_CLICK_DELAY_TIME = 1000
    }


}