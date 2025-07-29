package com.zhongjh.multimedia.widget.clickorlongbutton

import android.os.Handler
import android.os.Looper
import android.os.Message

/**
 * 用于处理长按按钮的事件
 * @author zhongjh
 */
class TouchTimeHandler(looper: Looper?, private val task: Task?) : Handler(looper!!) {
    private var delayTimeInMils: Long = 0
    var isFreeNow: Boolean = true
        private set
    private var shouldContinue = false

    fun clearMsg() {
        while (hasMessages(WHAT_233)) {
            removeMessages(WHAT_233)
        }
        shouldContinue = false
        isFreeNow = true
    }

    fun sendSingleMsg(timeDelayed: Long) {
        clearMsg()
        isFreeNow = false
        shouldContinue = false
        sendEmptyMessageDelayed(0, timeDelayed)
    }

    fun sendLoopMsg(timeDelayed: Long, timeDelayedInLoop: Long) {
        clearMsg()
        isFreeNow = false
        delayTimeInMils = timeDelayedInLoop
        shouldContinue = true
        sendEmptyMessageDelayed(0, timeDelayed)
    }

    override fun handleMessage(paramMessage: Message) {
        task?.run()
        if (shouldContinue) {
            sendEmptyMessageDelayed(0, delayTimeInMils)
        }
    }

    interface Task {
        /**
         * 长按的按钮事件
         */
        fun run()
    }

    companion object {
        const val WHAT_233: Int = 0
    }
}