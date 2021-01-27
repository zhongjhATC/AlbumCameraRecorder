package com.zhongjh.albumcamerarecorder.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Preview;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.camera.common.Constants;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.camera.listener.CaptureListener;
import com.zhongjh.albumcamerarecorder.camera.listener.ClickOrLongListener;
import com.zhongjh.albumcamerarecorder.camera.listener.CloseListener;
import com.zhongjh.albumcamerarecorder.camera.listener.ErrorListener;
import com.zhongjh.albumcamerarecorder.camera.listener.OperaeCameraListener;
import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;
import com.zhongjh.albumcamerarecorder.camera.util.LogUtil;
import com.zhongjh.albumcamerarecorder.preview.AlbumPreviewActivity;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.settings.CameraSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.settings.RecordeSpec;
import com.zhongjh.albumcamerarecorder.utils.BitmapUtils;
import com.zhongjh.albumcamerarecorder.utils.PackageManagerUtils;
import com.zhongjh.albumcamerarecorder.widget.ChildClickableFrameLayout;
import com.zhongjh.albumcamerarecorder.widget.OperationLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;

import gaode.zhongjh.com.common.entity.MultiMedia;
import gaode.zhongjh.com.common.enums.MimeType;
import gaode.zhongjh.com.common.enums.MultimediaTypes;
import gaode.zhongjh.com.common.utils.MediaStoreCompat;

import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.COLLECTION_IMAGE;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_COLLECTION_TYPE;
import static com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection.STATE_SELECTION;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.BUTTON_STATE_BOTH;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.BUTTON_STATE_ONLY_CLICK;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.BUTTON_STATE_ONLY_LONGCLICK;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.TYPE_DEFAULT;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.TYPE_PICTURE;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.TYPE_SHORT;
import static com.zhongjh.albumcamerarecorder.camera.common.Constants.TYPE_VIDEO;
import static com.zhongjh.albumcamerarecorder.utils.constants.Constant.REQUEST_CODE_PREVIEW_CAMRRA;

/**
 * 一个全局界面，包含了 右上角的闪光灯、前/后置摄像头的切换、底部按钮功能、对焦框等、显示当前拍照和摄像的界面
 * 该类类似MVP的View，主要包含有关 除了Camera的其他所有ui操作
 * Created by zhongjh on 2018/7/23.
 */
public class CameraLayout extends RelativeLayout {

    private final String TAG = CameraLayout.class.getSimpleName();

    private final Context mContext;
    private MediaStoreCompat mPictureMediaStoreCompat;  // 图片
    private MediaStoreCompat mVideoMediaStoreCompat; // 录像文件配置路径
    private GlobalSpec mGlobalSpec; // 公共配置
    private CameraSpec mCameraSpec; // 拍摄配置
    private RecordeSpec mRecordeSpec; // 录像配置

    public int mState = Constants.STATE_PREVIEW;// 当前活动状态，默认休闲

    private int mFlashType = Constants.TYPE_FLASH_OFF;  // 闪关灯状态 默认关闭

    public ViewHolder mViewHolder; // 当前界面的所有控件

    private Drawable mPlaceholder; // 默认图片
    public LinkedHashMap<Integer, BitmapData> mCaptureBitmaps = new LinkedHashMap<>();  // 拍照的图片-集合
    private final LinkedHashMap<Integer, View> mCaptureViews = new LinkedHashMap<>();   // 拍照的图片控件-集合
    private int mPosition = -1; // 数据目前的最长索引，上面两个集合都是根据这个索引进行删除增加。这个索引只有递增没有递减
    private File mVideoFile;    // 视频File

    // region 回调监听属性
    private ErrorListener mErrorLisenter;
    private CloseListener mCloseListener;           // 退出当前Activity的按钮监听
    private ClickOrLongListener mClickOrLongListener; // 按钮的监听
    private OperaeCameraListener mOperaeCameraListener;         // 确认跟返回的监听
    private CaptureListener mCaptureListener;       // 拍摄后操作图片的事件
    private Fragment fragment;


