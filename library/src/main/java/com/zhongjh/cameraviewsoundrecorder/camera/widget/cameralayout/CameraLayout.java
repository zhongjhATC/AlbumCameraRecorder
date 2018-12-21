package com.zhongjh.cameraviewsoundrecorder.camera.widget.cameralayout;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.zhongjh.cameraviewsoundrecorder.R;
import com.zhongjh.cameraviewsoundrecorder.camera.entity.BitmapData;
import com.zhongjh.cameraviewsoundrecorder.settings.SelectionSpec;
import com.zhongjh.cameraviewsoundrecorder.camera.common.Constants;
import com.zhongjh.cameraviewsoundrecorder.camera.entity.CameraButton;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.CaptureListener;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.CloseListener;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.ErrorListener;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.OperaeCameraListener;
import com.zhongjh.cameraviewsoundrecorder.camera.listener.ClickOrLongListener;
import com.zhongjh.cameraviewsoundrecorder.camera.util.DisplayMetricsSPUtils;
import com.zhongjh.cameraviewsoundrecorder.camera.util.FileUtil;
import com.zhongjh.cameraviewsoundrecorder.camera.util.LogUtil;
import com.zhongjh.cameraviewsoundrecorder.camera.widget.FoucsView;
import com.zhongjh.cameraviewsoundrecorder.utils.MediaStoreCompat;
import com.zhongjh.cameraviewsoundrecorder.widget.OperationLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.zhongjh.cameraviewsoundrecorder.album.model.SelectedItemCollection.COLLECTION_IMAGE;
import static com.zhongjh.cameraviewsoundrecorder.album.model.SelectedItemCollection.COLLECTION_UNDEFINED;
import static com.zhongjh.cameraviewsoundrecorder.album.model.SelectedItemCollection.COLLECTION_VIDEO;
import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.TYPE_DEFAULT;
import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.TYPE_PICTURE;
import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.TYPE_SHORT;
import static com.zhongjh.cameraviewsoundrecorder.camera.common.Constants.TYPE_VIDEO;

/**
 * 一个全局界面，包含了 右上角的闪光灯、前/后置摄像头的切换、底部按钮功能、对焦框等、显示当前拍照和摄像的界面
 * 该类类似MVP的View，主要包含有关 除了Camera的其他所有ui操作
 * Created by zhongjh on 2018/7/23.
 */
