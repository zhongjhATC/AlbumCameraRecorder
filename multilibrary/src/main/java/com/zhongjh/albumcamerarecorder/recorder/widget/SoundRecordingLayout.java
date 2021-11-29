package com.zhongjh.albumcamerarecorder.recorder.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.camerastate.StateInterface;
import com.zhongjh.albumcamerarecorder.camera.common.Constants;
import com.zhongjh.albumcamerarecorder.widget.BaseOperationLayout;

/**
 * 录音控件，多了一个控件集成
 *
 * @author zhongjh
 * @date 2018/10/16
 */
public class SoundRecordingLayout extends BaseOperationLayout {

    /**
     * 纯预览状态 - 没有多图，没有多视频
     */
    public static final int STATE_PREVIEW = 0x01;
    /**
     * 录音状态 - 录音后，就修改成这个状态
     */
    public static final int STATE_RECORDER = 0x02;

    /**
     * 当前活动状态，默认休闲
     */
    public int mState = STATE_PREVIEW;

    public SoundRecordingLayout(@NonNull Context context) {
        super(context);
    }

    public SoundRecordingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SoundRecordingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public ViewHolder newViewHolder() {
        return new ViewHolder(View.inflate(getContext(), R.layout.layout_soundrecording_operate, this));
    }

    @Override
    public void startShowLeftRightButtonsAnimator() {
        super.startShowLeftRightButtonsAnimator();
        // 显示播放的按钮
        ((ViewHolder) viewHolder).rlSoundRecording.setVisibility(VISIBLE);
        mState = STATE_RECORDER;
    }

    /**
     * 重置本身
     */
    @Override
    public void reset() {
        super.reset();
        // 隐藏播放的按钮
        ((ViewHolder) viewHolder).rlSoundRecording.setVisibility(INVISIBLE);
    }

    public static class ViewHolder extends BaseOperationLayout.ViewHolder {

        public ImageView ivRing;
        public ImageView ivRecord;
        public RelativeLayout rlSoundRecording;

        public ViewHolder(View rootView) {
            super(rootView);
            this.ivRing = rootView.findViewById(R.id.ivRing);
            this.ivRecord = rootView.findViewById(R.id.ivRecord);
            this.rlSoundRecording = rootView.findViewById(R.id.rlSoundRecording);

            // 设置成普通点击事件
            this.btnConfirm.setProgressMode(false);
        }

    }

}