    // 赋值Camera错误回调
    public void setErrorLisenter(ErrorListener errorLisenter) {
        this.mErrorLisenter = errorLisenter;
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

    // endregion

    public CameraLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initData();
        initView();
        initLisenter();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 初始化设置
        mRecordeSpec = RecordeSpec.getInstance();
        mCameraSpec = CameraSpec.getInstance();
        mGlobalSpec = GlobalSpec.getInstance();
        mPictureMediaStoreCompat = new MediaStoreCompat(getContext());
        mVideoMediaStoreCompat = new MediaStoreCompat(getContext());
        mVideoMediaStoreCompat.setSaveStrategy(mGlobalSpec.videoStrategy == null ? mGlobalSpec.saveStrategy : mGlobalSpec.videoStrategy);

        // 默认图片
        TypedArray ta = mContext.getTheme().obtainStyledAttributes(
                new int[]{R.attr.album_thumbnail_placeholder});
        mPlaceholder = ta.getDrawable(0);

        // 设置图片路径
        if (mGlobalSpec.pictureStrategy != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            mPictureMediaStoreCompat.setSaveStrategy(mGlobalSpec.pictureStrategy);
        } else {
            // 否则使用全局的
            if (mGlobalSpec.saveStrategy == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mPictureMediaStoreCompat.setSaveStrategy(mGlobalSpec.saveStrategy);
            }
        }
    }

    /**
     * 初始化view
     */
    private void initView() {
        // 自定义View中如果重写了onDraw()即自定义了绘制，那么就应该在构造函数中调用view的setWillNotDraw(false).
        setWillNotDraw(false);
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_camera_main_view_zjh, this);
        mViewHolder = new ViewHolder(view);

        // 初始化cameraView

        setFlashLamp(); // 设置闪光灯模式
        mViewHolder.imgSwitch.setImageResource(mCameraSpec.imageSwitch);
        mViewHolder.pvLayout.setDuration(mCameraSpec.duration * 1000);// 设置录制时间
        mViewHolder.pvLayout.setMinDuration(mCameraSpec.minDuration);// 最短录制时间

        // 判断点击和长按的权限
        if (mCameraSpec.onlySupportImages()) {
            // 禁用长按功能
            mViewHolder.pvLayout.setButtonFeatures(BUTTON_STATE_ONLY_CLICK);
            mViewHolder.pvLayout.setTip(getResources().getString(R.string.light_touch_take));
        } else if (mCameraSpec.onlySupportVideos()) {
            // 禁用点击功能
            mViewHolder.pvLayout.setButtonFeatures(BUTTON_STATE_ONLY_LONGCLICK);
            mViewHolder.pvLayout.setTip(getResources().getString(R.string.long_press_camera));
        } else {
            // 支持所有，不过要判断数量
            if (mGlobalSpec.maxImageSelectable == 0) {
                // 禁用点击功能
                mViewHolder.pvLayout.setButtonFeatures(BUTTON_STATE_ONLY_LONGCLICK);
                mViewHolder.pvLayout.setTip(getResources().getString(R.string.long_press_camera));
            } else if (mGlobalSpec.maxVideoSelectable == 0) {
                // 禁用长按功能
                mViewHolder.pvLayout.setButtonFeatures(BUTTON_STATE_ONLY_CLICK);
                mViewHolder.pvLayout.setTip(getResources().getString(R.string.light_touch_take));
            } else {
                mViewHolder.pvLayout.setButtonFeatures(BUTTON_STATE_BOTH);
                mViewHolder.pvLayout.setTip(getResources().getString(R.string.light_touch_take_long_press_camera));
            }
        }
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

