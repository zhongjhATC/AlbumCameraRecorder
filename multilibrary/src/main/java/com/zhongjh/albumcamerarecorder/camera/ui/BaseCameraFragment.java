package com.zhongjh.albumcamerarecorder.camera.ui;

import static android.app.Activity.RESULT_OK;
import static com.zhongjh.albumcamerarecorder.camera.constants.FlashModels.TYPE_FLASH_AUTO;
import static com.zhongjh.albumcamerarecorder.camera.constants.FlashModels.TYPE_FLASH_OFF;
import static com.zhongjh.albumcamerarecorder.camera.constants.FlashModels.TYPE_FLASH_ON;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_SELECTION_LOCAL_FILE;
import static com.zhongjh.albumcamerarecorder.utils.MediaStoreUtils.MediaTypes.TYPE_PICTURE;
import static com.zhongjh.albumcamerarecorder.widget.clickorlongbutton.ClickOrLongButton.BUTTON_STATE_BOTH;
import static com.zhongjh.albumcamerarecorder.widget.clickorlongbutton.ClickOrLongButton.BUTTON_STATE_CLICK_AND_HOLD;
import static com.zhongjh.albumcamerarecorder.widget.clickorlongbutton.ClickOrLongButton.BUTTON_STATE_ONLY_CLICK;
import static com.zhongjh.albumcamerarecorder.widget.clickorlongbutton.ClickOrLongButton.BUTTON_STATE_ONLY_LONG_CLICK;
import static com.zhongjh.imageedit.ImageEditActivity.EXTRA_HEIGHT;
import static com.zhongjh.imageedit.ImageEditActivity.EXTRA_WIDTH;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Flash;
import com.zhongjh.albumcamerarecorder.BaseFragment;
import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.camera.ui.impl.ICameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.impl.ICameraView;
import com.zhongjh.albumcamerarecorder.camera.PreviewVideoActivity;
import com.zhongjh.albumcamerarecorder.camera.adapter.PhotoAdapter;
import com.zhongjh.albumcamerarecorder.camera.adapter.PhotoAdapterListener;
import com.zhongjh.albumcamerarecorder.camera.ui.camerastate.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.ui.camerastate.StateInterface;
import com.zhongjh.albumcamerarecorder.camera.constants.FlashCacheUtils;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.camera.listener.ClickOrLongListener;
import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;
import com.zhongjh.albumcamerarecorder.camera.util.LogUtil;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.settings.CameraSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.utils.MediaStoreUtils;
import com.zhongjh.albumcamerarecorder.utils.PackageManagerUtils;
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils;
import com.zhongjh.albumcamerarecorder.widget.BaseOperationLayout;
import com.zhongjh.albumcamerarecorder.widget.ImageViewTouch;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.common.listener.OnMoreClickListener;
import com.zhongjh.common.listener.VideoEditListener;
import com.zhongjh.common.utils.MediaStoreCompat;
import com.zhongjh.common.utils.ThreadUtils;
import com.zhongjh.imageedit.ImageEditActivity;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个父类的拍摄Fragment，用于开放出来给开发自定义，但是同时也需要遵守一些规范
 * 因为该类含有过多方法，所以采用 多接口 + Facade 模式
 *
 * @author zhongjh
 * @date 2022/8/11
 */
