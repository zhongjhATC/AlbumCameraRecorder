package com.zhongjh.multimedia.camera.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.widget.BaseOperationLayout

/**
 * 用于录制，拍摄的一系列控件按钮
 *
 * @author zhongjh
 * @date 2018/10/16
 */
open class PhotoVideoLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseOperationLayout(context, attrs, defStyleAttr) {
    val photoVideoLayoutViewHolder: BaseViewHolder
        get() = viewHolder as BaseViewHolder

    public override fun newViewHolder(): BaseViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.layout_photovideo_operate_zjh, this, true)
        return BaseViewHolder(view)
    }

    class BaseViewHolder(rootView: View) : BaseOperationLayout.BaseViewHolder(rootView) {
        val rlEdit: RelativeLayout = rootView.findViewById(R.id.rlEdit)
    }
}
