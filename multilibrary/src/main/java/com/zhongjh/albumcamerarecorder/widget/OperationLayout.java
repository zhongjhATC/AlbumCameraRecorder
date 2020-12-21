package com.zhongjh.albumcamerarecorder.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.common.Constants;
import com.zhongjh.albumcamerarecorder.camera.listener.ClickOrLongListener;
import com.zhongjh.albumcamerarecorder.camera.util.DisplayMetricsSPUtils;
import com.zhongjh.albumcamerarecorder.widget.clickorlongbutton.ClickOrLongButton;

/**
 * 集成各个控件的布局
 * {@link com.zhongjh.albumcamerarecorder.widget.clickorlongbutton.ClickOrLongButton 点击或者长按的按钮 }
 * {@link com.zhongjh.albumcamerarecorder.widget.OperationButton 操作按钮(取消和确认) }
 * Created by zhongjh on 2018/8/7.
 */
public abstract class OperationLayout extends FrameLayout {

    // region 回调事件监听

    private ClickOrLongListener mClickOrLongListener;   // 点击或长按监听
    private OperaeListener mOperaeListener; // 点击或长按监听结束后的 确认取消事件监控
    /**
     * 操作按钮的Listener
     */
    public interface OperaeListener {

        void cancel();

        void confirm();

    }

    public void setPhotoVideoListener(ClickOrLongListener clickOrLongListener) {
        this.mClickOrLongListener = clickOrLongListener;
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

    protected abstract ViewHolder newViewHolder();

    public OperationLayout(@NonNull Context context) {
        this(context, null);
    }

    public OperationLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OperationLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

        mViewHolder = newViewHolder();
        mViewHolder.btnClickOrLong.setRecordingListener(new ClickOrLongListener() {
            @Override
            public void actionDown() {
                if (mClickOrLongListener != null) {
                    mClickOrLongListener.actionDown();
                }
            }

            @Override
            public void onClick() {
                if (mClickOrLongListener != null) {
                    mClickOrLongListener.onClick();
                }
            }

            @Override
            public void onLongClickShort(long time) {
                if (mClickOrLongListener != null) {
                    mClickOrLongListener.onLongClickShort(time);
                }
                startTipAlphaAnimation();
            }

            @Override
            public void onLongClick() {
                if (mClickOrLongListener != null) {
                    mClickOrLongListener.onLongClick();
                }
                startTipAlphaAnimation();
            }

            @Override
            public void onLongClickEnd(long time) {
                if (mClickOrLongListener != null) {
                    mClickOrLongListener.onLongClickEnd(time);
                }
                startTipAlphaAnimation();
                startOperaeBtnAnimator();
            }

            @Override
            public void onLongClickError() {
                if (mClickOrLongListener != null) {
                    mClickOrLongListener.onLongClickError();
                }
            }
        });

        // 返回事件
        mViewHolder.btnCancel.setOnClickListener(v -> {
            if (mOperaeListener != null)
                mOperaeListener.cancel();
            startTipAlphaAnimation();
        });

        // 提交事件
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
     * 点击长按结果后的动画 - 单图片
     */
    public void startOperaeBtnAnimator() {
        // 隐藏中间的按钮
        mViewHolder.btnClickOrLong.setVisibility(INVISIBLE);
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
     * 拍点击长按结果后的动画 - 多图片
     */
    public void startOperaeBtnAnimatorMulti() {
        // 如果本身隐藏的，就显示出来
        if (mViewHolder.btnConfirm.getVisibility() == View.GONE) {
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
     * 设置按钮 最长长按时间
     *
     * @param duration 时间秒
     */
    public void setDuration(int duration) {
        mViewHolder.btnClickOrLong.setDuration(duration);
    }

    /**
     * 最短录制时间
     *
     * @param duration 时间
     */
    public void setMinDuration(int duration){
        mViewHolder.btnClickOrLong.setMinDuration(duration);
    }

    /**
     * 重置本身
     */
    public void reset() {
        mViewHolder.btnClickOrLong.resetState();
        // 隐藏第二层的view
        mViewHolder.btnCancel.setVisibility(View.GONE);
        mViewHolder.btnConfirm.setVisibility(View.GONE);
        // 显示第一层的view
        mViewHolder.btnClickOrLong.setVisibility(View.VISIBLE);
    }

    /**
     * 设置按钮支持的功能：
     *
     * @param buttonStateBoth {@link Constants#BUTTON_STATE_ONLY_CLICK 只能点击
     * @link Constants#BUTTON_STATE_ONLY_LONGCLICK 只能长按
     * @link Constants#BUTTON_STATE_BOTH 两者皆可
     * }
     */
    public void setButtonFeatures(int buttonStateBoth) {
        mViewHolder.btnClickOrLong.setButtonFeatures(buttonStateBoth);
    }

    public class ViewHolder {
        View rootView;
        OperationButton btnCancel;
        public OperationButton btnConfirm;
        public ClickOrLongButton btnClickOrLong;
        TextView tvTip;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.btnCancel = rootView.findViewById(R.id.btnCancel);
            this.btnConfirm = rootView.findViewById(R.id.btnConfirm);
            this.btnClickOrLong = rootView.findViewById(R.id.btnClickOrLong);
            this.tvTip = rootView.findViewById(R.id.tvTip);
        }

    }

    // endregion


}
