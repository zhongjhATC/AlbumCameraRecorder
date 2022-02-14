package com.zhongjh.progresslibrary.entity

import android.os.Parcel
import android.view.View
import com.zhongjh.common.entity.LocalFile
import com.zhongjh.common.entity.MultiMedia
import com.zhongjh.progresslibrary.widget.MaskProgressView
import com.zhongjh.progresslibrary.widget.PlayProgressView

/**
 * 多媒体实体类,包含着view
 *
 * @author zhongjh
 * @date 2021/12/13
 */
class MultiMediaView : MultiMedia {

    companion object {

        private const val FULL_PERCENT = 100

    }

    /**
     * 绑定子view,包含其他所有控件（显示view,删除view）
     */
    lateinit var itemView: View

    /**
     * 绑定音频View
     */
    lateinit var playProgressView: PlayProgressView

    /**
     * 绑定子view: 用于显示图片、视频的view
     */
    lateinit var maskProgressView: MaskProgressView

    /**
     * 是否进行上传动作
     */
    var isUploading = false

    constructor() : super()

    constructor(input: Parcel) : super(input)

    constructor(mimeType: String) {
        this.mimeType = mimeType
    }

    constructor(localFile: LocalFile) : super(localFile)

    /**
     * 给予进度，根据类型设置相应进度动作
     */
    fun setPercentage(percent: Int) {
        if (isImageOrGif() || isVideo()) {
            this.maskProgressView.setPercentage(percent)
        } else if (isAudio()) {
            // 隐藏显示音频的设置一系列动作
            playProgressView.mViewHolder.numberProgressBar.progress = percent
            if (percent == FULL_PERCENT) {
                playProgressView.audioUploadCompleted()
            }
        }
    }


}