        // 切换摄像头前置/后置
        mViewHolder.imgSwitch.setOnClickListener(v -> mViewHolder.cameraView.toggleFacing());
        mViewHolder.imgSwitch.setOnClickListener(v -> mViewHolder.cameraView.toggleFacing());

        // 主按钮监听
        mViewHolder.pvLayout.setPhotoVideoListener(new ClickOrLongListener() {
            @Override
            public void actionDown() {
                if (mClickOrLongListener != null)
                    mClickOrLongListener.actionDown();
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick() {
                // 判断数量
                if (mViewHolder.llPhoto.getChildCount() < currentMaxSelectable()) {
                    // 拍照  隐藏 闪光灯、右上角的切换摄像头
                    setSwitchVisibility(INVISIBLE);
                    mViewHolder.imgFlash.setVisibility(INVISIBLE);
                    // 设置不能点击，防止多次点击报错
                    mViewHolder.rlMain.setChildClickable(false);
                    mViewHolder.cameraView.takePictureSnapshot();
                    if (mClickOrLongListener != null)
                        mClickOrLongListener.onClick();
                } else {
                    Toast.makeText(mContext, "已经达到拍照上限", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLongClickShort(final long time) {
                mViewHolder.pvLayout.setTipAlphaAnimation(getResources().getString(R.string.the_recording_time_is_too_short));  // 提示过短
                setSwitchVisibility(VISIBLE);
                mViewHolder.imgFlash.setVisibility(VISIBLE);
                postDelayed(() -> stopRecord(true), 1500 - time);
                if (mClickOrLongListener != null)
                    mClickOrLongListener.onLongClickShort(time);
            }

            @Override
            public void onLongClick() {
                if (mViewHolder.cameraView.isTakingVideo())
                    Toast.makeText(mContext, "Already taking video.", Toast.LENGTH_SHORT).show();
                if (mViewHolder.cameraView.getPreview() != Preview.GL_SURFACE)
                    Toast.makeText(mContext, "Video snapshots are only allowed with the GL_SURFACE preview.", Toast.LENGTH_SHORT).show();


                // 开始录像
                setSwitchVisibility(INVISIBLE);
                mViewHolder.imgFlash.setVisibility(INVISIBLE);
                // 用于播放的视频file
                mVideoFile = mVideoMediaStoreCompat.getFilePath(1);
                mViewHolder.cameraView.takeVideoSnapshot(mVideoFile);
                if (mClickOrLongListener != null)
                    mClickOrLongListener.onLongClick();
            }

            @Override
            public void onLongClickEnd(long time) {
                // 录像结束
                stopRecord(false);
                if (mClickOrLongListener != null)
                    mClickOrLongListener.onLongClickEnd(time);
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

        // 拍照监听
        mViewHolder.cameraView.addCameraListener(new CameraListener() {

            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                result.toBitmap(bitmap -> {
                    // 显示图片
                    showPicture(bitmap);
                    // 恢复点击
                    mViewHolder.rlMain.setChildClickable(true);
                });
                super.onPictureTaken(result);
            }

            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                super.onVideoTaken(result);
            }

            @Override
            public void onVideoRecordingStart() {
                Log.d(TAG, "onVideoRecordingStart");
                super.onVideoRecordingStart();
            }

            @Override
            public void onVideoRecordingEnd() {
                Log.d(TAG, "onVideoRecordingEnd");
                super.onVideoRecordingEnd();
                // 如果录制结束，播放该视频
                playVideo();
            }

            @Override
            public void onCameraError(@NonNull CameraException exception) {
                super.onCameraError(exception);
                if (!TextUtils.isEmpty(exception.getMessage())) {
                    Log.d(TAG, exception.getMessage());
                    mErrorLisenter.onError();
                }
            }

        });

        // 确认和取消
        mViewHolder.pvLayout.setOperaeListener(new OperationLayout.OperaeListener() {
            @Override
            public void cancel() {
                // 根据不同状态处理相应的事件,多图不需要取消事件（关闭所有图片就自动恢复了）。
                if (getState() == Constants.STATE_PICTURE) {
                    resetState(TYPE_PICTURE);   // 针对图片模式进行的重置
                    mViewHolder.pvLayout.reset();
                    setState(Constants.STATE_PREVIEW); // 设置空闲状态
                } else if (getState() == Constants.STATE_VIDEO) {
                    resetState(TYPE_VIDEO);     // 针对视频模式进行的重置
                    mViewHolder.pvLayout.reset();
                    setState(Constants.STATE_PREVIEW); // 设置空闲状态
                }
                if (mOperaeCameraListener != null)
                    mOperaeCameraListener.cancel();
            }

            @Override
            public void confirm() {
                // 根据不同状态处理相应的事件
                if (getState() == Constants.STATE_PICTURE) {
                    // 图片模式的提交
                    confirmState(TYPE_PICTURE);
                    setState(Constants.STATE_PREVIEW); // 设置空闲状态
                } else if (getState() == Constants.STATE_VIDEO) {
                    confirmState(TYPE_VIDEO);
                    setState(Constants.STATE_PREVIEW); // 设置空闲状态
                } else if (getState() == Constants.STATE_PICTURE_PREVIEW) {
                    // 图片模式的提交
                    confirmState(TYPE_PICTURE);
                    setState(Constants.STATE_PREVIEW); // 设置空闲状态
                }

            }
        });

        // 关闭事件
        mViewHolder.imgClose.setOnClickListener(v -> {
            if (mCloseListener != null)
                mCloseListener.onClose();
        });

    }

    /**
     * 生命周期onResume
     */
    public void onResume() {
        LogUtil.i("CameraLayout onResume");
        resetState(TYPE_DEFAULT); //重置状态
        mViewHolder.cameraView.open();
    }

    /**
     * 生命周期onPause
     */
    public void onPause() {
        LogUtil.i("CameraLayout onPause");
        stopVideo(); // 停止播放
        resetState(TYPE_PICTURE);
        mViewHolder.cameraView.close();
    }

    /**
     * 生命周期onDestroy
     */
    protected void onDestroy() {
        LogUtil.i("CameraLayout destroy");
        mViewHolder.cameraView.destroy();
    }

    /**
     * 进行删除
     *
     * @param position 索引
     */
    public void removePosition(int position) {
        // 删除
        mCaptureBitmaps.remove(position);
        mViewHolder.llPhoto.removeView(mCaptureViews.get(position));

        // 回调接口：删除图片后剩下的相关数据
        mCaptureListener.remove(mCaptureBitmaps);

        // 当列表全部删掉的话，就隐藏
        if (mCaptureBitmaps.size() <= 0) {
            // 隐藏横版列表
            mViewHolder.hsvPhoto.setVisibility(View.GONE);

            // 隐藏横版列表的线条空间
            mViewHolder.vLine1.setVisibility(View.GONE);
            mViewHolder.vLine2.setVisibility(View.GONE);

            // 隐藏右侧按钮
            mViewHolder.pvLayout.getViewHolder().btnConfirm.setVisibility(View.GONE);

            // 恢复长按事件，即重新启用录像
            mViewHolder.pvLayout.getViewHolder().btnClickOrLong.setButtonFeatures(BUTTON_STATE_BOTH);

            setState(Constants.STATE_PREVIEW); // 设置空闲状态
        }
    }

    /**
     * 刷新多个图片
     */
    public void refreshMultiPhoto() {
        ListIterator<Map.Entry<Integer, BitmapData>> i = new ArrayList<>(mCaptureBitmaps.entrySet()).listIterator(mCaptureBitmaps.size());
        while (i.hasPrevious()) {
            Map.Entry<Integer, BitmapData> entry = i.previous();
            ImageView imgPhoto = mCaptureViews.get(entry.getKey()).findViewById(R.id.imgPhoto);
            mGlobalSpec.imageEngine.loadThumbnail(getContext(), imgPhoto.getWidth(), mPlaceholder,
                    imgPhoto, mCaptureBitmaps.get(entry.getKey()).getUri());

        }
    }

    /**
     * 针对当前状态重新设置状态
     *
     * @param type 类型
     */
    private void resetState(int type) {
        switch (type) {
            case TYPE_VIDEO:
                stopVideo(); // 停止播放重新播放
                FileUtil.deleteFile(mVideoFile.getPath()); // 删除文件
                // 隐藏video
                mViewHolder.vvPreview.setVisibility(INVISIBLE);
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
        setSwitchVisibility(VISIBLE);
        mViewHolder.imgFlash.setVisibility(VISIBLE);
    }

    /**
     * 确认状态
     *
     * @param type 类型
     */
    private void confirmState(int type) {
        switch (type) {
            case TYPE_VIDEO:
                // 录视频完成
                stopVideo();    //停止播放
                if (mOperaeCameraListener != null) {
                    mOperaeCameraListener.recordSuccess(mVideoFile.getPath());
                }
                // 加入视频到android系统库里面
                BitmapUtils.displayToGallery(getContext(), mVideoFile);
                break;
            case TYPE_PICTURE:
                // 拍照完成
                mViewHolder.imgPhoto.setVisibility(INVISIBLE);
                if (mOperaeCameraListener != null) {
                    ArrayList<String> paths = getPaths();
                    ArrayList<Uri> uris = getUris(paths);
                    mOperaeCameraListener.captureSuccess(paths, uris);
                    // 加入图片到android系统库里面
                    for (BitmapData value : mCaptureBitmaps.values()) {
                        BitmapUtils.displayToGallery(getContext(), value.getFile());
                    }
                }
                break;
            case TYPE_SHORT:
                break;
            case TYPE_DEFAULT:
                break;
        }
        mViewHolder.pvLayout.reset();
    }

    /**
     * 显示图片 单个或者多个
     *
     * @param bitmap bitmap
     */
    private void showPicture(Bitmap bitmap) {
        // 初始化数据并且存储进file
        File file = mPictureMediaStoreCompat.saveFileByBitmap(bitmap);
        Uri uri = mPictureMediaStoreCompat.getUri(file.getPath());
        BitmapData bitmapData = new BitmapData(file, uri);
        // 回收bitmap
        if (bitmap != null && bitmap.isRecycled()) {
            // 回收并且置为null
            bitmap.recycle();
        }
        System.gc();// 加速回收机制

        // 判断是否多个图片
        if (mGlobalSpec.maxImageSelectable > 1) {
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
            mGlobalSpec.imageEngine.loadThumbnail(getContext(), viewHolderImageView.imgPhoto.getWidth(), mPlaceholder,
                    viewHolderImageView.imgPhoto, bitmapData.getUri());
            // 删除事件
            viewHolderImageView.imgCancel.setTag(R.id.tagid, mPosition);
            viewHolderImageView.imgCancel.setOnClickListener(v -> removePosition(Integer.parseInt(v.getTag(R.id.tagid).toString())));

            // 打开显示大图
            viewHolderImageView.imgPhoto.setTag(R.id.tagid, String.valueOf(mPosition));
            viewHolderImageView.imgPhoto.setOnClickListener(v -> {
                ArrayList<MultiMedia> items = new ArrayList<>();
                for (BitmapData value : mCaptureBitmaps.values()) {
                    MultiMedia item = new MultiMedia();
                    item.setUri(value.getUri());
                    item.setType(MultimediaTypes.PICTURE);
                    items.add(item);
                }
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList(STATE_SELECTION, items);
                bundle.putInt(STATE_COLLECTION_TYPE, COLLECTION_IMAGE);

                Intent intent = new Intent(mContext, AlbumPreviewActivity.class);

                // 获取目前点击的这个item
                MultiMedia item = new MultiMedia();
                item.setUri(mCaptureBitmaps.get(Integer.parseInt(String.valueOf(v.getTag(R.id.tagid)))).getUri());
                item.setType(MultimediaTypes.PICTURE);
                item.setMimeType(MimeType.JPEG.toString());
                intent.putExtra(AlbumPreviewActivity.EXTRA_ITEM, item);

                intent.putExtra(BasePreviewActivity.EXTRA_DEFAULT_BUNDLE, bundle);
                intent.putExtra(BasePreviewActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
                intent.putExtra(BasePreviewActivity.EXTRA_IS_ALLOW_REPEAT, true);
                intent.putExtra(BasePreviewActivity.IS_SELECTED_LISTENER, false);
                intent.putExtra(BasePreviewActivity.IS_SELECTED_CHECK, false);
                fragment.startActivityForResult(intent, REQUEST_CODE_PREVIEW_CAMRRA);
                if (mGlobalSpec.isCutscenes) {
                    if (fragment.getActivity() != null)
                        fragment.getActivity().overridePendingTransition(R.anim.activity_open, 0);
                }

            });

            mCaptureViews.put(mPosition, viewHolderImageView.rootView);
            mViewHolder.llPhoto.addView(viewHolderImageView.rootView);
            mViewHolder.pvLayout.startTipAlphaAnimation();
            mViewHolder.pvLayout.startOperaeBtnAnimatorMulti();

            // 重置按钮，因为每次点击，都会自动关闭
            mViewHolder.pvLayout.getViewHolder().btnClickOrLong.resetState();
            // 显示右上角
            setSwitchVisibility(View.VISIBLE);
            mViewHolder.imgFlash.setVisibility(View.VISIBLE);

            // 设置当前模式是图片休闲并存模式
            setState(Constants.STATE_PICTURE_PREVIEW);

            // 禁用长按事件，即禁止录像
            mViewHolder.pvLayout.getViewHolder().btnClickOrLong.setButtonFeatures(BUTTON_STATE_ONLY_CLICK);
        } else {
            // 如果只有单个图片，就显示相应的提示结果等等
            mViewHolder.imgPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
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

    /**
     * 播放视频,用于录制后，在是否确认的界面中，播放视频
     */
    private void playVideo() {
        mViewHolder.vvPreview.setVisibility(View.VISIBLE);
        // mediaController 是底部控制条
        MediaController mediaController = new MediaController(mContext);
        mediaController.setAnchorView(mViewHolder.vvPreview);
        mediaController.setMediaPlayer(mViewHolder.vvPreview);
        mediaController.setVisibility(View.GONE);
        mViewHolder.vvPreview.setMediaController(mediaController);
        mViewHolder.vvPreview.setVideoURI(Uri.fromFile(mVideoFile));
        if (!mViewHolder.vvPreview.isPlaying()) {
            mViewHolder.vvPreview.start();
        }
        mViewHolder.vvPreview.setOnCompletionListener(mediaPlayer -> {
            // 循环播放
            if (!mViewHolder.vvPreview.isPlaying()) {
                mViewHolder.vvPreview.start();
            }
        });
    }

    /**
     * 停止播放视频
     */
    private void stopVideo() {
        if (mViewHolder.vvPreview.isPlaying())
            mViewHolder.vvPreview.stopPlayback();
    }

    /**
     * 获取当前view的状态
     *
     * @return 状态
     */
    private int getState() {
        return mState;
    }

    /**
     * 设置当前view的状态
     *
     * @param state 状态
     */
    private void setState(int state) {
        this.mState = state;
    }

    /**
     * 设置闪关灯
     */
    private void setFlashLamp() {
        switch (mFlashType) {
            case Constants.TYPE_FLASH_AUTO:
                mViewHolder.imgFlash.setImageResource(mCameraSpec.imageFlashAuto);
                mViewHolder.cameraView.setFlash(Flash.AUTO);
                break;
            case Constants.TYPE_FLASH_ON:
                mViewHolder.imgFlash.setImageResource(mCameraSpec.imageFlashOn);
                mViewHolder.cameraView.setFlash(Flash.TORCH);
                break;
            case Constants.TYPE_FLASH_OFF:
                mViewHolder.imgFlash.setImageResource(mCameraSpec.imageFlashOff);
                mViewHolder.cameraView.setFlash(Flash.OFF);
                break;
        }
    }

    /**
     * 返回最多选择的图片数量
     *
     * @return 数量
     */
    private int currentMaxSelectable() {
        GlobalSpec spec = GlobalSpec.getInstance();
        // 返回最大选择数量
        return spec.maxImageSelectable;
    }

    /**
     * 返回当前所有的路径 paths
     */
    private ArrayList<String> getPaths() {
        ArrayList<String> paths = new ArrayList<>();
        for (BitmapData value : mCaptureBitmaps.values()) {
            paths.add(value.getFile().getPath());
        }
        return paths;
    }

    /**
     * 返回当前所有的路径 uris
     */
    private ArrayList<Uri> getUris(ArrayList<String> paths) {
        ArrayList<Uri> uris = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            uris.add(mPictureMediaStoreCompat.getUri(paths.get(i)));
        }
        return uris;
    }

    /**
     * 调用停止录像
     *
     * @param isShort 是否因为视频过短而停止
     */
    private void stopRecord(boolean isShort) {
        if (isShort) {
            // 如果视频过短就是录制不成功
            resetState(TYPE_SHORT);
            mViewHolder.pvLayout.reset();
        } else {
            mViewHolder.cameraView.stopVideo();
            // 设置成视频播放状态
            setState(Constants.STATE_VIDEO);
        }
    }

    /**
     * 设置闪光灯是否显示，如果不支持，是一直不会显示
     */
    private void setSwitchVisibility(int viewVisibility) {
        if (!PackageManagerUtils.isSupportCameraLedFlash(mContext.getPackageManager())) {
            mViewHolder.imgSwitch.setVisibility(View.GONE);
        } else {
            mViewHolder.imgSwitch.setVisibility(viewVisibility);
        }
    }

    /**
     * 设置fragment
     */
    public void setFragment(CameraFragment fragment) {
        this.fragment = fragment;
    }

    public static class ViewHolder {

        View rootView;
        ChildClickableFrameLayout rlMain;
        VideoView vvPreview;
        ImageView imgPhoto;
        public ImageView imgFlash;
        public ImageView imgSwitch;
        public OperationLayout pvLayout;
        public HorizontalScrollView hsvPhoto;
        LinearLayout llPhoto;
        View vLine1;
        View vLine2;
        View vLine3;
        ImageView imgClose;
        CameraView cameraView;
        ConstraintLayout clMenu;

        ViewHolder(View rootView) {
            this.rootView = rootView;
            this.rlMain = rootView.findViewById(R.id.rlMain);
            this.imgPhoto = rootView.findViewById(R.id.imgPhoto);
            this.imgFlash = rootView.findViewById(R.id.imgFlash);
            this.imgSwitch = rootView.findViewById(R.id.imgSwitch);
            this.pvLayout = rootView.findViewById(R.id.pvLayout);
            this.hsvPhoto = rootView.findViewById(R.id.hsvPhoto);
            this.llPhoto = rootView.findViewById(R.id.llPhoto);
            this.vLine1 = rootView.findViewById(R.id.vLine1);
            this.vLine2 = rootView.findViewById(R.id.vLine2);
            this.vLine3 = rootView.findViewById(R.id.vLine3);
            this.imgClose = rootView.findViewById(R.id.imgClose);
            this.cameraView = rootView.findViewById(R.id.cameraView);
            this.vvPreview = rootView.findViewById(R.id.vvPreview);
            this.clMenu = rootView.findViewById(R.id.clMenu);
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