public class CameraLayout extends FrameLayout implements SurfaceHolder
        .Callback, CameraContact.CameraView {

    private Context mContext;
    private CameraPresenter mCameraPresenter;  //控制层
    private MediaStoreCompat mMediaStoreCompat;
    private SelectionSpec mSpec;

    private int mState = Constants.STATE_PREVIEW;// 当前活动状态，默认休闲

    private int mFlashType = Constants.TYPE_FLASH_OFF;  // 闪关灯状态 默认关闭


    private int mLayoutWidth; // 整体宽度
    private float mScreenProp = 0f; // 当前录视频的高/宽的比例
    private int mZoomGradient = 0;  //缩放梯度 @@

    public ViewHolder mViewHolder; // 当前界面的所有控件

    private MediaPlayer mMediaPlayer; // 播放器

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, BitmapData> mCaptureBitmaps = new HashMap<>();  // 拍照的图片-集合
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, View> mCaptureViews = new HashMap<>();      // 拍照的图片控件-集合
    private int mPosition = -1;                                          // 数据目前的最长索引，上面两个集合都是根据这个索引进行删除增加。这个索引只有递增没有递减

    private Bitmap mFirstFrame;       // 第一帧图片
    private String mVideoUrl;         // 视频URL

    private CameraButton mCameraButton; // 摄像头按钮

    private boolean mIsMultiPicture;    // 是否多张图片
    private int mCollectionType = COLLECTION_UNDEFINED; // 类型: 允许图片或者视频，跟知乎的选择相片共用模式

    // region 属性

    // 回调监听
    private ErrorListener mErrorLisenter;
    private CloseListener mCloseListener;           // 退出当前Activity的按钮监听
    private ClickOrLongListener mClickOrLongListener; // 按钮的监听
    private OperaeCameraListener mOperaeCameraListener;         // 确认跟返回的监听
    private CaptureListener mCaptureListener;       // 拍摄后操作图片的事件


    // 赋值Camera错误回调
    public void setErrorLisenter(ErrorListener errorLisenter) {
        this.mErrorLisenter = errorLisenter;
        mCameraPresenter.setErrorLinsenter(errorLisenter);
    }

    // 退出当前Activity的按钮监听
    public void setCloseListener(CloseListener closeListener) {
        this.mCloseListener = closeListener;
    }

    // 核心按钮事件
    public void setPhotoVideoListener(ClickOrLongListener clickOrLongListener) {
        this.mClickOrLongListener = clickOrLongListener;
    }

    // 确认跟返回的监听
    public void setOperaeCameraListener(OperaeCameraListener operaeCameraListener) {
        this.mOperaeCameraListener = operaeCameraListener;
    }

    // 拍摄后操作图片的事件
    public void setCaptureListener(CaptureListener captureListener) {
        this.mCaptureListener = captureListener;
    }

    /**
     * 设置是否一次性拍摄多张图片
     *
     * @param b 是否
     */
    @Override
    public void isMultiPicture(boolean b) {
        this.mIsMultiPicture = b;
    }

    /**
     * 设置类型 允许图片或者视频，跟知乎的选择相片共用模式
     *
     * @param mCollectionType 类型
     */
    public void setCollectionType(int mCollectionType) {
        this.mCollectionType = mCollectionType;
    }

    // endregion


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
        float heightSize = mViewHolder.vvPreview.getMeasuredHeight();
        if (mScreenProp == 0) {
            mScreenProp = heightSize / widthSize;
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 初始化设置
        mSpec = SelectionSpec.getInstance();
        if (mSpec.capture) {
            mMediaStoreCompat = new MediaStoreCompat(getContext());
            if (mSpec.captureStrategy == null)
                throw new RuntimeException("Don't forget to set CaptureStrategy.");
            mMediaStoreCompat.setCaptureStrategy(mSpec.captureStrategy);
        }

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
        View view = LayoutInflater.from(mContext).inflate(R.layout.main_view_zjh, this);
        mViewHolder = new ViewHolder(view);
        setFlashLamp(); // 设置闪光灯模式
        mViewHolder.pvLayout.setDuration(mCameraButton.getDuration());
    }

    /**
     * 初始化有关事件
     */
    private void initLisenter() {
        // 切换闪光灯模式
        mViewHolder.imgFlash.setOnClickListener(v -> {
            mFlashType++;
            if (mFlashType > Constants.TYPE_FLASH_OFF)
                mFlashType = Constants.TYPE_FLASH_AUTO;
            // 重新设置当前闪光灯模式
            setFlashLamp();
        });

        // 录像机的回调事件
        mViewHolder.vvPreview.getHolder().addCallback(this);

        // 切换摄像头前置/后置
        mViewHolder.imgSwitch.setOnClickListener(v -> mCameraPresenter.swtich(mViewHolder.vvPreview.getHolder(), mScreenProp));

        // 拍照录像监听
        mViewHolder.pvLayout.setPhotoVideoListener(new ClickOrLongListener() {
            @Override
            public void actionDown() {
                if (mClickOrLongListener != null)
                    mClickOrLongListener.actionDown();
            }

            @Override
            public void onClick() {
                // 判断数量
                if (mViewHolder.llPhoto.getChildCount() < currentMaxSelectable()) {
                    // 拍照  隐藏 闪光灯、右上角的切换摄像头
                    mViewHolder.imgSwitch.setVisibility(INVISIBLE);
                    mViewHolder.imgFlash.setVisibility(INVISIBLE);
                    mCameraPresenter.capture();
                    if (mClickOrLongListener != null)
                        mClickOrLongListener.onClick();
                } else {
                    Toast.makeText(mContext, "已经达到拍照上限", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLongClickShort(final long time) {
                mViewHolder.pvLayout.setTipAlphaAnimation(getResources().getString(R.string.the_recording_time_is_too_short));
                mViewHolder.imgSwitch.setVisibility(VISIBLE);
                mViewHolder.imgFlash.setVisibility(VISIBLE);
                postDelayed(() -> mCameraPresenter.stopRecord(true, time), 1500 - time);
                if (mClickOrLongListener != null)
                    mClickOrLongListener.onLongClickShort(time);
            }

            @Override
            public void onLongClick() {
                // 开始录像
                mViewHolder.imgSwitch.setVisibility(INVISIBLE);
                mViewHolder.imgFlash.setVisibility(INVISIBLE);
                mCameraPresenter.record(mViewHolder.vvPreview.getHolder().getSurface(), mScreenProp);
                if (mClickOrLongListener != null)
                    mClickOrLongListener.onLongClick();
            }

            @Override
            public void onLongClickEnd(long time) {
                // 录像结束
                mCameraPresenter.stopRecord(false, time);
                if (mClickOrLongListener != null)
                    mClickOrLongListener.onLongClickEnd(time);
            }

            @Override
            public void onLongClickZoom(float zoom) {
                mCameraPresenter.zoom(zoom, Constants.TYPE_RECORDER);
                if (mClickOrLongListener != null)
                    mClickOrLongListener.onLongClickZoom(zoom);
            }

            @Override
            public void onLongClickError() {
                if (mErrorLisenter != null) {
                    mErrorLisenter.AudioPermissionError();
                }
                if (mClickOrLongListener != null)
                    mClickOrLongListener.onLongClickError();
            }
        });

        // 确认和取消
        mViewHolder.pvLayout.setOperaeListener(new OperationLayout.OperaeListener() {
            @Override
            public void cancel() {
                mCameraPresenter.cancle(mViewHolder.vvPreview.getHolder(), mScreenProp);
                if (mOperaeCameraListener != null)
                    mOperaeCameraListener.cancel();
            }

            @Override
            public void confirm() {
                mCameraPresenter.confirm();
            }
        });

        // 关闭事件
        mViewHolder.imgClose.setOnClickListener(v -> {
            if (mCloseListener != null)
                mCloseListener.onClose();
        });

    }

    @Override
    public void onResume() {
        LogUtil.i("CameraLayout onResume");
        resetState(TYPE_DEFAULT); //重置状态
        mCameraPresenter.registerSensorManager(mContext);
        mCameraPresenter.setImageViewSwitchAndFlash(mViewHolder.imgSwitch, mViewHolder.imgFlash);
        mCameraPresenter.start(mViewHolder.vvPreview.getHolder(), mScreenProp);
    }

    @Override
    public void onPause() {
        LogUtil.i("CameraLayout onPause");
        stopVideo(); // 停止播放
        resetState(TYPE_PICTURE); // @@ 为什么重置为图片模式
        mCameraPresenter.isPreview(false); // 设置为不是录像状态
        mCameraPresenter.unregisterSensorManager(mContext);
    }

    /**
     * SurfaceView生命周期
     * 当Surface第一次创建后会立即调用该函数
     *
     * @param surfaceHolder holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        LogUtil.i("JCameraView SurfaceCreated");
        new Thread() {
            @Override
            public void run() {
                mCameraPresenter.doOpenCamera();
            }
        }.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 无任何事件
    }

    /**
     * SurfaceView生命周期
     * 当Surface销毁后调用该函数
     *
     * @param surfaceHolder surfaceHolder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCameraPresenter.doDestroyCamera();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    setFocusViewWidthAnimation(event.getX(), event.getY());
                }
                break;
        }
        return true;
    }

    @Override
    public void resetState(int type) {
        switch (type) {
            case TYPE_VIDEO:
                stopVideo();    //停止播放
                FileUtil.deleteFile(mVideoUrl); // 删除文件
                mViewHolder.vvPreview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));     //初始化VideoView
                mCameraPresenter.start(mViewHolder.vvPreview.getHolder(), mScreenProp);
                break;
            case TYPE_PICTURE:
                // 隐藏图片
                mViewHolder.imgPhoto.setVisibility(INVISIBLE);
                break;
            case TYPE_SHORT:
                // 短视屏不处理任何事情
                break;
            case TYPE_DEFAULT:
                mViewHolder.vvPreview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                break;
        }
        mViewHolder.imgSwitch.setVisibility(VISIBLE);
        mViewHolder.imgFlash.setVisibility(VISIBLE);
        mViewHolder.pvLayout.reset();
    }

    @Override
    public void confirmState(int type) {
        switch (type) {
            case TYPE_VIDEO:
                stopVideo();    //停止播放
                mViewHolder.vvPreview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                mCameraPresenter.start(mViewHolder.vvPreview.getHolder(), mScreenProp);
                if (mOperaeCameraListener != null) {
                    mOperaeCameraListener.recordSuccess(mVideoUrl, mFirstFrame);
                }
                break;
            case TYPE_PICTURE:
                mViewHolder.imgPhoto.setVisibility(INVISIBLE);
                if (mOperaeCameraListener != null) {
                    mOperaeCameraListener.captureSuccess(getPaths());
                }
                break;
            case TYPE_SHORT:
                break;
            case TYPE_DEFAULT:
                break;
        }
        mViewHolder.pvLayout.reset();
    }

    @Override
    public void showPicture(Bitmap bitmap, boolean isVertical) {
        // 存储到临时文件
//        BitmapUtils.saveToFile(bitmap,)

        // 初始化数据
        BitmapData bitmapData = new BitmapData(bitmap, mMediaStoreCompat.saveFileByBitmap(bitmap));

        // 判断是否多个图片
        if (mIsMultiPicture) {
            mPosition++;
            // 如果是多个图片，就把当前图片添加到集合并显示出来
            mCaptureBitmaps.put(mPosition, bitmapData);
            // 显示横版列表
            mViewHolder.hsvPhoto.setVisibility(View.VISIBLE);

            // 显示横版列表的线条空间
            mViewHolder.vLine1.setVisibility(View.VISIBLE);
            mViewHolder.vLine2.setVisibility(View.VISIBLE);

            // 添加view
            ViewHolderImageView viewHolderImageView = new ViewHolderImageView(View.inflate(getContext(), R.layout.item_horizontal_image_zjh, null));
            viewHolderImageView.imgPhoto.setImageBitmap(bitmap);
            viewHolderImageView.imgPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
            viewHolderImageView.imgCancel.setTag(mPosition);
            viewHolderImageView.imgCancel.setOnClickListener(v -> {
                // 删除
                BitmapData viewBitmap = mCaptureBitmaps.get(Integer.parseInt(v.getTag().toString()));
                // 先判断是否已经回收
                if (viewBitmap != null && !viewBitmap.getBitmap().isRecycled()) {
                    // 回收并且置为null
                    viewBitmap.getBitmap().recycle();
                    mCaptureBitmaps.remove(Integer.parseInt(v.getTag().toString()));
                }
                System.gc();// 加速回收机制
                mViewHolder.llPhoto.removeView(mCaptureViews.get(Integer.parseInt(v.getTag().toString())));

                // 回调接口：删除图片后剩下的相关数据
                mCaptureListener.remove(mCaptureBitmaps);

                // 当列表全部删掉的话，就隐藏
                if (mCaptureBitmaps.size() <= 0) {
                    // 显示横版列表
                    mViewHolder.hsvPhoto.setVisibility(View.GONE);

                    // 显示横版列表的线条空间
                    mViewHolder.vLine1.setVisibility(View.GONE);
                    mViewHolder.vLine2.setVisibility(View.GONE);

                    // 隐藏右侧按钮
                    mViewHolder.pvLayout.getViewHolder().btnConfirm.setVisibility(View.GONE);
                }
            });
            mCaptureViews.put(mPosition, viewHolderImageView.rootView);
            mViewHolder.llPhoto.addView(viewHolderImageView.rootView);
            mViewHolder.pvLayout.startTipAlphaAnimation();
            mViewHolder.pvLayout.startOperaeBtnAnimatorMulti();

            // 因为拍照后会自动停止预览，所以要重新启动预览
            mCameraPresenter.start(mViewHolder.vvPreview.getHolder(), mScreenProp);
            // 重置按钮，因为每次点击，都会自动关闭
            mViewHolder.pvLayout.getViewHolder().btnClickOrLong.resetState();
            // 依然保持当前模式
            setState(Constants.STATE_PREVIEW);
            // 显示右上角
            mViewHolder.imgSwitch.setVisibility(View.VISIBLE);
            mViewHolder.imgFlash.setVisibility(View.VISIBLE);
        } else {
            // 如果只有单个图片，就显示相应的提示结果等等
            if (isVertical) {
                mViewHolder.imgPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
            } else {
                mViewHolder.imgPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            mCaptureBitmaps.put(0, bitmapData);
            mViewHolder.imgPhoto.setImageBitmap(bitmap);
            mViewHolder.imgPhoto.setVisibility(VISIBLE);
            mViewHolder.pvLayout.startTipAlphaAnimation();
            mViewHolder.pvLayout.startOperaeBtnAnimator();

            // 设置当前模式是图片模式
            setState(Constants.STATE_PICTURE);
        }
        // 回调接口：添加图片后剩下的相关数据
        mCaptureListener.add(mCaptureBitmaps);
    }

    @Override
    public void playVideo(Bitmap firstFrame, String url) {
        mVideoUrl = url;
        mFirstFrame = firstFrame;
        new Thread(() -> {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            } else {
                // 重置
                mMediaPlayer.reset();
            }
            try {
                mMediaPlayer.setDataSource(mVideoUrl);
                // 进行关联播放控件
                mMediaPlayer.setSurface(mViewHolder.vvPreview.getHolder().getSurface());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    // 填充模式
                    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                }
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // 指定流媒体的类型
                // 视频尺寸监听
                mMediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> updateVideoViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer
                        .getVideoHeight()));
                mMediaPlayer.setOnPreparedListener(mp -> {
                    // 播放视频
                    mMediaPlayer.start();
                });
                mMediaPlayer.setLooping(true); // 循环播放
                mMediaPlayer.prepare(); // 准备(同步)
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void stopVideo() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void setTip(String tip) {
        mViewHolder.pvLayout.setTip(tip);
    }

    @Override
    public boolean handlerFoucs(float x, float y) {
        if (y > mViewHolder.pvLayout.getTop()) {
            return false;
        }
        // 显示焦点框
        mViewHolder.fouce_view.setVisibility(VISIBLE);
        if (x < mViewHolder.fouce_view.getWidth() / 2)
            x = mViewHolder.fouce_view.getWidth() / 2;
        if (x > mLayoutWidth - mViewHolder.fouce_view.getWidth() / 2)
            x = mLayoutWidth - mViewHolder.fouce_view.getWidth() / 2;
        if (y < mViewHolder.fouce_view.getWidth() / 2)
            y = mViewHolder.fouce_view.getWidth() / 2;
        if (y > mViewHolder.pvLayout.getTop() - mViewHolder.fouce_view.getWidth() / 2)
            y = mViewHolder.pvLayout.getTop() - mViewHolder.fouce_view.getWidth() / 2;
        mViewHolder.fouce_view.setX(x - mViewHolder.fouce_view.getWidth() / 2);
        mViewHolder.fouce_view.setY(y - mViewHolder.fouce_view.getHeight() / 2);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mViewHolder.fouce_view, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mViewHolder.fouce_view, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mViewHolder.fouce_view, "alpha", 1f, 0.4f, 1f, 0.4f, 1f, 0.4f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();
        return true;
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public void setState(int state) {
        this.mState = state;
    }

    @Override
    public float getScreenProp() {
        return mScreenProp;
    }

    @Override
    public SurfaceHolder getSurfaceHolder() {
        return mViewHolder.vvPreview.getHolder();
    }

    @Override
    public void setSaveVideoPath(String saveVideoPath) {
        mCameraPresenter.setSaveVideoPath(saveVideoPath);
    }

    @Override
    public void setFeatures(int buttonStateBoth) {
        mViewHolder.pvLayout.setButtonFeatures(buttonStateBoth);
    }

    @Override
    public void setMediaQuality(int mediaQualityMiddle) {
        mCameraPresenter.setMediaQuality(mediaQualityMiddle);
    }

    @Override
    public void setPictureMaxNumber(int i) {
        mCameraPresenter.setPictureMaxNumber(i);
    }

    /**
     * 对焦框指示器动画
     *
     * @param x 坐标x
     * @param y 坐标y
     */
    private void setFocusViewWidthAnimation(float x, float y) {
        if (handlerFoucs(x, y)) {
            mCameraPresenter.handleFocus(x, y, () -> mViewHolder.fouce_view.setVisibility(INVISIBLE));
        }
    }

    /**
     * 更新当前视频播放控件的宽高
     *
     * @param videoWidth  宽度
     * @param videoHeight 高度
     */
    private void updateVideoViewSize(float videoWidth, float videoHeight) {
        if (videoWidth > videoHeight) {
            LayoutParams videoViewParam;
            int height = (int) ((videoHeight / videoWidth) * getWidth());
            videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, height);
            videoViewParam.gravity = Gravity.CENTER;
            mViewHolder.vvPreview.setLayoutParams(videoViewParam);
        }
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

    /**
     * 返回最多选择的数量
     *
     * @return 数量
     */
    private int currentMaxSelectable() {
        SelectionSpec spec = SelectionSpec.getInstance();
        if (spec.maxSelectable > 0) {
            // 返回最大选择数量
            return spec.maxSelectable;
        } else if (mCollectionType == COLLECTION_IMAGE) {
            // 如果是图片类型，则返回最大图片选择数量
            return spec.maxImageSelectable;
        } else if (mCollectionType == COLLECTION_VIDEO) {
            // 如果是视频类型，则返回最大视频选择数量
            return spec.maxVideoSelectable;
        } else {
            // 返回最大选择数量
            return spec.maxSelectable;
        }
    }

    /**
     * 返回当前所有的路径
     */
    private ArrayList<String> getPaths() {
        ArrayList<String> paths = new ArrayList<>();
        for (BitmapData value : mCaptureBitmaps.values()) {
            paths.add(value.getPath());
        }
        return paths;
    }

    public static class ViewHolder {
        View rootView;
        VideoView vvPreview;
        ImageView imgPhoto;
        public ImageView imgFlash;
        public ImageView imgSwitch;
        public OperationLayout pvLayout;
        FoucsView fouce_view;
        public HorizontalScrollView hsvPhoto;
        LinearLayout llPhoto;
        View vLine1;
        View vLine2;
        View vLine3;
        ImageView imgClose;

        ViewHolder(View rootView) {
            this.rootView = rootView;
            this.vvPreview = rootView.findViewById(R.id.vvPreview);
            this.imgPhoto = rootView.findViewById(R.id.imgPhoto);
            this.imgFlash = rootView.findViewById(R.id.imgFlash);
            this.imgSwitch = rootView.findViewById(R.id.imgSwitch);
            this.pvLayout = rootView.findViewById(R.id.pvLayout);
            this.fouce_view = rootView.findViewById(R.id.fouceView);
            this.hsvPhoto = rootView.findViewById(R.id.hsvPhoto);
            this.llPhoto = rootView.findViewById(R.id.llPhoto);
            this.vLine1 = rootView.findViewById(R.id.vLine1);
            this.vLine2 = rootView.findViewById(R.id.vLine2);
            this.vLine3 = rootView.findViewById(R.id.vLine3);
            this.imgClose = rootView.findViewById(R.id.imgClose);
        }

    }

    public static class ViewHolderImageView {
        public View rootView;
        public ImageView imgPhoto;
        public ImageView imgCancel;

        public ViewHolderImageView(View rootView) {
            this.rootView = rootView;
            this.imgPhoto = rootView.findViewById(R.id.imgPhoto);
            this.imgCancel = rootView.findViewById(R.id.imgCancel);
        }

    }
}
