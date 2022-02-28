/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
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
                TOAST -> Toast.makeText(context, cause.message, Toast.LENGTH_SHORT).show()
                else -> Toast.makeText(context, cause.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}