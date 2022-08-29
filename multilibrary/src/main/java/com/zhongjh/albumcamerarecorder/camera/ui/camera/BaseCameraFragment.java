package com.zhongjh.albumcamerarecorder.camera.ui.camera;

import static android.app.Activity.RESULT_OK;
import static com.otaliastudios.cameraview.controls.Mode.PICTURE;
import static com.otaliastudios.cameraview.controls.Mode.VIDEO;
import static com.zhongjh.albumcamerarecorder.camera.constants.FlashModels.TYPE_FLASH_AUTO;
import static com.zhongjh.albumcamerarecorder.camera.constants.FlashModels.TYPE_FLASH_OFF;
import static com.zhongjh.albumcamerarecorder.camera.constants.FlashModels.TYPE_FLASH_ON;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_SELECTION_LOCAL_FILE;
import static com.zhongjh.albumcamerarecorder.widget.clickorlongbutton.ClickOrLongButton.BUTTON_STATE_BOTH;
import static com.zhongjh.albumcamerarecorder.widget.clickorlongbutton.ClickOrLongButton.BUTTON_STATE_CLICK_AND_HOLD;
import static com.zhongjh.albumcamerarecorder.widget.clickorlongbutton.ClickOrLongButton.BUTTON_STATE_ONLY_CLICK;
import static com.zhongjh.albumcamerarecorder.widget.clickorlongbutton.ClickOrLongButton.BUTTON_STATE_ONLY_LONG_CLICK;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
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

import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Audio;
import com.otaliastudios.cameraview.controls.Flash;
import com.zhongjh.albumcamerarecorder.BaseFragment;
import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.camera.constants.FlashCacheUtils;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.camera.listener.ClickOrLongListener;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.impl.ICameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.impl.ICameraView;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraPicturePresenter;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter.BaseCameraVideoPresenter;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.IState;
import com.zhongjh.albumcamerarecorder.camera.ui.previewvideo.PreviewVideoActivity;
import com.zhongjh.albumcamerarecorder.camera.util.LogUtil;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.settings.CameraSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.utils.PackageManagerUtils;
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils;
import com.zhongjh.albumcamerarecorder.widget.BaseOperationLayout;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.listener.OnMoreClickListener;
import com.zhongjh.common.utils.StatusBarUtils;
import com.zhongjh.common.utils.ThreadUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

/**
 * 一个父类的拍摄Fragment，用于开放出来给开发自定义，但是同时也需要遵守一些规范
 * 因为该类含有过多方法，所以采用多接口 + Facade 模式
 * Facade模式在presenter包里面体现出来，这个并不是传统意义上的MVP上的P
 * 只是单纯将CameraFragment里面的涉及Picture和Video的两个操作分开出来
 * 这样做的好处是为了减少一个类的代码臃肿、易于扩展维护等等
 * <p>
 * 该类主要根据两个接口实现相关方法
 * [ICameraView]:
 * 主要让开发者提供相关View的实现
 * [ICameraFragment]:
 * 主要实现除了图片、视频的其他相关方法，比如显示LoadingView、闪光灯等操作、底部菜单显示隐藏、图廊预览等等
 *
 * @author zhongjh
 * @date 2022/8/11
 */