public abstract class BaseCameraFragment extends BaseFragment
        implements PhotoAdapterListener, ICameraFragment, ICameraView {

    private static final String TAG = BaseCameraFragment.class.getSimpleName();

    private final static int PROGRESS_MAX = 100;
    private final static int MILLISECOND = 2000;

    /**
     * 状态管理
     */
    public CameraStateManagement mCameraStateManagement;

    public CameraStateManagement getCameraStateManagement() {
        return mCameraStateManagement;
    }

    /**
     * 在图廊预览界面点击了确定
     */
    public ActivityResultLauncher<Intent> mAlbumPreviewActivityResult;

    /**
     * 从视频预览界面回来
     */
    ActivityResultLauncher<Intent> mPreviewVideoActivityResult;

    /**
     * 从编辑图片界面回来
     */
    ActivityResultLauncher<Intent> mImageEditActivityResult;

    public Context mContext;
    public MainActivity mActivity;

    /**
     * 图片
     */
    private MediaStoreCompat mPictureMediaStoreCompat;
    /**
     * 录像文件配置路径
     */
    private MediaStoreCompat mVideoMediaStoreCompat;
    /**
     * 公共配置
     */
    private GlobalSpec mGlobalSpec;
    /**
     * 拍摄配置
     */
    public CameraSpec mCameraSpec;

    /**
     * 一个迁移图片的异步线程
     */
    public ThreadUtils.SimpleTask<ArrayList<LocalFile>> mMovePictureFileTask;

    /**
     * 拍照的图片集合适配器
     */
    private PhotoAdapter mPhotoAdapter;
    /**
     * 图片,单图或者多图都会加入该列表
     */
    List<BitmapData> mBitmapData = new ArrayList<>();
    /**
     * 处于分段录制模式下的视频的文件列表
     */
    public final ArrayList<String> mVideoPaths = new ArrayList<>();
    /**
     * 处于分段录制模式下的视频的时间列表
     */
    private final ArrayList<Long> mVideoTimes = new ArrayList<>();
    /**
     * 处于分段录制模式下合成的新的视频
     */
    private String mNewSectionVideoPath;
    /**
     * 视频File,用于后面能随时删除
     */
    public File mVideoFile;
    /**
     * 照片File,用于后面能随时删除
     */
    public File mPhotoFile;
    /**
     * 编辑后的照片
     */
    private File mPhotoEditFile;
    /**
     * 照片Uri,作用于单图
     */
    public Uri mSinglePhotoUri;
    /**
     * 闪关灯状态 默认关闭
     */
    private int mFlashModel = TYPE_FLASH_OFF;
    /**
     * 默认图片
     */
    private Drawable mPlaceholder;
    /**
     * 上一个分段录制的时间
     */
    private long mSectionRecordTime;
    /**
     * 声明一个long类型变量：用于存放上一点击“返回键”的时刻
     */
    private long mExitTime;
    /**
     * 是否短时间录像
     */
    public boolean mIsShort;
    /**
     * 是否分段录制
     */
    public boolean mIsSectionRecord;
    /**
     * 是否提交,如果不是提交则要删除冗余文件
     */
    private boolean mIsCommit = false;
    /**
     * 是否中断录像
     */
    private boolean mIsBreakOff;
    /**
     * 延迟拍摄，用于打开闪光灯再拍摄
     */
    private final Handler mCameraTakePictureHandler = new Handler(Looper.getMainLooper());
    private final Runnable mCameraTakePictureRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCameraSpec.getEnableImageHighDefinition()) {
                getCameraView().takePicture();
            } else {
                getCameraView().takePictureSnapshot();
            }
        }
    };

    /**
     * 修饰多图控件的View数组
     */
    @Nullable
    private View[] multiplePhotoViews;
    /**
     * 修饰单图控件的View
     */
    private ImageViewTouch singlePhotoViews;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(setContentView(), container, false);
        view.setOnKeyListener((v, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        initView(view, savedInstanceState);
        setView();
        initData();
        initListener();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mActivity = (MainActivity) context;
            mContext = context.getApplicationContext();
        }
    }

    @Override
    public boolean onBackPressed() {
        Boolean isTrue = getCameraStateManagement().onBackPressed();
        if (isTrue != null) {
            return isTrue;
        } else {
            // 与上次点击返回键时刻作差，第一次不能立即退出
            if ((System.currentTimeMillis() - mExitTime) > MILLISECOND) {
                // 大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(mActivity.getApplicationContext(), getResources().getString(R.string.z_multi_library_press_confirm_again_to_close), Toast.LENGTH_SHORT).show();
                // 并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NotNull KeyEvent event) {
        if ((keyCode & mCameraSpec.getKeyCodeTakePhoto()) > 0) {
            takePhoto();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 生命周期onResume
     */
    @Override
    public void onResume() {
        super.onResume();
        LogUtil.i("CameraLayout onResume");
        // 清空进度，防止正在进度中突然按home键
        getPhotoVideoLayout().getViewHolder().btnClickOrLong.reset();
        // 重置当前按钮的功能
        initPvLayoutButtonFeatures();
        getCameraView().open();
    }

    /**
     * 生命周期onPause
     */
    @Override
    public void onPause() {
        super.onPause();
        LogUtil.i("CameraLayout onPause");
        getCameraView().close();
    }

    @Override
    public void onDestroy() {
        onDestroy(mIsCommit);
        mCameraSpec.setOnCaptureListener(null);
        super.onDestroy();
    }

    /**
     * 生命周期onDestroy
     *
     * @param isCommit 是否提交了数据,如果不是提交则要删除冗余文件
     */
    protected void onDestroy(boolean isCommit) {
        LogUtil.i("CameraLayout destroy");
        if (!isCommit) {
            if (mPhotoFile != null) {
                // 删除图片
                FileUtil.deleteFile(mPhotoFile);
            }
            if (mVideoFile != null) {
                // 删除视频
                FileUtil.deleteFile(mVideoFile);
            }
            // 删除多个视频
            for (String item : mVideoPaths) {
                FileUtil.deleteFile(item);
            }
            // 删除多个图片
            if (mPhotoAdapter.getListData() != null) {
                for (BitmapData bitmapData : mPhotoAdapter.getListData()) {
                    FileUtil.deleteFile(bitmapData.getPath());
                }
            }
            // 新合成视频删除
            if (mNewSectionVideoPath != null) {
                FileUtil.deleteFile(mNewSectionVideoPath);
            }
        } else {
            // 如果是提交的，删除合成前的视频
            for (String item : mVideoPaths) {
                FileUtil.deleteFile(item);
            }
        }
        getPhotoVideoLayout().getViewHolder().btnConfirm.reset();
        if (mCameraSpec.isMergeEnable()) {
            if (mCameraSpec.getVideoMergeCoordinator() != null) {
                mCameraSpec.getVideoMergeCoordinator().onMergeDestroy(this.getClass());
                mCameraSpec.setVideoMergeCoordinator(null);
            }
        }
        mCameraTakePictureHandler.removeCallbacks(mCameraTakePictureRunnable);
        if (mMovePictureFileTask != null) {
            mMovePictureFileTask.cancel();
        }
        getCameraView().destroy();
        // 记忆模式
        flashSaveCache();
    }

    /**
     * 设置相关view，由子类赋值
     */
    protected void setView() {
        multiplePhotoViews = getMultiplePhotoView();
        singlePhotoViews = getSinglePhotoView();
    }

    /**
     * 初始化相关数据
     */
    protected void initData() {
        // 初始化设置
        mGlobalSpec = GlobalSpec.INSTANCE;
        mCameraSpec = CameraSpec.INSTANCE;
        mCameraStateManagement = new CameraStateManagement(this);
        onActivityResult();
        // 设置图片路径
        if (mGlobalSpec.getPictureStrategy() != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            mPictureMediaStoreCompat = new MediaStoreCompat(mContext, mGlobalSpec.getPictureStrategy());
        } else {
            // 否则使用全局的
            if (mGlobalSpec.getSaveStrategy() == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mPictureMediaStoreCompat = new MediaStoreCompat(mContext, mGlobalSpec.getSaveStrategy());
            }
        }
        // 设置视频路径
        if (mGlobalSpec.getVideoStrategy() != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            mVideoMediaStoreCompat = new MediaStoreCompat(mContext, mGlobalSpec.getVideoStrategy());
        } else {
            // 否则使用全局的
            if (mGlobalSpec.getSaveStrategy() == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mVideoMediaStoreCompat = new MediaStoreCompat(mContext, mGlobalSpec.getSaveStrategy());
            }
        }

        // 默认图片
        TypedArray ta = mContext.getTheme().obtainStyledAttributes(
                new int[]{R.attr.album_thumbnail_placeholder});
        mPlaceholder = ta.getDrawable(0);

        // 闪光灯修改默认模式
        mFlashModel = mCameraSpec.getFlashModel();
        // 记忆模式
        flashGetCache();
        initMultiplePhotoAdapter();
    }

    /**
     * 初始化相关事件
     */
    protected void initListener() {
        // 关闭事件
        initCameraLayoutCloseListener();
        // 切换闪光灯模式
        initImgFlashListener();
        // 切换摄像头前置/后置
        initImgSwitchListener();
        // 主按钮监听
        initPvLayoutPhotoVideoListener();
        // 左右确认和取消
        initPvLayoutOperateListener();
        // 录制界面按钮事件监听，目前只有一个，点击分段录制
        initPvLayoutRecordListener();
        // 视频编辑后的事件，目前只有分段录制后合并
        initVideoEditListener();
        // 拍照监听
        initCameraViewListener();
        // 编辑图片事件
        initPhotoEditListener();
    }

    /**
     * 关闭View初始化事件
     */
    private void initCameraLayoutCloseListener() {
        if (getCloseView() != null) {
            getCloseView().setOnClickListener(new OnMoreClickListener() {
                @Override
                public void onListener(@NonNull View v) {
                    setBreakOff(true);
                    mActivity.finish();
                }
            });
        }
    }

    /**
     * 切换闪光灯模式
     */
    private void initImgFlashListener() {
        if (getFlashView() != null) {
            getFlashView().setOnClickListener(v -> {
                mFlashModel++;
                if (mFlashModel > TYPE_FLASH_OFF) {
                    mFlashModel = TYPE_FLASH_AUTO;
                }
                // 重新设置当前闪光灯模式
                setFlashLamp();
            });
        }
    }

    /**
     * 切换摄像头前置/后置
     */
    private void initImgSwitchListener() {
        if (getSwitchView() != null) {
            getSwitchView().setOnClickListener(v -> getCameraView().toggleFacing());
            getSwitchView().setOnClickListener(v -> getCameraView().toggleFacing());
        }
    }

    /**
     * 主按钮监听
     */
    private void initPvLayoutPhotoVideoListener() {
        getPhotoVideoLayout().setPhotoVideoListener(new ClickOrLongListener() {
            @Override
            public void actionDown() {
                Log.d(TAG, "pvLayout actionDown");
                // 母窗体隐藏底部滑动
                mActivity.showHideTableLayout(false);
            }

            @Override
            public void onClick() {
                Log.d(TAG, "pvLayout onClick");
                takePhoto();
            }

            @Override
            public void onLongClickShort(final long time) {
                Log.d(TAG, "pvLayout onLongClickShort");
                longClickShort(time);
            }

            @Override
            public void onLongClick() {
                Log.d(TAG, "pvLayout onLongClick ");
                recordVideo();
            }

            @Override
            public void onLongClickEnd(long time) {
                Log.d(TAG, "pvLayout onLongClickEnd " + time);
                mSectionRecordTime = time;
                // 录像结束
                stopRecord(false);
            }

            @Override
            public void onLongClickError() {
                Log.d(TAG, "pvLayout onLongClickError ");
            }

            @Override
            public void onBanClickTips() {
                // 判断如果是分段录制模式就提示
                if (mIsSectionRecord) {
                    getPhotoVideoLayout().setTipAlphaAnimation(getResources().getString(R.string.z_multi_library_working_video_click_later));
                }
            }

            @Override
            public void onClickStopTips() {
                if (mIsSectionRecord) {
                    getPhotoVideoLayout().setTipAlphaAnimation(getResources().getString(R.string.z_multi_library_touch_your_suspension));
                } else {
                    getPhotoVideoLayout().setTipAlphaAnimation(getResources().getString(R.string.z_multi_library_touch_your_end));
                }
            }
        });
    }

    /**
     * 左右两个按钮：确认和取消
     */
    private void initPvLayoutOperateListener() {
        getPhotoVideoLayout().setOperateListener(new BaseOperationLayout.OperateListener() {
            @Override
            public void cancel() {
                Log.d(TAG, "cancel " + getState().toString());
                mCameraStateManagement.pvLayoutCancel();
            }

            @Override
            public void confirm() {
                Log.d(TAG, "confirm " + getState().toString());
                mCameraStateManagement.pvLayoutCommit();
            }

            @Override
            public void startProgress() {
                Log.d(TAG, "startProgress " + getState().toString());
                mCameraStateManagement.pvLayoutCommit();
            }

            @Override
            public void stopProgress() {
                Log.d(TAG, "stopProgress " + getState().toString());
                mCameraStateManagement.stopProgress();
                // 重置按钮
                getPhotoVideoLayout().resetConfirm();
            }

            @Override
            public void doneProgress() {
                Log.d(TAG, "doneProgress " + getState().toString());
                getPhotoVideoLayout().resetConfirm();
            }
        });
    }

    /**
     * 录制界面按钮事件监听，目前只有一个，点击分段录制
     */
    private void initPvLayoutRecordListener() {
        getPhotoVideoLayout().setRecordListener(tag -> {
            mIsSectionRecord = "1".equals(tag);
            getPhotoVideoLayout().setProgressMode(true);
        });
    }

    /**
     * 视频编辑后的事件，目前 有分段录制后合并、压缩视频
     */
    private void initVideoEditListener() {
        if (mCameraSpec.isMergeEnable() && mCameraSpec.getVideoMergeCoordinator() != null) {
            mCameraSpec.getVideoMergeCoordinator().setVideoMergeListener(BaseCameraFragment.this.getClass(), new VideoEditListener() {
                @Override
                public void onFinish() {
                    Log.d(TAG, "videoMergeCoordinator onFinish");
                    getPhotoVideoLayout().getViewHolder().btnConfirm.setProgress(100);
                    PreviewVideoActivity.startActivity(BaseCameraFragment.this, mPreviewVideoActivityResult, mNewSectionVideoPath);
                }

                @Override
                public void onProgress(int progress, long progressTime) {
                    Log.d(TAG, "videoMergeCoordinator onProgress progress: " + progress + " progressTime: " + progressTime);
                    if (progress >= PROGRESS_MAX) {
                        getPhotoVideoLayout().getViewHolder().btnConfirm.setProgress(99);
                    } else {
                        getPhotoVideoLayout().getViewHolder().btnConfirm.setProgress(progress);
                    }
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "videoMergeCoordinator onCancel");
                    // 重置按钮
                    getPhotoVideoLayout().getViewHolder().btnConfirm.reset();
                }

                @Override
                public void onError(@NotNull String message) {
                    Log.d(TAG, "videoMergeCoordinator onError" + message);
                    // 重置按钮
                    getPhotoVideoLayout().getViewHolder().btnConfirm.reset();
                }
            });
        }
    }

    /**
     * 拍照、录制监听
     */
    private void initCameraViewListener() {
        getCameraView().addCameraListener(new CameraListener() {

            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                // 如果是自动闪光灯模式便关闭闪光灯
                if (mFlashModel == TYPE_FLASH_AUTO) {
                    getCameraView().setFlash(Flash.OFF);
                }
                result.toBitmap(bitmap -> {
                    // 显示图片
                    addCaptureData(bitmap);
                    // 恢复点击
                    getChildClickableLayout().setChildClickable(true);
                });
                super.onPictureTaken(result);
            }

            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                Log.d(TAG, "onVideoTaken");
                super.onVideoTaken(result);
                // 判断是否短时间结束
                if (!mIsShort && !isBreakOff()) {
                    if (!mIsSectionRecord) {
                        //  如果录制结束，打开该视频。打开底部菜单
                        PreviewVideoActivity.startActivity(BaseCameraFragment.this, mPreviewVideoActivityResult, result.getFile().getPath());
                        Log.d(TAG, "onVideoTaken " + result.getFile().getPath());
                    } else {
                        Log.d(TAG, "onVideoTaken 分段录制 " + result.getFile().getPath());
                        mVideoTimes.add(mSectionRecordTime);
                        // 如果已经有录像缓存，那么就不执行这个动作了
                        if (mVideoPaths.size() <= 0) {
                            getPhotoVideoLayout().startShowLeftRightButtonsAnimator();
                            getPhotoVideoLayout().getViewHolder().tvSectionRecord.setVisibility(View.GONE);
                        }
                        // 加入视频列表
                        mVideoPaths.add(result.getFile().getPath());
                        // 显示当前进度
                        getPhotoVideoLayout().setData(mVideoTimes);
                        // 创建新的file
                        mVideoFile = mVideoMediaStoreCompat.createFile(1, true, "mp4");
                        // 如果是在已经合成的情况下继续拍摄，那就重置状态
                        if (!getPhotoVideoLayout().getProgressMode()) {
                            getPhotoVideoLayout().resetConfirm();
                        }
                    }
                } else {
                    Log.d(TAG, "onVideoTaken delete " + mVideoFile.getPath());
                    FileUtil.deleteFile(mVideoFile);
                }
                mIsShort = false;
                setBreakOff(false);
                getPhotoVideoLayout().setEnabled(true);
            }

            @Override
            public void onVideoRecordingStart() {
                Log.d(TAG, "onVideoRecordingStart");
                super.onVideoRecordingStart();
                // 录制开始后，在没有结果之前，禁止第二次点击
                getPhotoVideoLayout().setEnabled(false);
            }

            @Override
            public void onCameraError(@NonNull CameraException exception) {
                Log.d(TAG, "onCameraError");
                super.onCameraError(exception);
                if (mIsSectionRecord) {
                    getPhotoVideoLayout().setTipAlphaAnimation(getResources().getString(R.string.z_multi_library_recording_error_roll_back_previous_paragraph));
                    getPhotoVideoLayout().getViewHolder().btnClickOrLong.selectionRecordRollBack();
                }
                if (!TextUtils.isEmpty(exception.getMessage())) {
                    Log.d(TAG, "onCameraError:" + exception.getMessage() + " " + exception.getReason());
                }
                getPhotoVideoLayout().setEnabled(true);
            }

        });
    }

    /**
     * 编辑图片事件
     */
    private void initPhotoEditListener() {
        getPhotoVideoLayout().getViewHolder().rlEdit.setOnClickListener(view -> {
            Uri uri = (Uri) view.getTag();
            mPhotoEditFile = mPictureMediaStoreCompat.createFile(0, true, "jpg");

            Intent intent = new Intent();
            intent.setClass(getContext(), ImageEditActivity.class);
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SCREEN_ORIENTATION, mActivity.getRequestedOrientation());
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_URI, uri);
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SAVE_PATH, mPhotoEditFile.getAbsolutePath());
            mImageEditActivityResult.launch(intent);
        });
    }

    /**
     * 针对回调
     */
    private void onActivityResult() {
        // 在图廊预览界面点击了确定
        mAlbumPreviewActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            boolean isReturn = onActivityResult(result.getResultCode());
            if (isReturn) {
                return;
            }
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() == null) {
                    return;
                }
                if (result.getData().getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
                    // 请求的预览界面
                    Bundle resultBundle = result.getData().getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE);
                    // 获取选择的数据
                    ArrayList<MultiMedia> selected = resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
                    if (selected == null) {
                        return;
                    }
                    // 重新赋值
                    ArrayList<BitmapData> bitmapDatas = new ArrayList<>();
                    for (MultiMedia item : selected) {
                        BitmapData bitmapData = new BitmapData(item.getPath(), item.getUri(), item.getWidth(), item.getHeight());
                        bitmapDatas.add(bitmapData);
                    }
                    // 全部刷新
                    refreshMultiPhoto(bitmapDatas);
                }
            }
        });

        // 从视频预览界面回来
        mPreviewVideoActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            boolean isReturn = onActivityResult(result.getResultCode());
            if (isReturn) {
                return;
            }
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() == null) {
                    return;
                }
                // 从视频预览界面回来
                ArrayList<LocalFile> localFiles = new ArrayList<>();
                LocalFile localFile = result.getData().getParcelableExtra(PreviewVideoActivity.LOCAL_FILE);
                localFiles.add(localFile);
                mIsCommit = true;
                if (mGlobalSpec.getOnResultCallbackListener() == null) {
                    // 获取视频路径
                    Intent intent = new Intent();
                    intent.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION_LOCAL_FILE, localFiles);
                    mActivity.setResult(RESULT_OK, intent);
                } else {
                    mGlobalSpec.getOnResultCallbackListener().onResult(localFiles);
                }
                mActivity.finish();
            }
        });

        // 从编辑图片界面回来
        mImageEditActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            boolean isReturn = onActivityResult(result.getResultCode());
            if (isReturn) {
                return;
            }
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() == null) {
                    return;
                }
                // 编辑图片界面
                refreshEditPhoto(result.getData().getIntExtra(EXTRA_WIDTH, 0),
                        result.getData().getIntExtra(EXTRA_HEIGHT, 0));
            }
        });
    }

    /**
     * 返回true的时候即是纸条跳过了后面的ActivityResult事件
     *
     * @param resultCode Activity的返回码
     * @return 返回true是跳过，返回false则是继续
     */
    private boolean onActivityResult(int resultCode) {
        return mCameraStateManagement.onActivityResult(resultCode);
    }

    /**
     * 初始化多图适配器
     */
    public void initMultiplePhotoAdapter() {
        // 初始化多图适配器，先判断是不是多图配置
        mPhotoAdapter = new PhotoAdapter(getContext(), mGlobalSpec, mBitmapData, this);
        if (getRecyclerViewPhoto() != null) {
            if (SelectableUtils.getImageMaxCount() > 1) {
                getRecyclerViewPhoto().setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
                getRecyclerViewPhoto().setAdapter(mPhotoAdapter);
                getRecyclerViewPhoto().setVisibility(View.VISIBLE);
            } else {
                getRecyclerViewPhoto().setVisibility(View.GONE);
            }
        }
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        // 开启才能执行别的事件, 如果已经有分段视频，则不允许拍照了
        if (getCameraView().isOpened() && mVideoTimes.size() <= 0) {
            // 判断数量
            if (mPhotoAdapter.getItemCount() < SelectableUtils.getImageMaxCount()) {
                // 设置不能点击，防止多次点击报错
                getChildClickableLayout().setChildClickable(false);
                // 判断如果是自动闪光灯模式便开启闪光灯
                if (mFlashModel == TYPE_FLASH_AUTO) {
                    getCameraView().setFlash(Flash.TORCH);
                    // 延迟1秒拍照
                    mCameraTakePictureHandler.postDelayed(mCameraTakePictureRunnable, 1000);
                } else {
                    mCameraTakePictureRunnable.run();
                }
            } else {
                getPhotoVideoLayout().setTipAlphaAnimation(getResources().getString(R.string.z_multi_library_the_camera_limit_has_been_reached));
            }
        }
    }

    /**
     * 录制视频
     */
    private void recordVideo() {
        // 开启录制功能才能执行别的事件
        if (getCameraView().isOpened()) {
            // 用于播放的视频file
            if (mVideoFile == null) {
                mVideoFile = mVideoMediaStoreCompat.createFile(1, true, "mp4");
            }
            if (mCameraSpec.getEnableVideoHighDefinition()) {
                getCameraView().takeVideo(mVideoFile);
            } else {
                getCameraView().takeVideoSnapshot(mVideoFile);
            }
            // 设置录制状态
            if (mIsSectionRecord) {
                mCameraStateManagement.setState(mCameraStateManagement.getVideoMultipleIn());
            } else {
                mCameraStateManagement.setState(mCameraStateManagement.getVideoIn());
            }
            // 开始录像
            setMenuVisibility(View.INVISIBLE);
        }
    }

    /**
     * 录制时间过短
     */
    private void longClickShort(final long time) {
        Log.d(TAG, "longClickShort " + time);
        mCameraStateManagement.longClickShort(time);
        // 提示过短
        getPhotoVideoLayout().setTipAlphaAnimation(getResources().getString(R.string.z_multi_library_the_recording_time_is_too_short));
        // 显示右上角菜单
        setMenuVisibility(View.VISIBLE);
        // 停止录像
        stopRecord(true);
    }

    /**
     * 点击图片事件
     *
     * @param intent 点击后，封装相关数据进入该intent
     */
    @Override
    public void onClick(Intent intent) {
        mAlbumPreviewActivityResult.launch(intent);
        if (mGlobalSpec.getCutscenesEnabled()) {
            if (getActivity() != null) {
                getActivity().overridePendingTransition(R.anim.activity_open_zjh, 0);
            }
        }
    }

    /**
     * 多图进行删除的时候
     *
     * @param bitmapData 数据
     * @param position   删除的索引
     */
    @Override
    public void onDelete(BitmapData bitmapData, int position) {
        // 删除文件
        FileUtil.deleteFile(bitmapData.getPath());

        // 判断如果删除光图片的时候，母窗体启动滑动
        if (mBitmapData.size() <= 0) {
            mActivity.showHideTableLayout(true);
        }
        if (mCameraSpec.getOnCaptureListener() != null) {
            mCameraSpec.getOnCaptureListener().remove(mBitmapData, position);
        }

        // 当列表全部删掉隐藏列表框的UI
        Log.d(TAG, "onDelete " + mBitmapData.size());
        if (mBitmapData.size() <= 0) {
            hideViewByMultipleZero();
        }
    }

    /**
     * 当多个图片删除到没有图片时候，隐藏相关View
     */
    @Override
    public void hideViewByMultipleZero() {
        // 隐藏横版列表
        if (getRecyclerViewPhoto() != null) {
            getRecyclerViewPhoto().setVisibility(View.GONE);
        }

        // 隐藏修饰多图控件的View
        if (multiplePhotoViews != null) {
            for (View view : multiplePhotoViews) {
                view.setVisibility(View.GONE);
            }
        }

        // 隐藏左右侧按钮
        getPhotoVideoLayout().getViewHolder().btnCancel.setVisibility(View.GONE);
        getPhotoVideoLayout().getViewHolder().btnConfirm.setVisibility(View.GONE);

        // 如果是单图编辑情况下,隐藏编辑按钮
        getPhotoVideoLayout().getViewHolder().rlEdit.setVisibility(View.GONE);

        // 恢复长按事件，即重新启用录制
        getPhotoVideoLayout().getViewHolder().btnClickOrLong.setVisibility(View.VISIBLE);
        initPvLayoutButtonFeatures();

        // 设置空闲状态
        mCameraStateManagement.setState(mCameraStateManagement.getPreview());

        showBottomMenu();
    }

    /**
     * 刷新多个图片
     */
    @Override
    public void refreshMultiPhoto(ArrayList<BitmapData> bitmapDatas) {
        mBitmapData = bitmapDatas;
        mPhotoAdapter.setListData(mBitmapData);
    }

    /**
     * 刷新编辑后的单图
     *
     * @param width  最新图片的宽度
     * @param height 最新图片的高度
     */
    @Override
    public void refreshEditPhoto(int width, int height) {
        // 删除旧图
        if (mPhotoFile.exists()) {
            boolean wasSuccessful = mPhotoFile.delete();
            if (!wasSuccessful) {
                System.out.println("was not successful.");
            }
        }
        // 用编辑后的图作为新的图片
        mPhotoFile = mPhotoEditFile;
        Uri uri = mPictureMediaStoreCompat.getUri(mPhotoFile.getPath());
        mSinglePhotoUri = uri;

        // 重置mCaptureBitmaps
        mBitmapData.clear();
        BitmapData bitmapData = new BitmapData(mPhotoFile.getPath(), uri, width, height);
        mBitmapData.add(bitmapData);

        // 重置位置
        if (getSinglePhotoView() != null) {
            getSinglePhotoView().resetMatrix();
            mGlobalSpec.getImageEngine().loadUriImage(mContext, getSinglePhotoView(), uri);
        }
    }

    /**
     * 删除视频 - 多个模式
     */
    public void removeVideoMultiple() {
        // 每次删除，后面都要重新合成,新合成的也删除
        getPhotoVideoLayout().resetConfirm();
        if (mNewSectionVideoPath != null) {
            FileUtil.deleteFile(mNewSectionVideoPath);
        }
        // 删除最后一个视频和视频文件
        FileUtil.deleteFile(mVideoPaths.get(mVideoPaths.size() - 1));
        mVideoPaths.remove(mVideoPaths.size() - 1);
        mVideoTimes.remove(mVideoTimes.size() - 1);

        // 显示当前进度
        getPhotoVideoLayout().setData(mVideoTimes);
        getPhotoVideoLayout().invalidateClickOrLongButton();
        if (mVideoPaths.size() == 0) {
            mCameraStateManagement.resetState();
        }
    }

    /**
     * 初始化中心按钮状态
     */
    protected void initPvLayoutButtonFeatures() {
        // 判断点击和长按的权限
        if (mCameraSpec.isClickRecord()) {
            // 禁用长按功能
            getPhotoVideoLayout().setButtonFeatures(BUTTON_STATE_CLICK_AND_HOLD);
            getPhotoVideoLayout().setTip(getResources().getString(R.string.z_multi_library_light_touch_camera));
        } else {
            if (mCameraSpec.onlySupportImages()) {
                // 禁用长按功能
                getPhotoVideoLayout().setButtonFeatures(BUTTON_STATE_ONLY_CLICK);
                getPhotoVideoLayout().setTip(getResources().getString(R.string.z_multi_library_light_touch_take));
            } else if (mCameraSpec.onlySupportVideos()) {
                // 禁用点击功能
                getPhotoVideoLayout().setButtonFeatures(BUTTON_STATE_ONLY_LONG_CLICK);
                getPhotoVideoLayout().setTip(getResources().getString(R.string.z_multi_library_long_press_camera));
            } else {
                // 支持所有，不过要判断数量
                if (SelectableUtils.getImageMaxCount() == 0) {
                    // 禁用点击功能
                    getPhotoVideoLayout().setButtonFeatures(BUTTON_STATE_ONLY_LONG_CLICK);
                    getPhotoVideoLayout().setTip(getResources().getString(R.string.z_multi_library_long_press_camera));
                } else if (SelectableUtils.getVideoMaxCount() == 0) {
                    // 禁用长按功能
                    getPhotoVideoLayout().setButtonFeatures(BUTTON_STATE_ONLY_CLICK);
                    getPhotoVideoLayout().setTip(getResources().getString(R.string.z_multi_library_light_touch_take));
                } else {
                    getPhotoVideoLayout().setButtonFeatures(BUTTON_STATE_BOTH);
                    getPhotoVideoLayout().setTip(getResources().getString(R.string.z_multi_library_light_touch_take_long_press_camera));
                }
            }
        }
    }

    @Override
    public void showProgress() {
        // 执行等待动画
        getPhotoVideoLayout().getViewHolder().btnConfirm.setProgress(1);
    }

    @Override
    public void setProgress(int progress) {
        getPhotoVideoLayout().getViewHolder().btnConfirm.addProgress(progress);
    }

    /**
     * 迁移图片文件，缓存文件迁移到配置目录
     * 在 doInBackground 线程里面也执行了 runOnUiThread 跳转UI的最终事件
     */
    public void movePictureFile() {
        showProgress();
        // 开始迁移文件
        ThreadUtils.executeByIo(getMovePictureFileTask());
    }

    /**
     * 迁移图片的线程
     */
    private ThreadUtils.SimpleTask<ArrayList<LocalFile>> getMovePictureFileTask() {
        mMovePictureFileTask = new ThreadUtils.SimpleTask<ArrayList<LocalFile>>() {
            @Override
            public ArrayList<LocalFile> doInBackground() throws IOException {
                // 每次拷贝文件后记录，最后用于全部添加到相册，回调等操作
                ArrayList<LocalFile> newFiles = new ArrayList<>();
                // 将 缓存文件 拷贝到 配置目录
                for (BitmapData item : mBitmapData) {
                    File oldFile = new File(item.getPath());
                    // 压缩图片
                    File compressionFile;
                    if (mGlobalSpec.getImageCompressionInterface() != null) {
                        compressionFile = mGlobalSpec.getImageCompressionInterface().compressionFile(mContext, oldFile);
                    } else {
                        compressionFile = oldFile;
                    }
                    // 移动文件,获取文件名称
                    String newFileName = item.getPath().substring(item.getPath().lastIndexOf(File.separator));
                    File newFile = mPictureMediaStoreCompat.createFile(newFileName, 0, false);
                    // new localFile
                    LocalFile localFile = new LocalFile();
                    localFile.setPath(newFile.getAbsolutePath());
                    localFile.setWidth(item.getWidth());
                    localFile.setHeight(item.getHeight());
                    localFile.setSize(compressionFile.length());
                    newFiles.add(localFile);
                    FileUtil.copy(compressionFile, newFile, null, (ioProgress, file) -> {
                        if (ioProgress >= 1) {
                            Log.d(TAG, file.getAbsolutePath());
                            // 每次迁移完一个文件的进度
                            int progress = 100 / mBitmapData.size();
                            ThreadUtils.runOnUiThread(() -> setProgress(progress));
                        }
                    });
                }
                for (LocalFile item : newFiles) {
                    if (item.getPath() != null) {
                        // 加入图片到android系统库里面
                        Uri uri = MediaStoreUtils.displayToGallery(mContext, new File(item.getPath()), TYPE_PICTURE, -1, item.getWidth(), item.getHeight(),
                                mPictureMediaStoreCompat.getSaveStrategy().getDirectory(), mPictureMediaStoreCompat);
                        // 加入相册后的最后是id，直接使用该id
                        item.setId(MediaStoreUtils.getId(uri));
                        item.setMimeType(MimeType.JPEG.getMimeTypeName());
                        item.setUri(mPictureMediaStoreCompat.getUri(item.getPath()));
                    }
                }
                // 执行完成
                Log.d(TAG, "captureSuccess");
                return newFiles;
            }

            @Override
            public void onSuccess(ArrayList<LocalFile> newFiles) {
                Log.d(TAG, "mMovePictureFileTask onSuccess");
                mIsCommit = true;
                if (mGlobalSpec.getOnResultCallbackListener() == null) {
                    Intent result = new Intent();
                    result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION_LOCAL_FILE, newFiles);
                    mActivity.setResult(RESULT_OK, result);
                } else {
                    mGlobalSpec.getOnResultCallbackListener().onResult(newFiles);
                }
                mIsCommit = true;
                mActivity.finish();
                setUiEnableTrue();
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                Log.e(TAG, t.getMessage());
                failByConfirm(t);
                setUiEnableTrue();
            }
        };
        return mMovePictureFileTask;
    }

    /**
     * 添加入数据源
     *
     * @param bitmap bitmap
     */
    @Override
    public void addCaptureData(Bitmap bitmap) {
        // 初始化数据并且存储进file
        File file = mPictureMediaStoreCompat.saveFileByBitmap(bitmap, true);
        Uri uri = mPictureMediaStoreCompat.getUri(file.getPath());
        Log.d(TAG, "file:" + file.getAbsolutePath());
        BitmapData bitmapData = new BitmapData(file.getPath(), uri, bitmap.getWidth(), bitmap.getHeight());
        // 回收bitmap
        if (bitmap.isRecycled()) {
            // 回收并且置为null
            bitmap.recycle();
        }
        // 加速回收机制
        System.gc();
        // 判断是否多个图片
        if (SelectableUtils.getImageMaxCount() > 1) {
            // 添加入数据源
            mBitmapData.add(bitmapData);
            showMultiplePicture();
        } else {
            mBitmapData.add(bitmapData);
            showSinglePicture(bitmapData, file, uri);
        }

        if (mBitmapData.size() > 0) {
            // 母窗体禁止滑动
            mActivity.showHideTableLayout(false);
        }

        // 回调接口：添加图片后剩下的相关数据
        if (mCameraSpec.getOnCaptureListener() != null) {
            mCameraSpec.getOnCaptureListener().add(mBitmapData, mBitmapData.size() - 1);
        }
    }

    /**
     * 显示单图
     *
     * @param bitmapData 显示单图数据源
     * @param file       显示单图的文件
     * @param uri        显示单图的uri
     */
    @Override
    public void showSinglePicture(BitmapData bitmapData, File file, Uri uri) {
        // 拍照  隐藏 闪光灯、右上角的切换摄像头
        setMenuVisibility(View.INVISIBLE);
        // 重置位置
        getSinglePhotoView().resetMatrix();
        getSinglePhotoView().setVisibility(View.VISIBLE);
        mGlobalSpec.getImageEngine().loadUriImage(mContext, getSinglePhotoView(), bitmapData.getUri());
        getCameraView().close();
        getPhotoVideoLayout().startTipAlphaAnimation();
        getPhotoVideoLayout().startShowLeftRightButtonsAnimator();
        mPhotoFile = file;

        // 设置当前模式是图片模式
        mCameraStateManagement.setState(mCameraStateManagement.getPictureComplete());

        // 判断是否要编辑
        if (mGlobalSpec.getImageEditEnabled()) {
            getPhotoVideoLayout().getViewHolder().rlEdit.setVisibility(View.VISIBLE);
            getPhotoVideoLayout().getViewHolder().rlEdit.setTag(uri);
        } else {
            getPhotoVideoLayout().getViewHolder().rlEdit.setVisibility(View.INVISIBLE);
        }

        // 隐藏拍照按钮
        getPhotoVideoLayout().getViewHolder().btnClickOrLong.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示多图
     */
    @Override
    public void showMultiplePicture() {
        // 显示横版列表
        if (getRecyclerViewPhoto() != null) {
            getRecyclerViewPhoto().setVisibility(View.VISIBLE);
        }

        // 显示横版列表的线条空间
        if (getMultiplePhotoView() != null) {
            for (View view : getMultiplePhotoView()) {
                view.setVisibility(View.VISIBLE);
                view.setVisibility(View.VISIBLE);
            }
        }

        // 更新最后一个添加
        mPhotoAdapter.notifyItemInserted(mPhotoAdapter.getItemCount() - 1);
        mPhotoAdapter.notifyItemRangeChanged(mPhotoAdapter.getItemCount() - 1, mPhotoAdapter.getItemCount());

        getPhotoVideoLayout().startTipAlphaAnimation();
        getPhotoVideoLayout().startOperaeBtnAnimatorMulti();

        // 重置按钮，因为每次点击，都会自动关闭
        getPhotoVideoLayout().getViewHolder().btnClickOrLong.resetState();
        // 显示右上角
        setMenuVisibility(View.VISIBLE);

        // 设置当前模式是图片休闲并存模式
        mCameraStateManagement.setState(mCameraStateManagement.getPictureMultiple());

        // 禁用长按事件，即禁止录像
        getPhotoVideoLayout().setButtonFeatures(BUTTON_STATE_ONLY_CLICK);
    }

    /**
     * 获取当前view的状态
     *
     * @return 状态
     */
    public StateInterface getState() {
        return mCameraStateManagement.getState();
    }

    /**
     * 确认提交这些多媒体数据
     *
     * @param localFiles 多媒体数据
     */
    @Override
    public void confirm(ArrayList<LocalFile> localFiles) {
        mIsCommit = true;
        if (mGlobalSpec.getOnResultCallbackListener() == null) {
            Intent result = new Intent();
            result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION_LOCAL_FILE, localFiles);
            mActivity.setResult(RESULT_OK, result);
        } else {
            mGlobalSpec.getOnResultCallbackListener().onResult(localFiles);
        }
        mIsCommit = true;
        mActivity.finish();
    }

    @Override
    public void failByConfirm(Throwable throwable) {
        getPhotoVideoLayout().setTipAlphaAnimation(throwable.getMessage());
    }

    /**
     * 取消单图后的重置
     */
    public void cancelOnResetBySinglePicture() {
        mBitmapData.clear();

        // 根据不同状态处理相应的事件
        resetStateAll();
    }

    /**
     * 结束所有当前活动，重置状态
     * 一般指完成了当前活动，或者清除所有活动的时候调用
     */
    public void resetStateAll() {
        // 重置右上角菜单
        setMenuVisibility(View.VISIBLE);

        // 重置分段录制按钮 如果启动视频编辑并且可录制数量>=0，便显示分段录制功能
        if (SelectableUtils.getVideoMaxCount() <= 0 || !mCameraSpec.isMergeEnable()) {
            getPhotoVideoLayout().getViewHolder().tvSectionRecord.setVisibility(View.GONE);
        } else {
            getPhotoVideoLayout().getViewHolder().tvSectionRecord.setVisibility(View.VISIBLE);
        }

        // 恢复底部
        showBottomMenu();

        // 隐藏大图
        getSinglePhotoView().setVisibility(View.GONE);

        // 隐藏编辑按钮
        getPhotoVideoLayout().getViewHolder().rlEdit.setVisibility(View.GONE);

        // 恢复底部按钮
        getPhotoVideoLayout().reset();
    }

    /**
     * 恢复底部菜单,母窗体启动滑动
     */
    @Override
    public void showBottomMenu() {
        mActivity.showHideTableLayout(true);
    }

    /**
     * 打开预览视频界面
     */
    public void openPreviewVideoActivity() {
        if (mIsSectionRecord && mCameraSpec.getVideoMergeCoordinator() != null) {
            // 合并视频
            mNewSectionVideoPath = mVideoMediaStoreCompat.createFile(1, true, "mp4").getPath();
            Log.d(TAG, "新的合并视频：" + mNewSectionVideoPath);
            for (String item : mVideoPaths) {
                Log.d(TAG, "新的合并视频素材：" + item);
            }
            // 合并结束后会执行 mCameraSpec.getVideoMergeCoordinator() 的相关回调
            mCameraSpec.getVideoMergeCoordinator().merge(this.getClass(), mNewSectionVideoPath, mVideoPaths,
                    mContext.getCacheDir().getPath() + File.separator + "cam.txt");
        }
    }

    /**
     * 设置界面的功能按钮可以使用
     * 场景：如果压缩或者移动文件时异常，则恢复
     */
    @Override
    public void setUiEnableTrue() {
        if (getFlashView() != null) {
            getFlashView().setEnabled(true);
        }
        if (getSwitchView() != null) {
            getSwitchView().setEnabled(true);
        }
        // 重置按钮进度
        getPhotoVideoLayout().getViewHolder().btnConfirm.reset();
    }

    /**
     * 设置界面的功能按钮禁止使用
     * 场景：确认图片时，压缩中途禁止某些功能使用
     */
    @Override
    public void setUiEnableFalse() {
        if (getFlashView() != null) {
            getFlashView().setEnabled(false);
        }
        if (getSwitchView() != null) {
            getSwitchView().setEnabled(false);
        }
    }

    public boolean isBreakOff() {
        Log.d(TAG, "isBreakOff: " + mIsBreakOff);
        return mIsBreakOff;
    }

    public void setBreakOff(boolean breakOff) {
        Log.d(TAG, "setBreakOff: " + breakOff);
        this.mIsBreakOff = breakOff;
    }

    /**
     * 记忆模式下获取闪光灯缓存的模式
     */
    private void flashGetCache() {
        // 判断闪光灯是否记忆模式，如果是记忆模式则使用上个闪光灯模式
        if (mCameraSpec.getEnableFlashMemoryModel()) {
            mFlashModel = FlashCacheUtils.getFlashModel(getContext());
        }
    }

    /**
     * 记忆模式下缓存闪光灯模式
     */
    private void flashSaveCache() {
        // 判断闪光灯是否记忆模式，如果是记忆模式则存储当前闪光灯模式
        if (mCameraSpec.getEnableFlashMemoryModel()) {
            FlashCacheUtils.saveFlashModel(getContext(), mFlashModel);
        }
    }

    /**
     * 停止录像并且完成它，如果是因为视频过短则清除冗余数据
     *
     * @param isShort 是否因为视频过短而停止
     */
    public void stopRecord(boolean isShort) {
        mCameraStateManagement.stopRecord(isShort);
    }

    /**
     * 设置右上角菜单是否显示
     */
    public void setMenuVisibility(int viewVisibility) {
        setSwitchVisibility(viewVisibility);
        if (getFlashView() != null) {
            getFlashView().setVisibility(viewVisibility);
        }
    }

    /**
     * 设置闪光灯是否显示，如果不支持，是一直不会显示
     */
    private void setSwitchVisibility(int viewVisibility) {
        if (getSwitchView() != null) {
            if (!PackageManagerUtils.isSupportCameraLedFlash(mContext.getPackageManager())) {
                getSwitchView().setVisibility(View.GONE);
            } else {
                getSwitchView().setVisibility(viewVisibility);
            }
        }
    }

    /**
     * 设置闪关灯
     */
    private void setFlashLamp() {
        if (getFlashView() != null) {
            switch (mFlashModel) {
                case TYPE_FLASH_AUTO:
                    getFlashView().setImageResource(mCameraSpec.getImageFlashAuto());
                    getCameraView().setFlash(Flash.AUTO);
                    break;
                case TYPE_FLASH_ON:
                    getFlashView().setImageResource(mCameraSpec.getImageFlashOn());
                    getCameraView().setFlash(Flash.TORCH);
                    break;
                case TYPE_FLASH_OFF:
                    getFlashView().setImageResource(mCameraSpec.getImageFlashOff());
                    getCameraView().setFlash(Flash.OFF);
                    break;
                default:
                    break;
            }
        }
    }

}
