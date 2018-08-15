package com.zhongjh.cameraviewsoundrecorder.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.listener.OperaeListener;
import com.zhongjh.cameraviewsoundrecorder.listener.PhotoVideoListener;
import com.zhongjh.cameraviewsoundrecorder.util.DisplayMetricsSPUtils;

/**
 * 关于底部集成各个控件的布局
 * Created by zhongjh on 2018/8/7.
 */
public class PhotoVideoLayout extends FrameLayout {

    // region 回调事件监听

    private PhotoVideoListener mPhotoVideoListener;   // 拍照或录制监听
    private OperaeListener mOperaeListener; // 拍照或录制监听结束后的 确认取消事件监控
    private View.OnClickListener mLeftClickListener; // 左边按钮监听
    private View.OnClickListener mRightClickListener; // 右边按钮监听

    public void setPhotoVideoListener(PhotoVideoListener photoVideoListener) {
        this.mPhotoVideoListener = photoVideoListener;
    }

    public void setOperaeListener(OperaeListener mOperaeListener) {
        this.mOperaeListener = mOperaeListener;
    }

    public void setLeftClickListener(OnClickListener leftClickListener) {
        this.mLeftClickListener = leftClickListener;
    }

    public void setRightClickListener(OnClickListener rightClickListener) {
        this.mRightClickListener = rightClickListener;
    }

    // endregion

    private PhotoVideoButton mBtnPhotoVideo;    //拍照按钮
    private OperaeButton mBtnCancel;            // 取消按钮
    private OperaeButton mBtnConfirm;           // 确认按钮
    private DownView mBtnReturn;                // 返回按钮
    private ImageView mImgCustomLeft;           //左边自定义按钮
    private ImageView mImgCustomRight;          //右边自定义按钮
    private TextView mTvTip;                   // 浮現在拍照按鈕上面的一个提示文本

    private int mLayoutWidth; // 该布局宽度
    private int mLayoutHeight; // 该布局高度
    private int mButtonSize; // 中心的按钮大小
    private int mIconLeft = 0; // 左图标的资源id
    private int mIconRight = 0;// 右图标的资源id

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
        mButtonSize = (int) (mLayoutWidth / 4.5f);
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

        // 提示文本
        mTvTip = new TextView(getContext());
        LayoutParams txtParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        txtParam.gravity = Gravity.CENTER_HORIZONTAL;
        txtParam.setMargins(0, 0, 0, 0);
        mTvTip.setText(R.string.light_touch_take_long_press_camera);
        mTvTip.setTextColor(0xFFFFFFFF);
        mTvTip.setGravity(Gravity.CENTER);
        mTvTip.setLayoutParams(txtParam);

        // region 拍照录制前的功能按钮

