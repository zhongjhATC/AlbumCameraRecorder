package com.zhongjh.multimedia.recorder.widget

import android.content.Context
import android.util.AttributeSet
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
class SoundRecordingLayout : BaseOperationLayout {
    /**
     * 当前活动状态，默认休闲
     */
    var state: Int = STATE_PREVIEW

    val soundRecordingLayoutViewHolder: SoundRecordingLayoutBaseViewHolder
        get() = viewHolder as SoundRecordingLayoutBaseViewHolder

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    public override fun newViewHolder(): SoundRecordingLayoutBaseViewHolder {
        return SoundRecordingLayoutBaseViewHolder(inflate(context, R.layout.layout_soundrecording_operate_zjh, this))
    }

    override fun startShowLeftRightButtonsAnimator(showCancel: Boolean) {
        super.startShowLeftRightButtonsAnimator(showCancel)
        // 显示播放的按钮
        soundRecordingLayoutViewHolder.rlSoundRecording.visibility = VISIBLE
        state = STATE_RECORDER
    }

    /**
     * 重置本身
     */
    override fun reset() {
        super.reset()
        // 隐藏播放的按钮
        soundRecordingLayoutViewHolder.rlSoundRecording.visibility = INVISIBLE
    }

    class SoundRecordingLayoutBaseViewHolder(rootView: View) : BaseViewHolder(rootView) {
        val ivRecord: ImageView = rootView.findViewById(R.id.ivRecord)
        val rlSoundRecording: RelativeLayout = rootView.findViewById(R.id.rlSoundRecording)

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
