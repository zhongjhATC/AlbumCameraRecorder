package com.zhongjh.albumcamerarecorder.camera;

import static android.app.Activity.RESULT_OK;
import static com.zhongjh.albumcamerarecorder.camera.constants.FlashModels.TYPE_FLASH_OFF;
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
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.otaliastudios.cameraview.CameraView;
import com.zhongjh.albumcamerarecorder.BaseFragment;
import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.camera.adapter.PhotoAdapter;
import com.zhongjh.albumcamerarecorder.camera.adapter.PhotoAdapterListener;
import com.zhongjh.albumcamerarecorder.camera.camerastate.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.constants.FlashCacheUtils;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;
import com.zhongjh.albumcamerarecorder.camera.widget.PhotoVideoLayout;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.settings.CameraSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.utils.MediaStoreUtils;
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils;
import com.zhongjh.albumcamerarecorder.widget.ImageViewTouch;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.enums.MimeType;
import com.zhongjh.common.listener.OnMoreClickListener;
import com.zhongjh.common.utils.MediaStoreCompat;
import com.zhongjh.common.utils.ThreadUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个父类的拍摄Fragment，用于开放出来给开发自定义，但是同时也需要遵守一些规范
 *
 * @author zhongjh
 * @date 2022/8/11
 */
public abstract class BaseCameraFragment extends BaseFragment implements PhotoAdapterListener, ICameraFragment {

    private static final String TAG = BaseCameraFragment.class.getSimpleName();

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

    private Context mContext;
    private MainActivity mActivity;

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
     * 用于延迟隐藏的事件，如果不用延迟，会有短暂闪屏现象
     */
    private final Handler mCameraViewGoneHandler = new Handler(Looper.getMainLooper());
    /**
     * 用于延迟显示的事件，如果不用延迟，会有短暂闪屏现象
     */
    private final Handler mCameraViewVisibleHandler = new Handler(Looper.getMainLooper());
    /**
     * 延迟拍摄，用于打开闪光灯再拍摄
     */
    private final Handler mCameraTakePictureHandler = new Handler(Looper.getMainLooper());
    private final Runnable mCameraViewGoneRunnable = new Runnable() {
        @Override
        public void run() {
            // 隐藏cameraView
            getCameraView().close();
        }
    };
    private final Runnable mCameraViewVisibleRunnable = new Runnable() {
        @Override
        public void run() {
            getCameraView().open();
        }
    };
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
        multiplePhotoViews = getMultiplePhotoView();
        singlePhotoViews = getSinglePhotoView();
        setView();
        init();
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

    /**
     * 初始化根布局
     *
     * @return 布局layout的id
     */

    public abstract int setContentView();

    /**
     * 初始化相关view
     *
     * @param view               初始化好的view
     * @param savedInstanceState savedInstanceState
     */
    public abstract void initView(View view, Bundle savedInstanceState);

    /**
     * 设置相关view，由子类赋值
     */
    protected void setView() {
        getCameraView();
    }

