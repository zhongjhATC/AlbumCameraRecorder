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
    val photoVideoLayoutViewHolder: ViewHolder
        get() = viewHolder as ViewHolder

    public override fun newViewHolder(): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.layout_photovideo_operate_zjh, this, true)
        return ViewHolder(view)
    }

    class ViewHolder(rootView: View) : BaseOperationLayout.ViewHolder(rootView) {
        val rlEdit: RelativeLayout = rootView.findViewById(R.id.rlEdit)
    }
}