public abstract class BaseCameraFragment
        <StateManagement extends CameraStateManagement,
                CameraPicture extends BaseCameraPicturePresenter,
                CameraVideo extends BaseCameraVideoPresenter>
        extends BaseFragment implements ICameraView, ICameraFragment {

    private static final String TAG = BaseCameraFragment.class.getSimpleName();

    private final static int MILLISECOND = 2000;

    private Context myContext;
    private MainActivity mainActivity;
    /**
     * 在图廊预览界面点击了确定
     */
    public ActivityResultLauncher<Intent> mAlbumPreviewActivityResult;

    /**
     * 公共配置
     */
    private GlobalSpec globalSpec;
    /**
     * 拍摄配置
     */
    private CameraSpec cameraSpec;
    /**
     * 闪关灯状态 默认关闭
     */
    private int flashModel = TYPE_FLASH_OFF;
    /**
     * 默认图片
     */
    private Drawable mPlaceholder;
    /**
     * 声明一个long类型变量：用于存放上一点击“返回键”的时刻
     */
    private long mExitTime;
    /**
     * 是否提交,如果不是提交则要删除冗余文件
     */
    private boolean isCommit = false;
    /**
     * 修饰多图控件的View数组
     */
    @Nullable
    private View[] multiplePhotoViews;

    /**
     * 设置状态管理,处理不同状态下进行相关逻辑
     * 有以下状态：
     * [Preview]
     * [PictureComplete] [PictureMultiple]
     * [VideoComplete] [VideoIn] [VideoMultiple] [VideoMultipleIn]
     */
    @NonNull
    public abstract StateManagement getCameraStateManagement();

    /**
     * 设置[BaseCameraPicturePresenter]，专门处理有关图片逻辑
     * 如果没有自定义，则直接返回[BaseCameraPicturePresenter]
     *
     * @return BaseCameraPicturePresenter
     */
    @NonNull
    public abstract CameraPicture getCameraPicturePresenter();

    /**
     * 设置[BaseCameraVideoPresenter]，专门处理有关视频逻辑
     * 如果没有自定义，则直接返回[BaseCameraPicturePresenter]
     *
     * @return BaseCameraVideoPresenter
     */
    @NonNull
    public abstract CameraVideo getCameraVideoPresenter();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityResult();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = setContentView(inflater,container);
        view.setOnKeyListener((v, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        initView(view, savedInstanceState);
        initData();
        setView();
        initListener();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
            this.myContext = context.getApplicationContext();
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
                Toast.makeText(mainActivity.getApplicationContext(), getResources().getString(R.string.z_multi_library_press_confirm_again_to_close), Toast.LENGTH_SHORT).show();
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
        if ((keyCode & cameraSpec.getKeyCodeTakePhoto()) > 0) {
            getCameraPicturePresenter().takePhoto();
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
        onDestroy(isCommit);
        super.onDestroy();
    }

    /**
     * 设置相关view，由子类赋值
     */
    protected void setView() {
        multiplePhotoViews = getMultiplePhotoView();

        // 如果有设置高清模式，则根据相应高清模式更改模式
        if (cameraSpec.getEnableImageHighDefinition()) {
            getCameraView().setMode(PICTURE);
        } else if (cameraSpec.getEnableVideoHighDefinition()) {
            getCameraView().setMode(VIDEO);
        } else {
            getCameraView().setMode(VIDEO);
        }

        if (cameraSpec.getWatermarkResource() != -1) {
            LayoutInflater.from(getContext()).inflate(cameraSpec.getWatermarkResource(), getCameraView(), true);
        }

        // 回调cameraView可以自定义相关参数
        if (cameraSpec.getOnCameraViewListener() != null) {
            cameraSpec.getOnCameraViewListener().onInitListener(getCameraView());
        }

        // 兼容沉倾状态栏
        if (getTopView() != null) {
            int statusBarHeight = StatusBarUtils.getStatusBarHeight(getMyContext());
            getTopView().setPadding(0, statusBarHeight, 0, 0);
            ViewGroup.LayoutParams layoutParams = getTopView().getLayoutParams();
            layoutParams.height = layoutParams.height + statusBarHeight;
        }

        // 如果启动视频编辑并且可录制数量>=0，便显示分段录制功能
        if (SelectableUtils.getVideoMaxCount() <= 0 || !cameraSpec.isMergeEnable()) {
            getPhotoVideoLayout().getViewHolder().tvSectionRecord.setVisibility(View.GONE);
        } else {
            getPhotoVideoLayout().getViewHolder().tvSectionRecord.setVisibility(View.VISIBLE);
        }

        // 处理图片、视频等需要进度显示
        getPhotoVideoLayout().getViewHolder().btnConfirm.setProgressMode(true);

        // 初始化cameraView,判断是否开启录制视频，如果开启就开启声音
        if (!SelectableUtils.videoValid()) {
            getCameraView().setAudio(Audio.OFF);
        }

        // 设置闪光灯模式
        setFlashLamp();
        if (getSwitchView() != null) {
            getSwitchView().setImageResource(cameraSpec.getImageSwitch());
        }
        // 设置录制时间
        getPhotoVideoLayout().setDuration(cameraSpec.getDuration() * 1000);
        // 最短录制时间
        getPhotoVideoLayout().setMinDuration(cameraSpec.getMinDuration());
    }

    /**
     * 初始化相关数据
     */
    protected void initData() {
        // 初始化设置
        globalSpec = GlobalSpec.INSTANCE;
        cameraSpec = CameraSpec.INSTANCE;

        getCameraPicturePresenter().initData();
        getCameraVideoPresenter().initData();

        // 默认图片
        TypedArray ta = myContext.getTheme().obtainStyledAttributes(
                new int[]{R.attr.album_thumbnail_placeholder});
        mPlaceholder = ta.getDrawable(0);

        // 闪光灯修改默认模式
        flashModel = cameraSpec.getFlashModel();
        // 记忆模式
        flashGetCache();

        // 初始化适配器
        getCameraPicturePresenter().initMultiplePhotoAdapter();
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
        getCameraVideoPresenter().initVideoEditListener();
        // 拍照监听
        initCameraViewListener();
        // 编辑图片事件
        getCameraPicturePresenter().initPhotoEditListener();
    }

    /**
     * 关闭View初始化事件
     */
    private void initCameraLayoutCloseListener() {
        if (getCloseView() != null) {
            getCloseView().setOnClickListener(new OnMoreClickListener() {
                @Override
                public void onListener(@NonNull View v) {
                    getCameraVideoPresenter().setBreakOff(true);
                    mainActivity.finish();
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
                flashModel++;
                if (flashModel > TYPE_FLASH_OFF) {
                    flashModel = TYPE_FLASH_AUTO;
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
     * 主按钮监听,拍摄和录制的触发源头事件
     * onClick() 即代表触发拍照
     * onLongClick() 即代表触发录制
     */
    private void initPvLayoutPhotoVideoListener() {
        getPhotoVideoLayout().setPhotoVideoListener(new ClickOrLongListener() {
            @Override
            public void actionDown() {
                Log.d(TAG, "pvLayout actionDown");
                // 母窗体隐藏底部滑动
                mainActivity.showHideTableLayout(false);
            }

            @Override
            public void onClick() {
                Log.d(TAG, "pvLayout onClick");
                getCameraPicturePresenter().takePhoto();
            }

            @Override
            public void onLongClickShort(final long time) {
                Log.d(TAG, "pvLayout onLongClickShort");
                longClickShort(time);
            }

            @Override
            public void onLongClick() {
                Log.d(TAG, "pvLayout onLongClick ");
                // 开启录制功能才能录制事件
                if (getCameraView().isOpened()) {
                    getCameraVideoPresenter().recordVideo();
                    // 设置录制状态
                    if (getCameraVideoPresenter().isSectionRecord()) {
                        getCameraStateManagement().setState(getCameraStateManagement().getVideoMultipleIn());
                    } else {
                        getCameraStateManagement().setState(getCameraStateManagement().getVideoIn());
                    }
                    // 开始录像
                    setMenuVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onLongClickEnd(long time) {
                Log.d(TAG, "pvLayout onLongClickEnd " + time);
                getCameraVideoPresenter().setSectionRecordTime(time);
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
                if (getCameraVideoPresenter().isSectionRecord()) {
                    getPhotoVideoLayout().setTipAlphaAnimation(getResources().getString(R.string.z_multi_library_working_video_click_later));
                }
            }

            @Override
            public void onClickStopTips() {
                if (getCameraVideoPresenter().isSectionRecord()) {
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
                getCameraStateManagement().pvLayoutCancel();
            }

            @Override
            public void confirm() {
                Log.d(TAG, "confirm " + getState().toString());
                getCameraStateManagement().pvLayoutCommit();
            }

            @Override
            public void startProgress() {
                Log.d(TAG, "startProgress " + getState().toString());
                getCameraStateManagement().pvLayoutCommit();
            }

            @Override
            public void stopProgress() {
                Log.d(TAG, "stopProgress " + getState().toString());
                getCameraStateManagement().stopProgress();
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
            getCameraVideoPresenter().setSectionRecord("1".equals(tag));
            getPhotoVideoLayout().setProgressMode(true);
        });
    }

    /**
     * 拍照、录制监听
     */
    private void initCameraViewListener() {
        getCameraView().addCameraListener(new CameraListener() {

            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                // 如果是自动闪光灯模式便关闭闪光灯
                if (flashModel == TYPE_FLASH_AUTO) {
                    getCameraView().setFlash(Flash.OFF);
                }
                result.toBitmap(bitmap -> {
                    // 显示图片
                    getCameraPicturePresenter().addCaptureData(bitmap);
                    // 恢复点击
                    getChildClickableLayout().setChildClickable(true);
                });
                super.onPictureTaken(result);
            }

            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                Log.d(TAG, "onVideoTaken");
                super.onVideoTaken(result);
                getCameraVideoPresenter().onVideoTaken(result);
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
                if (getCameraVideoPresenter().isSectionRecord()) {
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
     * 初始化Activity回调
     */
    private void initActivityResult() {
        // 在图廊预览界面点击了确定
        mAlbumPreviewActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            boolean isReturn = initActivityResult(result.getResultCode());
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
                    getCameraPicturePresenter().refreshMultiPhoto(bitmapDatas);
                }
            }
        });
        getCameraVideoPresenter().initActivityResult();
        getCameraPicturePresenter().initActivityResult();
    }

    /**
     * 返回true的时候即是纸条跳过了后面的ActivityResult事件
     *
     * @param resultCode Activity的返回码
     * @return 返回true是跳过，返回false则是继续
     */
    public boolean initActivityResult(int resultCode) {
        return getCameraStateManagement().onActivityResult(resultCode);
    }

    /**
     * 生命周期onDestroy
     *
     * @param isCommit 是否提交了数据,如果不是提交则要删除冗余文件
     */
    protected void onDestroy(boolean isCommit) {
        LogUtil.i("CameraLayout destroy");
        getCameraPicturePresenter().onDestroy(isCommit);
        getCameraVideoPresenter().onDestroy(isCommit);
        getPhotoVideoLayout().getViewHolder().btnConfirm.reset();
        getCameraView().destroy();
        // 记忆模式
        flashSaveCache();
        cameraSpec.setOnCaptureListener(null);
    }

    /**
     * 提交图片成功后，返回数据给上一个页面
     */
    @Override
    public void commitPictureSuccess(ArrayList<LocalFile> newFiles) {
        Log.d(TAG, "mMovePictureFileTask onSuccess");
        isCommit = true;
        if (globalSpec.getOnResultCallbackListener() == null) {
            Intent result = new Intent();
            result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION_LOCAL_FILE, newFiles);
            mainActivity.setResult(RESULT_OK, result);
        } else {
            globalSpec.getOnResultCallbackListener().onResult(newFiles);
        }
        mainActivity.finish();
        setUiEnableTrue();
    }

    /**
     * 提交图片失败后
     *
     * @param throwable 异常
     */
    @Override
    public void commitFail(Throwable throwable) {
        getPhotoVideoLayout().setTipAlphaAnimation(throwable.getMessage());
        setUiEnableTrue();
    }

    /**
     * 提交视频成功后，返回数据给上一个页面
     *
     * @param intentPreviewVideo 从预览视频界面返回来的数据intent
     */
    @Override
    public void commitVideoSuccess(Intent intentPreviewVideo) {
        ArrayList<LocalFile> localFiles = new ArrayList<>();
        LocalFile localFile = intentPreviewVideo.getParcelableExtra(PreviewVideoActivity.LOCAL_FILE);
        localFiles.add(localFile);
        isCommit = true;
        if (globalSpec.getOnResultCallbackListener() == null) {
            // 获取视频路径
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION_LOCAL_FILE, localFiles);
            mainActivity.setResult(RESULT_OK, intent);
        } else {
            globalSpec.getOnResultCallbackListener().onResult(localFiles);
        }
        mainActivity.finish();
    }

    /**
     * 打开预览图片
     *
     * @param intent 包含数据源
     */
    public void openAlbumPreviewActivity(Intent intent) {
        mAlbumPreviewActivityResult.launch(intent);
        if (globalSpec.getCutscenesEnabled()) {
            if (getActivity() != null) {
                getActivity().overridePendingTransition(R.anim.activity_open_zjh, 0);
            }
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
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());

        showBottomMenu();
    }

    /**
     * 录制时间过短
     */
    private void longClickShort(final long time) {
        Log.d(TAG, "longClickShort " + time);
        getCameraStateManagement().longClickShort(time);
        // 提示过短
        getPhotoVideoLayout().setTipAlphaAnimation(getResources().getString(R.string.z_multi_library_the_recording_time_is_too_short));
        // 显示右上角菜单
        setMenuVisibility(View.VISIBLE);
        // 停止录像
        stopRecord(true);
    }

    /**
     * 初始化中心按钮状态
     */
    protected void initPvLayoutButtonFeatures() {
        // 判断点击和长按的权限
        if (cameraSpec.isClickRecord()) {
            // 禁用长按功能
            getPhotoVideoLayout().setButtonFeatures(BUTTON_STATE_CLICK_AND_HOLD);
            getPhotoVideoLayout().setTip(getResources().getString(R.string.z_multi_library_light_touch_camera));
        } else {
            if (cameraSpec.onlySupportImages()) {
                // 禁用长按功能
                getPhotoVideoLayout().setButtonFeatures(BUTTON_STATE_ONLY_CLICK);
                getPhotoVideoLayout().setTip(getResources().getString(R.string.z_multi_library_light_touch_take));
            } else if (cameraSpec.onlySupportVideos()) {
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
        ThreadUtils.executeByIo(getCameraPicturePresenter().getMovePictureFileTask());
    }

    /**
     * 针对单图进行相关UI变化
     *
     * @param bitmapData 显示单图数据源
     * @param file       显示单图的文件
     * @param uri        显示单图的uri
     */
    @Override
    public void showSinglePicture(BitmapData bitmapData, File file, Uri uri) {
        // 拍照  隐藏 闪光灯、右上角的切换摄像头
        setMenuVisibility(View.INVISIBLE);
        // 这样可以重置
        getSinglePhotoView().setZoomable(true);
        getSinglePhotoView().setVisibility(View.VISIBLE);
        globalSpec.getImageEngine().loadUriImage(myContext, getSinglePhotoView(), bitmapData.getUri());
        getCameraView().close();
        getPhotoVideoLayout().startTipAlphaAnimation();
        getPhotoVideoLayout().startShowLeftRightButtonsAnimator();

        // 设置当前模式是图片模式
        getCameraStateManagement().setState(getCameraStateManagement().getPictureComplete());

        // 判断是否要编辑
        if (globalSpec.getImageEditEnabled()) {
            getPhotoVideoLayout().getViewHolder().rlEdit.setVisibility(View.VISIBLE);
            getPhotoVideoLayout().getViewHolder().rlEdit.setTag(uri);
        } else {
            getPhotoVideoLayout().getViewHolder().rlEdit.setVisibility(View.INVISIBLE);
        }

        // 隐藏拍照按钮
        getPhotoVideoLayout().getViewHolder().btnClickOrLong.setVisibility(View.INVISIBLE);
    }

    /**
     * 针对多图进行相关UI变化
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

        getPhotoVideoLayout().startTipAlphaAnimation();
        getPhotoVideoLayout().startOperaeBtnAnimatorMulti();

        // 重置按钮，因为每次点击，都会自动关闭
        getPhotoVideoLayout().getViewHolder().btnClickOrLong.resetState();
        // 显示右上角
        setMenuVisibility(View.VISIBLE);

        // 设置当前模式是图片休闲并存模式
        getCameraStateManagement().setState(getCameraStateManagement().getPictureMultiple());

        // 禁用长按事件，即禁止录像
        getPhotoVideoLayout().setButtonFeatures(BUTTON_STATE_ONLY_CLICK);
    }

    /**
     * 获取当前view的状态
     *
     * @return 状态
     */
    public IState getState() {
        return getCameraStateManagement().getState();
    }

    /**
     * 取消单图后的重置
     */
    public void cancelOnResetBySinglePicture() {
        getCameraPicturePresenter().clearBitmapDatas();

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
        if (SelectableUtils.getVideoMaxCount() <= 0 || !cameraSpec.isMergeEnable()) {
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
        mainActivity.showHideTableLayout(true);
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
            if (!PackageManagerUtils.isSupportCameraLedFlash(myContext.getPackageManager())) {
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
            switch (flashModel) {
                case TYPE_FLASH_AUTO:
                    getFlashView().setImageResource(cameraSpec.getImageFlashAuto());
                    getCameraView().setFlash(Flash.AUTO);
                    break;
                case TYPE_FLASH_ON:
                    getFlashView().setImageResource(cameraSpec.getImageFlashOn());
                    getCameraView().setFlash(Flash.TORCH);
                    break;
                case TYPE_FLASH_OFF:
                    getFlashView().setImageResource(cameraSpec.getImageFlashOff());
                    getCameraView().setFlash(Flash.OFF);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 记忆模式下获取闪光灯缓存的模式
     */
    private void flashGetCache() {
        // 判断闪光灯是否记忆模式，如果是记忆模式则使用上个闪光灯模式
        if (cameraSpec.getEnableFlashMemoryModel()) {
            flashModel = FlashCacheUtils.getFlashModel(getContext());
        }
    }

    /**
     * 记忆模式下缓存闪光灯模式
     */
    private void flashSaveCache() {
        // 判断闪光灯是否记忆模式，如果是记忆模式则存储当前闪光灯模式
        if (cameraSpec.getEnableFlashMemoryModel()) {
            FlashCacheUtils.saveFlashModel(getContext(), flashModel);
        }
    }

    /**
     * 多视频分段录制中止提交
     */
    public void stopVideoMultiple() {
        if (cameraSpec.isMergeEnable() && cameraSpec.getVideoMergeCoordinator() != null) {
            cameraSpec.getVideoMergeCoordinator().onMergeDispose(this.getClass());
        }
    }

    /**
     * 停止录像并且完成它，如果是因为视频过短则清除冗余数据
     *
     * @param isShort 是否因为视频过短而停止
     */
    public void stopRecord(boolean isShort) {
        getCameraStateManagement().stopRecord(isShort);
    }

    public int getFlashModel() {
        return flashModel;
    }

    public Context getMyContext() {
        return myContext;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public GlobalSpec getGlobalSpec() {
        return globalSpec;
    }

    public CameraSpec getCameraSpec() {
        return cameraSpec;
    }
}
