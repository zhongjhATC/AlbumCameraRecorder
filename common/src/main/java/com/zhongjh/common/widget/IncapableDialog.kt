package com.zhongjh.common.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.zhongjh.common.R

/**
 * 公共弹窗
 * @author zhongjh
 * @date 2021/11/10
 */
class IncapableDialog : DialogFragment() {

    private var mContext: Context? = null

    companion object {

        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_MESSAGE = "extra_message"

        @JvmStatic
        fun newInstance(title: String?, message: String?): IncapableDialog? {
            val dialog = IncapableDialog()
            val args = Bundle()
            args.putString(EXTRA_TITLE, title)
            args.putString(EXTRA_MESSAGE, message)
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bundle = arguments
        var title: String? = null
        var message: String? = null
        if (bundle != null) {
            title = bundle.getString(EXTRA_TITLE)
            message = bundle.getString(EXTRA_MESSAGE)
        }
        val builder = AlertDialog.Builder(mContext!!)
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title)
        }
        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message)
        }
        builder.setPositiveButton(R.string.button_ok) { dialog, _ -> dialog.dismiss() }
        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }


}