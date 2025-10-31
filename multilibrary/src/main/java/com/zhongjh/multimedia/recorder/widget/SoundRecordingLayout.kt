package com.zhongjh.multimedia.recorder.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.zhongjh.multimedia.R
import com.zhongjh.multimedia.widget.BaseOperationLayout

/**
 * 录音控件，多了一个控件集成
 *
 * @author zhongjh
 * @date 2018/10/16
 */
open class SoundRecordingLayout : BaseOperationLayout {

    /**
     * @param context 上下文对象
     * @param attrs XML属性集合
     * @param defStyleAttr 默认样式属性
     */
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null, 0)

    /**
     * 当前活动状态，默认休闲
     */
    var state: Int = STATE_PREVIEW

    val soundRecordingLayoutViewHolder: SoundRecordingLayoutBaseViewHolder
        get() = viewHolder as SoundRecordingLayoutBaseViewHolder

    public override fun newViewHolder(): SoundRecordingLayoutBaseViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.layout_soundrecording_operate_zjh, this, true)
        return SoundRecordingLayoutBaseViewHolder(view)
    }

    override fun startShowLeftRightButtonsAnimator(showCancel: Boolean) {
        super.startShowLeftRightButtonsAnimator(showCancel)
        // 显示播放的按钮
        soundRecordingLayoutViewHolder.rlEdit.visibility = VISIBLE
        state = STATE_RECORDER
    }

    /**
     * 重置本身
     */
    override fun reset() {
        super.reset()
        // 隐藏播放的按钮
        soundRecordingLayoutViewHolder.rlEdit.visibility = INVISIBLE
    }

    class SoundRecordingLayoutBaseViewHolder(rootView: View) : BaseViewHolder(rootView) {
        val ivRecord: ImageView = rootView.findViewById(R.id.ivRecord)
        val rlEdit: RelativeLayout = rootView.findViewById(R.id.rlEdit)

        init {
            // 设置成普通点击事件
            btnConfirm.setProgressMode(false)
        }
    }

    companion object {
        /**
         * 纯预览状态 - 没有多图，没有多视频
         */
        const val STATE_PREVIEW: Int = 0x01

        /**
         * 录音状态 - 录音后，就修改成这个状态
         */
        const val STATE_RECORDER: Int = 0x02
    }
}
