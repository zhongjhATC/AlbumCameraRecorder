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
open class PhotoVideoLayout : BaseOperationLayout {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null, 0)

    val photoVideoLayoutViewHolder: PhotoVideoLayoutViewHolder
        get() = viewHolder as PhotoVideoLayoutViewHolder

    public override fun newViewHolder(): PhotoVideoLayoutViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.layout_photovideo_operate_zjh, this, true)
        return PhotoVideoLayoutViewHolder(view)
    }

    class PhotoVideoLayoutViewHolder(rootView: View) : BaseViewHolder(rootView) {
        val rlEdit: RelativeLayout = rootView.findViewById(R.id.rlEdit)
    }
}
