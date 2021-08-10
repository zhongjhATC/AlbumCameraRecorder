package com.zhongjh.progresslibrary.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.zhongjh.progresslibrary.R;
import com.zhongjh.progresslibrary.entity.RecordingItem;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;

/**
 * 这是包含播放音频的view 和 上传音频进度的view
 *
 * @author zhongjh
 * @date 2021/7/8
 */
public class PlayProgressView extends ConstraintLayout {

    /**
     * 控件集合
     */
    public ViewHolder mViewHolder;

    /**
     * 是否允许操作
     */
    private boolean isOperation = true;

    private Callback callback;

    public interface Callback {

        /**
         * 音频删除事件
         */
        void onRemoveRecorder();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public PlayProgressView(@NonNull Context context) {
        this(context, null);
    }

    public PlayProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayProgressView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * 初始化view
     */
    private void initView() {
        // 自定义View中如果重写了onDraw()即自定义了绘制，那么就应该在构造函数中调用view的setWillNotDraw(false).
        setWillNotDraw(false);
        mViewHolder = new ViewHolder(View.inflate(getContext(), R.layout.layout_play_progress, this));
        addInit();
        initListener();
    }

    /**
     * 初始化所有事件
     */
    private void initListener() {
        // 音频删除事件
        this.mViewHolder.imgRemoveRecorder.setOnClickListener(v -> {
            callback.onRemoveRecorder();
            // 隐藏音频相关控件
            mViewHolder.groupRecorderProgress.setVisibility(View.GONE);
            mViewHolder.playView.setVisibility(View.GONE);
            mViewHolder.imgRemoveRecorder.setVisibility(View.GONE);
            isShowRemoveRecorder();
        });
    }

    /**
     * 初始化样式
     */
    public void initStyle(int audioDeleteColor, int audioProgressColor, int audioPlayColor) {
        // 设置上传音频等属性
        mViewHolder.imgRemoveRecorder.setColorFilter(audioDeleteColor);
        isShowRemoveRecorder();
        mViewHolder.numberProgressBar.setProgressTextColor(audioProgressColor);
        mViewHolder.numberProgressBar.setReachedBarColor(audioProgressColor);
        mViewHolder.tvRecorderTip.setTextColor(audioProgressColor);

        // 设置播放控件里面的播放按钮的颜色
        mViewHolder.playView.mViewHolder.imgPlay.setColorFilter(audioPlayColor);
        mViewHolder.playView.mViewHolder.tvCurrentProgress.setTextColor(audioProgressColor);
        mViewHolder.playView.mViewHolder.tvTotalProgress.setTextColor(audioProgressColor);
    }

    /**
     * 重置
     */
    public void reset() {
        mViewHolder.playView.reset();
    }

    /**
     * 赋值事件
     *
     * @param listener 事件
     */
    public void setListener(MaskProgressLayoutListener listener) {
        mViewHolder.playView.setListener(listener);
    }

    /**
     * 添加后的初始化
     */
    public void addInit() {
        // 显示上传中的音频
        mViewHolder.groupRecorderProgress.setVisibility(View.VISIBLE);
        mViewHolder.playView.setVisibility(View.GONE);
        isShowRemoveRecorder();
    }

    /**
     * 设置是否显示删除音频按钮
     */
    public void isShowRemoveRecorder() {
        if (isOperation) {
            // 如果是可操作的，就判断是否有音频数据
            if (this.mViewHolder.playView.getVisibility() == View.VISIBLE || this.mViewHolder.groupRecorderProgress.getVisibility() == View.VISIBLE) {
                mViewHolder.imgRemoveRecorder.setVisibility(View.VISIBLE);
            } else {
                mViewHolder.imgRemoveRecorder.setVisibility(View.GONE);
            }
        } else {
            mViewHolder.imgRemoveRecorder.setVisibility(View.GONE);
        }
    }

    /**
     * 设置是否可操作(一般只用于展览作用)
     *
     * @param isOperation 是否操作
     */
    public void setOperation(boolean isOperation) {
        this.isOperation = isOperation;
    }

    /**
     * 初始化相关数据
     *
     * @param recordingItem      音频数据源
     * @param audioProgressColor 进度条颜色
     */
    public void setData(RecordingItem recordingItem, int audioProgressColor) {
        mViewHolder.playView.setData(recordingItem, audioProgressColor);
    }

    /**
     * 音频上传完成后
     */
    public void audioUploadCompleted() {
        // 显示完成后的音频
        mViewHolder.groupRecorderProgress.setVisibility(View.GONE);
        mViewHolder.playView.setVisibility(View.VISIBLE);
        isShowRemoveRecorder();
    }

    public static class ViewHolder {

        View rootView;
        public NumberProgressBar numberProgressBar;
        public ImageView imgRemoveRecorder;
        public Group groupRecorderProgress;
        public PlayView playView;
        public TextView tvRecorderTip;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.numberProgressBar = rootView.findViewById(R.id.numberProgressBar);
            this.imgRemoveRecorder = rootView.findViewById(R.id.imgRemoveRecorder);
            this.playView = rootView.findViewById(R.id.playView);
            this.groupRecorderProgress = rootView.findViewById(R.id.groupRecorderProgress);
            this.tvRecorderTip = rootView.findViewById(R.id.tvRecorderTip);
        }
    }

}