        //拍照按钮
        mBtnPhotoVideo = new PhotoVideoButton(getContext(), mButtonSize);
        LayoutParams photoVideoParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        photoVideoParam.gravity = Gravity.CENTER;
        mBtnPhotoVideo.setLayoutParams(photoVideoParam);
        mBtnPhotoVideo.setRecordingListener(new PhotoVideoListener() {
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

        // 左边返回按钮，如果没有自定义，就使用当前这个
        mBtnReturn = new DownView(getContext(), (int) (mButtonSize / 2.5f));
        LayoutParams btnReturnParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        btnReturnParam.gravity = Gravity.CENTER_VERTICAL;
        btnReturnParam.setMargins(mLayoutWidth / 6, 0, 0, 0);
        mBtnReturn.setLayoutParams(btnReturnParam);
        mBtnReturn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLeftClickListener != null)
                    mLeftClickListener.onClick(v);
            }
        });
        // 左边自定义按钮，如果自定义，就隐藏上面，使用当前这个
        mImgCustomLeft = new ImageView(getContext());
        LayoutParams imgLeftParam = new LayoutParams((int) (mButtonSize / 2.5f), (int) (mButtonSize / 2.5f));
        imgLeftParam.gravity = Gravity.CENTER_VERTICAL;
        imgLeftParam.setMargins(mLayoutWidth / 6, 0, 0, 0);
        mImgCustomLeft.setLayoutParams(imgLeftParam);
        mImgCustomLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLeftClickListener != null) {
                    mLeftClickListener.onClick(v);
                }
            }
        });

        // 右边自定义按钮
        mImgCustomRight = new ImageView(getContext());
        LayoutParams imgRightParam = new LayoutParams((int) (mButtonSize / 2.5f), (int) (mButtonSize / 2.5f));
        imgRightParam.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        imgRightParam.setMargins(0, 0, mLayoutWidth / 6, 0);
        mImgCustomRight.setLayoutParams(imgRightParam);
        mImgCustomRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRightClickListener != null) {
                    mRightClickListener.onClick(v);
                }
            }
        });

        // endregion

        // region 拍照或者录制完后，显示的按钮

        // 左侧的取消按钮
        mBtnCancel = new OperaeButton(getContext(), OperaeButton.TYPE_CANCEL, mButtonSize);
        LayoutParams btnCancelParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        btnCancelParams.gravity = Gravity.CENTER_VERTICAL;
        btnCancelParams.setMargins((mLayoutWidth / 4) - mButtonSize / 2, 0, 0, 0); // 左边间隔
        mBtnCancel.setLayoutParams(btnCancelParams);
        mBtnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOperaeListener != null)
                    mOperaeListener.cancel();
                startTipAlphaAnimation();
            }
        });

        // 右侧的确认按钮
        mBtnConfirm = new OperaeButton(getContext(), OperaeButton.TYPE_CONFIRM, mButtonSize);
        LayoutParams btnConfirmParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        btnConfirmParams.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        btnConfirmParams.setMargins(0, 0, (mLayoutWidth / 4) - mButtonSize / 2, 0);
        mBtnConfirm.setLayoutParams(btnConfirmParams);
        mBtnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOperaeListener != null)
                    mOperaeListener.confirm();
                startTipAlphaAnimation();
            }
        });


        // endregion

        this.addView(mBtnPhotoVideo);
        this.addView(mBtnCancel);
        this.addView(mBtnConfirm);
        this.addView(mBtnReturn);
        this.addView(mImgCustomLeft);
        this.addView(mImgCustomRight);
        this.addView(mTvTip);

        // 拍照或录制完的按钮 默认隐藏
        mImgCustomRight.setVisibility(GONE);
        mBtnCancel.setVisibility(GONE);
        mBtnConfirm.setVisibility(GONE);
    }

    /**
     * 拍照录制结果后的动画
     */
    public void startOperaeBtnAnimator() {
        // 如果左按钮有资源id
        if (this.mIconLeft != 0)
            // 就隐藏自定义的左图片
            mImgCustomLeft.setVisibility(GONE);
        else
            // 否则隐藏左按钮
            mBtnReturn.setVisibility(GONE);
        // 如果右按钮有资源id就隐藏自定义的右图片
        if (this.mIconRight != 0)
            mImgCustomRight.setVisibility(GONE);
        // 隐藏中间的按钮
        mBtnPhotoVideo.setVisibility(GONE);
        // 显示提交和取消按钮
        mBtnConfirm.setVisibility(VISIBLE);
        mBtnCancel.setVisibility(VISIBLE);
        // 动画未结束前不能让它们点击
        mBtnConfirm.setClickable(false);
        mBtnCancel.setClickable(false);

        // 显示动画
        ObjectAnimator animatorConfirm = ObjectAnimator.ofFloat(mBtnConfirm, "translationX", -mLayoutWidth / 4, 0);
        ObjectAnimator animatorCancel = ObjectAnimator.ofFloat(mBtnCancel, "translationX", mLayoutWidth / 4, 0);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorCancel, animatorConfirm);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 动画结束使得按钮可点击
                mBtnConfirm.setClickable(true);
                mBtnCancel.setClickable(true);
            }
        });
        set.setDuration(200);
        set.start();
    }

    // region 对外提供的api

    /**
     * 提示文本框 - 浮现渐现动画
     */
    public void startTipAlphaAnimation() {
        if (mIsFirst) {
            ObjectAnimator animatorTxtTip = ObjectAnimator.ofFloat(mTvTip, "alpha", 1f, 0f);
            animatorTxtTip.setDuration(500);
            animatorTxtTip.start();
            mIsFirst = false;
        }
    }

    /**
     * 提示文本框 - 浮现渐现动画，显示新的文字
     * @param tip 提示文字
     */
    public void setTipAlphaAnimation(String tip) {
        mTvTip.setText(tip);
        ObjectAnimator animatorTxtTip = ObjectAnimator.ofFloat(mTvTip, "alpha", 0f, 1f, 1f, 0f);
        animatorTxtTip.setDuration(2500);
        animatorTxtTip.start();
    }

    /**
     * 设置拍照按钮 最长录制时间
     *
     * @param duration 时间秒
     */
    public void setDuration(int duration) {
        mBtnPhotoVideo.setDuration(duration);
    }

    /**
     * 设置自定义的按钮图片 - 拍摄录像前的按钮图片
     *
     * @param iconLeft  自定义的左边图片
     * @param iconRight 自定义的右边图片
     */
    public void setIconSrc(int iconLeft, int iconRight) {
        this.mIconLeft = iconLeft;
        this.mIconRight = iconRight;
        // 设置左边的图片，如果有自定义图片，就隐藏自己画的返回按钮
        if (this.mIconLeft != 0) {
            this.mImgCustomLeft.setImageResource(iconLeft);
            this.mImgCustomLeft.setVisibility(VISIBLE);
            this.mBtnReturn.setVisibility(GONE);
        } else {
            this.mImgCustomLeft.setVisibility(GONE);
            this.mBtnReturn.setVisibility(VISIBLE);
        }
        // 设置右边的图片
        if (this.mIconRight != 0) {
            this.mImgCustomRight.setImageResource(iconRight);
            this.mImgCustomRight.setVisibility(VISIBLE);
        } else {
            this.mImgCustomRight.setVisibility(GONE);
        }
    }

    // endregion


}
