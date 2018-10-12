package com.zhongjh.cameraviewsoundrecorder.camera.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.camera.common.Constants;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.OperaeListener;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.PhotoVideoListener;
import com.zhongjh.cameraviewsoundrecorder.camera.util.DisplayMetricsSPUtils;
import com.zhongjh.cameraviewsoundrecorder.camera.widget.photovieobutton.RecordButton;

/**
 * 关于底部集成各个控件的布局
 * Created by zhongjh on 2018/8/7.
 */
public class PhotoVideoLayout extends FrameLayout {

    // region 回调事件监听

    private PhotoVideoListener mPhotoVideoListener;   // 拍照或录制监听
    private OperaeListener mOperaeListener; // 拍照或录制监听结束后的 确认取消事件监控

    public void setPhotoVideoListener(PhotoVideoListener photoVideoListener) {
        this.mPhotoVideoListener = photoVideoListener;
    }

    public void setOperaeListener(OperaeListener mOperaeListener) {
        this.mOperaeListener = mOperaeListener;
    }

    // endregion

    public ViewHolder getViewHolder() {
        return mViewHolder;
    }

    public ViewHolder mViewHolder; // 控件集合

    private int mLayoutWidth; // 该布局宽度
    private int mLayoutHeight; // 该布局高度

    private boolean mIsFirst = true; // 是否第一次

    public PhotoVideoLayout(@NonNull Context context) {
        this(context, null);
    }

