package com.zhongjh.cameraviewsoundrecorder.camera;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.camera.other.CameraCallback;
import com.zhongjh.cameraviewsoundrecorder.common.Constants;
import com.zhongjh.cameraviewsoundrecorder.entity.CameraButton;
import com.zhongjh.cameraviewsoundrecorder.listener.PhotoVideoListener;
import com.zhongjh.cameraviewsoundrecorder.util.DisplayMetricsSPUtils;
import com.zhongjh.cameraviewsoundrecorder.widget.FoucsView;
import com.zhongjh.cameraviewsoundrecorder.widget.PhotoVideoLayout;

/**
 * 一个全局界面，包含了 右上角的闪光灯、前/后置摄像头的切换、底部按钮功能、对焦框等、显示当前拍照和摄像的界面
 * Created by zhongjh on 2018/7/23.
 */
public class CameraLayout extends FrameLayout implements CameraCallback.CameraOpenOverCallback, SurfaceHolder
        .Callback, CameraContact.CameraView {

    private Context mContext;
    private CameraPresenter mCameraPresenter;  //控制层

    private int mState = Constants.STATE_PREVIEW;// 当前活动状态，默认休闲

    private int mFlashType = Constants.TYPE_FLASH_OFF;  // 闪关灯状态 默认关闭

    private int mLayoutWidth; // 整体宽度
    private float mScreenProp = 0f; // 当前录视频的高/宽的比例
    private int mZoomGradient = 0;  //缩放梯度 @@

    private ViewHolder mViewHolder; // 当前界面的所有控件

    private Bitmap mCaptureBitmap;   // 捕获的图片
    private Bitmap firstFrame;       // 第一帧图片
    private String videoUrl;         // 视频URL

    private CameraButton mCameraButton; // 摄像头按钮

    public CameraLayout(@NonNull Context context) {
        super(context);
    }

    public CameraLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        // 获取属性
        TypedArray cameraViewTypedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CameraView, defStyleAttr, 0);
        mCameraButton = new CameraButton();
        mCameraButton.setIconSize(cameraViewTypedArray.getDimensionPixelSize(R.styleable.CameraView_iconSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 35, getResources().getDisplayMetrics())));
        mCameraButton.setIconMargin(cameraViewTypedArray.getDimensionPixelSize(R.styleable.CameraView_iconMargin, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics())));
        mCameraButton.setIconSrc(cameraViewTypedArray.getResourceId(R.styleable.CameraView_iconSrc, R.drawable.ic_camera));
        mCameraButton.setIconLeft(cameraViewTypedArray.getResourceId(R.styleable.CameraView_iconLeft, 0));
        mCameraButton.setIconRight(cameraViewTypedArray.getResourceId(R.styleable.CameraView_iconRight, 0));
        mCameraButton.setDuration(cameraViewTypedArray.getInteger(R.styleable.CameraView_duration_max, 10 * 1000));
        // google建议回收对象
        cameraViewTypedArray.recycle();
        initData();
        initView();
        initLisenter();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float widthSize = mViewHolder.vvPreview.getMeasuredWidth();
        float heightSize =  mViewHolder.vvPreview.getMeasuredHeight();
        if (mScreenProp == 0) {
            mScreenProp = heightSize / widthSize;
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mLayoutWidth = DisplayMetricsSPUtils.getScreenWidth(mContext);
        //缩放梯度
        mZoomGradient = (int) (mLayoutWidth / 16f);
        mCameraPresenter = new CameraPresenter(getContext(), this);
    }

    /**
     * 初始化view
     */
    private void initView() {
        // 自定义View中如果重写了onDraw()即自定义了绘制，那么就应该在构造函数中调用view的setWillNotDraw(false).
        setWillNotDraw(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.main_view, this);
        mViewHolder = new ViewHolder(view);
        setFlashLamp(); // 设置闪光灯模式
        mViewHolder.pvLayout.setDuration(mCameraButton.getDuration());
        mViewHolder.pvLayout.setIconSrc(mCameraButton.getIconLeft(), mCameraButton.getIconRight());
    }

    /**
     * 初始化有关事件
     */
    private void initLisenter() {
        // 切换闪光灯模式
        mViewHolder.imgFlash.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mFlashType++;
                if (mFlashType > Constants.TYPE_FLASH_OFF)
                    mFlashType = Constants.TYPE_FLASH_AUTO;
                // 重新设置当前闪光灯模式
                setFlashLamp();
            }
        });

        // 录像机的回调事件
        mViewHolder.vvPreview.getHolder().addCallback(this);

        // 切换摄像头前置/后置
        mViewHolder.imgSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraPresenter.swtich(mViewHolder.vvPreview.getHolder(), mScreenProp);
            }
        });

        // 拍照录像监听
        mViewHolder.pvLayout.setPhotoVideoListener(new PhotoVideoListener() {
            @Override
            public void takePictures() {
                // 拍照  隐藏 闪光灯、右上角的切换摄像头
                mViewHolder.imgSwitch.setVisibility(INVISIBLE);
                mViewHolder.imgFlash.setVisibility(INVISIBLE);
                mCameraPresenter.capture();
            }

            @Override
            public void recordShort(final long time) {
                mViewHolder.pvLayout.setTipAlphaAnimation(getResources().getString(R.string.the_recording_time_is_too_short));
                mViewHolder.imgSwitch.setVisibility(VISIBLE);
                mViewHolder.imgFlash.setVisibility(VISIBLE);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCameraPresenter.stopRecord(true, time);
                    }
                }, 1500 - time);
            }

            @Override
            public void recordStart() {
                // 开始录像
                mViewHolder.imgSwitch.setVisibility(INVISIBLE);
                mViewHolder.imgFlash.setVisibility(INVISIBLE);
                mCameraPresenter.record(mViewHolder.vvPreview.getHolder().getSurface(), mScreenProp);
            }

            @Override
            public void recordEnd(long time) {

            }

            @Override
            public void recordZoom(float zoom) {

            }

            @Override
            public void recordError() {

            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void cameraHasOpened() {

    }

    @Override
    public void resetState(int type) {

    }

    @Override
    public void confirmState(int type) {

    }

    @Override
    public void showPicture(Bitmap bitmap, boolean isVertical) {
        if (isVertical) {
            mViewHolder.imgPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            mViewHolder.imgPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        mCaptureBitmap = bitmap;
        mViewHolder.imgPhoto.setImageBitmap(bitmap);
        mViewHolder.imgPhoto.setVisibility(VISIBLE);
        mViewHolder.pvLayout.startTipAlphaAnimation();
        mViewHolder.pvLayout.startOperaeBtnAnimator();
    }

    @Override
    public void playVideo(Bitmap firstFrame, String url) {

    }

    @Override
    public void stopVideo() {

    }

    @Override
    public void setTip(String tip) {

    }

    @Override
    public void startPreviewCallback() {

    }

    @Override
    public boolean handlerFoucs(float x, float y) {
        return false;
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public void setState(int state) {
        this.mState = state;
    }

    /**
     * 设置闪关灯
     */
    private void setFlashLamp() {
        switch (mFlashType) {
            case Constants.TYPE_FLASH_AUTO:
                mViewHolder.imgFlash.setImageResource(R.drawable.ic_flash_auto);
                mCameraPresenter.flash(Camera.Parameters.FLASH_MODE_AUTO);
                break;
            case Constants.TYPE_FLASH_ON:
                mViewHolder.imgFlash.setImageResource(R.drawable.ic_flash_on);
                mCameraPresenter.flash(Camera.Parameters.FLASH_MODE_ON);
                break;
            case Constants.TYPE_FLASH_OFF:
                mViewHolder.imgFlash.setImageResource(R.drawable.ic_flash_off);
                mCameraPresenter.flash(Camera.Parameters.FLASH_MODE_OFF);
                break;
        }
    }

    public static class ViewHolder {
        public View rootView;
        public VideoView vvPreview;
        public ImageView imgPhoto;
        public ImageView imgFlash;
        public ImageView imgSwitch;
        public PhotoVideoLayout pvLayout;
        public FoucsView fouce_view;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.vvPreview = rootView.findViewById(R.id.vvPreview);
            this.imgPhoto = rootView.findViewById(R.id.imgPhoto);
            this.imgFlash = rootView.findViewById(R.id.imgFlash);
            this.imgSwitch = rootView.findViewById(R.id.imgSwitch);
            this.pvLayout = rootView.findViewById(R.id.pvLayout);
            this.fouce_view = rootView.findViewById(R.id.fouce_view);
        }

    }
}
