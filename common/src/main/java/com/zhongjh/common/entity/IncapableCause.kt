package com.zhongjh.common.entity

import android.content.Context
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.fragment.app.FragmentActivity
import com.zhongjh.common.widget.IncapableDialog

/**
 * 信息处理，toast和dialog
 * @author zhongjh
 */
class IncapableCause {

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TOAST, DIALOG, NONE)
    annotation class Form

    /**
     * 默认toast模式
     */
    private var form = TOAST
    private var title: String? = null
    private val message: String

    constructor(message: String) {
        this.message = message
    }

    constructor(@Form form: Int, message: String) {
        this.form = form
        this.message = message
    }

    companion object {
        const val TOAST = 0x00
        const val DIALOG = 0x01
        const val NONE = 0x02

        /**
         * 处理窗体显示
         * @param context 上下文
         * @param cause 本身
         */
        @JvmStatic
        fun handleCause(context: Context, cause: IncapableCause?) {
            if (cause == null) {
                return
            }
            when (cause.form) {
                NONE -> {
                }
                DIALOG -> {
                    val incapableDialog: IncapableDialog = IncapableDialog.newInstance(cause.title ?: "", cause.message)
                    incapableDialog.show((context as FragmentActivity).supportFragmentManager,
                            IncapableDialog::class.java.name)
                }
                TOAST -> Toast.makeText(context.applicationContext, cause.message, Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(context.applicationContext, cause.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}