    /**
     * 初始化相关数据
     */
    protected void init() {
        // 初始化设置
        mGlobalSpec = GlobalSpec.INSTANCE;
        mCameraSpec = CameraSpec.INSTANCE;
        mCameraStateManagement = new CameraStateManagement(this);
        onActivityResult();
        // 设置图片路径
        if (mGlobalSpec.getPictureStrategy() != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            mPictureMediaStoreCompat = new MediaStoreCompat(getContext(), mGlobalSpec.getPictureStrategy());
        } else {
            // 否则使用全局的
            if (mGlobalSpec.getSaveStrategy() == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mPictureMediaStoreCompat = new MediaStoreCompat(getContext(), mGlobalSpec.getSaveStrategy());
            }
        }
        // 设置视频路径
        if (mGlobalSpec.getVideoStrategy() != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            mVideoMediaStoreCompat = new MediaStoreCompat(getContext(), mGlobalSpec.getVideoStrategy());
        } else {
            // 否则使用全局的
            if (mGlobalSpec.getSaveStrategy() == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                mVideoMediaStoreCompat = new MediaStoreCompat(getContext(), mGlobalSpec.getSaveStrategy());
            }
        }

        // 默认图片
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(
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
        // 关闭View
        initCameraLayoutCloseListener();
        initCameraLayoutOperateCameraListener();
        initCameraLayoutCaptureListener();
        initCameraLayoutEditListener();
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
     * 拍照录像 操作按钮的Listener
     */
    private void initCameraLayoutOperateCameraListener() {

    }

    /**
     * 设置CameraView
     *
     * @return 返回CameraView，主要用于拍摄、录制，里面包含水印
     */
    @NonNull
    public abstract CameraView getCameraView();

    /**
     * 当想使用自带的多图显示控件，请设置它
     *
     * @return 返回多图的Recycler显示控件
     */
    @Nullable
    public abstract RecyclerView getRecyclerViewPhoto();

    /**
     * 修饰多图控件的View，只有第一次初始化有效
     * 一般用于群体隐藏和显示
     * 你也可以重写[hideViewByMultipleZero]方法自行隐藏显示相关view
     *
     * @return View[]
     */
    @Nullable
    public abstract View[] getMultiplePhotoView();

    /**
     * 当想使用自带的功能按钮（包括拍摄、录制、录音、确认、取消），请设置它
     *
     * @return PhotoVideoLayout
     */
    @Nullable
    public abstract PhotoVideoLayout getPhotoVideoLayout();

    /**
     * 单图控件的View
     * 你也可以重写[hideViewByMultipleZero]方法自行隐藏显示相关view
     *
     * @return View
     */
    @Nullable
    public abstract ImageViewTouch getSinglePhotoView();

    /**
     * 左上角的关闭控件
     *
     * @return View
     */
    @Nullable
    public abstract View getCloseView();

    /**
     * 右上角的关闭控件
     *
     * @return View
     */
    @Nullable
    public abstract View getFlashView();

    /**
     * 右上角的切换前置/后置摄像控件
     *
     * @return View
     */
    @Nullable
    public abstract View getSwitchView();

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

        if (getPhotoVideoLayout() != null) {
            // 隐藏左右侧按钮
            getPhotoVideoLayout().getViewHolder().btnCancel.setVisibility(View.GONE);
            getPhotoVideoLayout().getViewHolder().btnConfirm.setVisibility(View.GONE);

            // 如果是单图编辑情况下,隐藏编辑按钮
            getPhotoVideoLayout().getViewHolder().rlEdit.setVisibility(View.GONE);

            // 恢复长按事件，即重新启用录制
            getPhotoVideoLayout().getViewHolder().btnClickOrLong.setVisibility(View.VISIBLE);
            initPvLayoutButtonFeatures();
        }

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
     * 初始化中心按钮状态
     */
    protected void initPvLayoutButtonFeatures() {
        if (getPhotoVideoLayout() == null) {
            return;
        }
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
        if (getPhotoVideoLayout() != null) {
            getPhotoVideoLayout().getViewHolder().btnConfirm.setProgress(1);
        }
    }

    @Override
    public void setProgress(int progress) {
        if (getPhotoVideoLayout() != null) {
            getPhotoVideoLayout().getViewHolder().btnConfirm.addProgress(progress);
        }
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
        if (getPhotoVideoLayout() != null) {
            getPhotoVideoLayout().setTipAlphaAnimation(throwable.getMessage());
        }
    }

    /**
     * 恢复底部菜单,母窗体启动滑动
     */
    @Override
    public void showBottomMenu() {
        mActivity.showHideTableLayout(true);
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
        if (getPhotoVideoLayout() != null) {
            getPhotoVideoLayout().getViewHolder().btnConfirm.reset();
        }
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


}