    public PhotoVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mLayoutWidth = DisplayMetricsSPUtils.getScreenWidth(context);
        int mButtonSize = (int) (mLayoutWidth / 4.5f);  // 中心的按钮大小
        mLayoutHeight = mButtonSize + (mButtonSize / 5) * 2 + 100;
        initView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mLayoutWidth, mLayoutHeight);
    }

    /**
     * 初始化view
     */
    private void initView() {
        // 自定义View中如果重写了onDraw()即自定义了绘制，那么就应该在构造函数中调用view的setWillNotDraw(false).
        setWillNotDraw(false);

        mViewHolder = new ViewHolder(View.inflate(getContext(), R.layout.layout_photovideo_zjh, this));
        mViewHolder.btnPhotoVideo.setRecordingListener(new PhotoVideoListener() {
            @Override
            public void actionDown() {
                if (mPhotoVideoListener != null) {
                    mPhotoVideoListener.actionDown();
                }
            }

            @Override
            public void takePictures() {
                if (mPhotoVideoListener != null) {
                    mPhotoVideoListener.takePictures();
                }
            }

            @Override
            public void recordShort(long time) {
                if (mPhotoVideoListener != null) {
                    mPhotoVideoListener.recordShort(time);
                }
                startTipAlphaAnimation();
            }

            @Override
            public void recordStart() {
                if (mPhotoVideoListener != null) {
                    mPhotoVideoListener.recordStart();
                }
                startTipAlphaAnimation();
            }

            @Override
            public void recordEnd(long time) {
                if (mPhotoVideoListener != null) {
                    mPhotoVideoListener.recordEnd(time);
                }
                startTipAlphaAnimation();
                startOperaeBtnAnimator();
            }

            @Override
            public void recordZoom(float zoom) {
                if (mPhotoVideoListener != null) {
                    mPhotoVideoListener.recordZoom(zoom);
                }
            }

            @Override
            public void recordError() {
                if (mPhotoVideoListener != null) {
                    mPhotoVideoListener.recordError();
                }
            }
        });

        // 录像返回
        mViewHolder.btnCancel.setOnClickListener(v -> {
            if (mOperaeListener != null)
                mOperaeListener.cancel();
            startTipAlphaAnimation();
        });

        // 录像，拍照，提交事件
        mViewHolder.btnConfirm.setOnClickListener(v -> {
            if (mOperaeListener != null)
                mOperaeListener.confirm();
            startTipAlphaAnimation();
        });

        // 默认隐藏
        mViewHolder.btnCancel.setVisibility(GONE);
        mViewHolder.btnConfirm.setVisibility(GONE);
    }

    /**
     * 拍照录制结果后的动画 - 单图片
     */
    public void startOperaeBtnAnimator() {
        // 隐藏中间的按钮
        mViewHolder.btnPhotoVideo.setVisibility(INVISIBLE);
        // 显示提交和取消按钮
        mViewHolder.btnConfirm.setVisibility(VISIBLE);
        mViewHolder.btnCancel.setVisibility(VISIBLE);
        // 动画未结束前不能让它们点击
        mViewHolder.btnConfirm.setClickable(false);
        mViewHolder.btnCancel.setClickable(false);

        // 显示动画
        ObjectAnimator animatorConfirm = ObjectAnimator.ofFloat(mViewHolder.btnConfirm, "translationX", -mLayoutWidth / 4, 0);
        ObjectAnimator animatorCancel = ObjectAnimator.ofFloat(mViewHolder.btnCancel, "translationX", mLayoutWidth / 4, 0);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorCancel, animatorConfirm);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 动画结束使得按钮可点击
                mViewHolder.btnConfirm.setClickable(true);
                mViewHolder.btnCancel.setClickable(true);
            }
        });
        set.setDuration(200);
        set.start();
    }

    /**
     * 拍照录制结果后的动画 - 多图片
     */
    public void startOperaeBtnAnimatorMulti() {
        // 如果本身隐藏的，就显示出来
        if (mViewHolder.btnConfirm.getVisibility() == View.GONE){
            // 显示提交按钮
            mViewHolder.btnConfirm.setVisibility(VISIBLE);
            // 动画未结束前不能让它们点击
            mViewHolder.btnConfirm.setClickable(false);

            // 显示动画
            ObjectAnimator animatorConfirm = ObjectAnimator.ofFloat(mViewHolder.btnConfirm, "translationX", -mLayoutWidth / 4, 0);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(animatorConfirm);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    // 动画结束使得按钮可点击
                    mViewHolder.btnConfirm.setClickable(true);
                }
            });
            set.setDuration(200);
            set.start();
        }
    }

    // region 对外提供的api

    /**
     * 设置提示文本
     *
     * @param tip 提示文本
     */
    public void setTip(String tip) {
        mViewHolder.tvTip.setText(tip);
    }

    /**
     * 提示文本框 - 浮现渐现动画
     */
    public void startTipAlphaAnimation() {
        if (mIsFirst) {
            ObjectAnimator animatorTxtTip = ObjectAnimator.ofFloat(mViewHolder.tvTip, "alpha", 1f, 0f);
            animatorTxtTip.setDuration(500);
            animatorTxtTip.start();
            mIsFirst = false;
        }
    }

    /**
     * 提示文本框 - 浮现渐现动画，显示新的文字
     *
     * @param tip 提示文字
     */
    public void setTipAlphaAnimation(String tip) {
        mViewHolder.tvTip.setText(tip);
        ObjectAnimator animatorTxtTip = ObjectAnimator.ofFloat(mViewHolder.tvTip, "alpha", 0f, 1f, 1f, 0f);
        animatorTxtTip.setDuration(2500);
        animatorTxtTip.start();
    }

    /**
     * 设置拍照按钮 最长录制时间
     *
     * @param duration 时间秒
     */
    public void setDuration(int duration) {
        mViewHolder.btnPhotoVideo.setDuration(duration);
    }

    /**
     * 重置本身
     */
    public void reset() {
        mViewHolder.btnPhotoVideo.resetState();
        // 隐藏第二层的view
        mViewHolder.btnCancel.setVisibility(View.GONE);
        mViewHolder.btnConfirm.setVisibility(View.GONE);
        // 显示第一层的view
        mViewHolder.btnPhotoVideo.setVisibility(View.VISIBLE);
    }

    /**
     * 设置按钮支持的功能：
     *
     * @param buttonStateBoth {@link Constants#BUTTON_STATE_ONLY_CAPTURE 只能拍照
     * @link Constants#BUTTON_STATE_ONLY_RECORDER 只能录像
     * @link Constants#BUTTON_STATE_BOTH 两者皆可
     * }
     */
    public void setButtonFeatures(int buttonStateBoth) {
        mViewHolder.btnPhotoVideo.setButtonFeatures(buttonStateBoth);
    }

    public static class ViewHolder {
        View rootView;
        OperaeButton btnCancel;
        public OperaeButton btnConfirm;
//        public PhotoVideoButton btnPhotoVideo;
        public RecordButton btnPhotoVideo;
        TextView tvTip;

        ViewHolder(View rootView) {
            this.rootView = rootView;
            this.btnCancel = rootView.findViewById(R.id.btnCancel);
            this.btnConfirm = rootView.findViewById(R.id.btnConfirm);
            this.btnPhotoVideo = rootView.findViewById(R.id.btnPhotoVideo);
            this.tvTip = rootView.findViewById(R.id.tvTip);
        }

    }

    // endregion


}
