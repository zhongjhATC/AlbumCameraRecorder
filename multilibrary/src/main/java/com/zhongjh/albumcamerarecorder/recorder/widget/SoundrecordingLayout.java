package com.zhongjh.albumcamerarecorder.recorder.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.common.Constants;
import com.zhongjh.albumcamerarecorder.widget.OperationLayout;

/**
 * 录音控件，多了一个控件集成
 * Created by zhongjh on 2018/10/16.
 */
public class SoundrecordingLayout extends OperationLayout {

    public int mState = Constants.STATE_PREVIEW;// 当前活动状态，默认休闲

    public SoundrecordingLayout(@NonNull Context context) {
        super(context);
    }

    public SoundrecordingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SoundrecordingLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public ViewHolder newViewHolder() {
        return new ViewHolder(View.inflate(getContext(), R.layout.layout_soundrecording_operae, this));
    }

    @Override
    public void startShowLeftRightButtonsAnimator() {
        super.startShowLeftRightButtonsAnimator();
        // 显示播放的按钮
        ((ViewHolder)mViewHolder).rlSoundRecording.setVisibility(VISIBLE);
        mState = Constants.STATE_RECORDER;
    }

    /**
     * 重置本身
     */
    @Override
    public void reset() {
        super.reset();
        // 隐藏播放的按钮
        ((ViewHolder)mViewHolder).rlSoundRecording.setVisibility(INVISIBLE);
    }

    public class ViewHolder extends OperationLayout.ViewHolder {

        public ImageView iv_ring;
        public ImageView iv_record;
        public RelativeLayout rlSoundRecording;

        public ViewHolder(View rootView) {
            super(rootView);
            this.iv_ring = rootView.findViewById(R.id.iv_ring);
            this.iv_record = rootView.findViewById(R.id.iv_record);
            this.rlSoundRecording = rootView.findViewById(R.id.rlSoundRecording);
        }

    }

}
