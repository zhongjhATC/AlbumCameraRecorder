package com.zhongjh.multimedia.camera.ui.camera;

import static android.app.Activity.RESULT_OK;
import static androidx.camera.core.ImageCapture.FLASH_MODE_AUTO;
import static androidx.camera.core.ImageCapture.FLASH_MODE_OFF;
import static androidx.camera.core.ImageCapture.FLASH_MODE_ON;
import static com.zhongjh.multimedia.widget.clickorlongbutton.ClickOrLongButton.BUTTON_STATE_BOTH;
import static com.zhongjh.multimedia.widget.clickorlongbutton.ClickOrLongButton.BUTTON_STATE_CLICK_AND_HOLD;
import static com.zhongjh.multimedia.widget.clickorlongbutton.ClickOrLongButton.BUTTON_STATE_ONLY_CLICK;
import static com.zhongjh.multimedia.widget.clickorlongbutton.ClickOrLongButton.BUTTON_STATE_ONLY_LONG_CLICK;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.zhongjh.common.entity.LocalMedia;
import com.zhongjh.common.listener.OnMoreClickListener;
import com.zhongjh.common.utils.StatusBarUtils;
import com.zhongjh.common.utils.ThreadUtils;
import com.zhongjh.multimedia.BaseFragment;
import com.zhongjh.multimedia.MainActivity;
import com.zhongjh.multimedia.R;
import com.zhongjh.multimedia.camera.constants.FlashCacheUtils;
import com.zhongjh.multimedia.camera.entity.BitmapData;
import com.zhongjh.multimedia.camera.listener.ClickOrLongListener;
import com.zhongjh.multimedia.camera.listener.OnCameraManageListener;
import com.zhongjh.multimedia.camera.ui.camera.impl.ICameraFragment;
import com.zhongjh.multimedia.camera.ui.camera.impl.ICameraView;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraManage;
import com.zhongjh.multimedia.camera.ui.camera.manager.CameraVideoManager;
import com.zhongjh.multimedia.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.multimedia.camera.ui.camera.state.IState;
import com.zhongjh.multimedia.camera.ui.preview.video.PreviewVideoActivity;
import com.zhongjh.multimedia.camera.util.LogUtil;
import com.zhongjh.multimedia.model.SelectedData;
import com.zhongjh.multimedia.settings.CameraSpec;
import com.zhongjh.multimedia.settings.GlobalSpec;
import com.zhongjh.multimedia.utils.PackageManagerUtils;
import com.zhongjh.multimedia.utils.SelectableUtils;
import com.zhongjh.multimedia.widget.BaseOperationLayout;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

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
        <StateManager extends CameraStateManager,
                PictureManager extends com.zhongjh.multimedia.camera.ui.camera.manager.CameraPictureManager,
                VideoManager extends CameraVideoManager>
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
    public CameraManage cameraManage;
    /**
     * 闪关灯状态 默认关闭
     */
    private int flashMode = FLASH_MODE_OFF;
    /**
     * 请求权限的回调
     */
    ActivityResultLauncher<String[]> mRequestPermissionActivityResult;
    /**
     * 声明一个long类型变量：用于存放上一点击“返回键”的时刻
     */
    private long mExitTime;
    /**
     * 是否提交,如果不是提交则要删除冗余文件
     */
    private boolean isCommit = false;
    private boolean isShowContinueTip = false;
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
    public abstract StateManager getCameraStateManager();

    /**
     * 设置[BaseCameraPicturePresenter]，专门处理有关图片逻辑
     * 如果没有自定义，则直接返回[BaseCameraPicturePresenter]
     *
     * @return BaseCameraPicturePresenter
     */
    @NonNull
    public abstract PictureManager getCameraPictureManager();

    /**
     * 设置[BaseCameraVideoPresenter]，专门处理有关视频逻辑
     * 如果没有自定义，则直接返回[BaseCameraPicturePresenter]
     *
     * @return BaseCameraVideoPresenter
     */
    @NonNull
    public abstract VideoManager getCameraVideoManager();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityResult();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = setContentView(inflater, container);
        view.setOnKeyListener((v, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        initView(view, savedInstanceState);
        cameraManage = new CameraManage(getMainActivity(), getPreviewView(), getFocusView());
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
        Boolean isTrue = getCameraStateManager().onBackPressed();
        if (isTrue != null) {
            return isTrue;
        } else {
            // 与上次点击返回键时刻作差，第一次不能立即退出
            if ((System.currentTimeMillis() - mExitTime) > MILLISECOND) {
                // 大于2000ms则认为是误操作，使用Toast进行提示
                Toast.makeText(getMyContext(), getResources().getString(R.string.z_multi_library_press_confirm_again_to_close), Toast.LENGTH_SHORT).show();
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
            getCameraPictureManager().takePhoto();
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
        cameraManage.onResume();
        LogUtil.i("CameraLayout onResume");
        // 清空进度，防止正在进度中突然按home键
        getPhotoVideoLayout().getViewHolder().btnClickOrLong.reset();
        // 重置当前按钮的功能
        initPvLayoutButtonFeatures();
    }

    /**
     * 生命周期onPause
     */
    @Override
    public void onPause() {
        super.onPause();
        cameraManage.onPause();
        LogUtil.i("CameraLayout onPause");
    }

    @Override
    public void onDestroy() {
        onDestroy(isCommit);
        getPhotoVideoLayout().onDestroy();
        cameraManage.onDestroy();
        super.onDestroy();
    }

    /**
     * 设置相关view，由子类赋值
     */
    protected void setView() {
        multiplePhotoViews = getMultiplePhotoView();
        cameraManage.init();

//        // 水印资源 TODO
//        if (cameraSpec.getWatermarkResource() != -1) {
//            LayoutInflater.from(getContext()).inflate(cameraSpec.getWatermarkResource(), cameraManage, true);
//        }

//        // 回调cameraView可以自定义相关参数 TODO
//        if (cameraSpec.getOnCameraViewListener() != null) {
//            cameraSpec.getOnCameraViewListener().onInitListener(cameraManage);
//        }

        // 兼容沉倾状态栏
        if (getTopView() != null) {
            int statusBarHeight = StatusBarUtils.getStatusBarHeight(requireActivity());
            getTopView().setPadding(0, statusBarHeight, 0, 0);
            ViewGroup.LayoutParams layoutParams = getTopView().getLayoutParams();
            layoutParams.height = layoutParams.height + statusBarHeight;
        }

        // 处理图片、视频等需要进度显示
        getPhotoVideoLayout().getViewHolder().btnConfirm.setProgressMode(true);

        // 初始化cameraView,判断是否开启录制视频，如果开启就开启录制声音x
        cameraManage.setAudio(SelectableUtils.videoValid());
        if (getSwitchView() != null) {
            getSwitchView().setImageResource(cameraSpec.getImageSwitch());
        }
        // 设置录制时间
        getPhotoVideoLayout().setDuration(cameraSpec.getMaxDuration());
    }

    /**
     * 初始化相关数据
     */
    protected void initData() {
        // 初始化设置
        globalSpec = GlobalSpec.INSTANCE;
        cameraSpec = CameraSpec.INSTANCE;

        // 闪光灯修改默认模式
        flashMode = cameraSpec.getFlashMode();
        // 记忆模式
        flashGetCache();

        // 初始化适配器
        getCameraPictureManager().initMultiplePhotoAdapter();
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
        // 拍照监听
        initCameraViewListener();
        // 编辑图片事件
        getCameraPictureManager().initPhotoEditListener();
    }

    /**
     * 关闭View初始化事件
     */
    private void initCameraLayoutCloseListener() {
        if (getCloseView() != null) {
            getCloseView().setOnClickListener(new OnMoreClickListener() {
                @Override
                public void onListener(@NonNull View v) {
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
                flashMode++;
                if (flashMode > FLASH_MODE_OFF) {
                    flashMode = FLASH_MODE_AUTO;
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
            getSwitchView().setOnClickListener(v -> cameraManage.toggleFacing());
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
                getCameraPictureManager().takePhoto();
            }

            @Override
            public void onLongClick() {
                Log.d(TAG, "pvLayout onLongClick ");
                getCameraVideoManager().recordVideo();
                // 设置录制状态
                getCameraStateManager().setState(getCameraStateManager().getVideoMultipleIn());
                // 开始录像
                setMenuVisibility(View.INVISIBLE);
            }

            @Override
            public void onLongClickEnd(long time) {
                Log.d(TAG, "pvLayout onLongClickEnd ");
                // 录像暂停
                pauseRecord();
            }

            @Override
            public void onLongClickFinish() {
                Log.d(TAG, "pvLayout onLongClickFinish ");
                getState().onLongClickFinish();
            }

            @Override
            public void onLongClickError() {
                Log.d(TAG, "pvLayout onLongClickError ");
            }

            @Override
            public void onBanClickTips() {
                // 判断如果是分段录制模式就提示
                getPhotoVideoLayout().setTipAlphaAnimation(getResources().getString(R.string.z_multi_library_working_video_click_later));
            }

            @Override
            public void onClickStopTips() {
                getPhotoVideoLayout().setTipAlphaAnimation(getResources().getString(R.string.z_multi_library_touch_your_suspension));
            }
        });
    }

    /**
     * 左右两个按钮：确认和取消
     */
    private void initPvLayoutOperateListener() {
        getPhotoVideoLayout().setOperateListener(new BaseOperationLayout.OperateListener() {
            @Override
            public boolean beforeConfirm() {
                return requestPermissions();
            }

            @Override
            public void cancel() {
                Log.d(TAG, "cancel " + getState().toString());
                getCameraStateManager().pvLayoutCancel();
            }

            @Override
            public void startProgress() {
                Log.d(TAG, "startProgress " + getState().toString());
                // 没有所需要请求的权限，就进行后面的逻辑
                getCameraStateManager().pvLayoutCommit();
            }

            @Override
            public void stopProgress() {
                Log.d(TAG, "stopProgress " + getState().toString());
                getCameraStateManager().stopProgress();
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
     * 拍照、录制监听
     */
    private void initCameraViewListener() {
        cameraManage.setOnCameraManageListener(new OnCameraManageListener() {

            @Override
            public void onRecordStart() {
                getCameraVideoManager().onRecordStart();
            }

            @Override
            public void onActivityPause() {
                // 重置View
                getCameraStateManager().onActivityPause();
            }

            @Override
            public void onPictureSuccess(@NonNull String path) {
                Log.d(TAG, "onPictureSuccess");
                if (cameraSpec.getOnInitCameraManager() != null) {
                    cameraSpec.getOnInitCameraManager().initWatermarkedImage(path);
                }
                // 显示图片
                getCameraPictureManager().addCaptureData(path);
                // 恢复点击
                getChildClickableLayout().setChildClickable(true);
            }

            @Override
            public void bindSucceed() {
                // 设置闪光灯模式
                setFlashLamp();
            }

            @Override
            public void onRecordSuccess(@NonNull String path) {
                Log.d(TAG, "onRecordSuccess");
                // 处理视频文件,最后会解除《禁止点击》
                getCameraVideoManager().onRecordSuccess(path);
            }

            @Override
            public void onRecordPause(long recordedDurationNanos) {
                // 处理暂停,最后会解除《禁止点击》
                getCameraVideoManager().onRecordPause(recordedDurationNanos);
            }

            @Override
            public void onError(int errorCode, @Nullable String message, @Nullable Throwable cause) {
                Toast.makeText(getMyContext(), message, Toast.LENGTH_LONG).show();
            }

        });
    }

    /**
     * 初始化Activity回调
     */
    private void initActivityResult() {
        // 在图廊预览界面点击了确定
        mAlbumPreviewActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() == null) {
                    return;
                }
                // 获取选择的数据
                ArrayList<LocalMedia> selected;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    selected = result.getData().getParcelableArrayListExtra(SelectedData.STATE_SELECTION, LocalMedia.class);
                } else {
                    selected = result.getData().getParcelableArrayListExtra(SelectedData.STATE_SELECTION);
                }
                if (selected == null) {
                    return;
                }
                // 重新赋值
                ArrayList<BitmapData> bitmapDataArrayList = new ArrayList<>();
                for (LocalMedia item : selected) {
                    // 如果有编辑图片,则将该图片覆盖最新的拍照图片
                    String path = null == item.getEditorPath() ? item.getAbsolutePath() : item.getEditorPath();
                    String uri = Uri.fromFile(new File(path)).toString();
                    BitmapData bitmapData = new BitmapData(item.getFileId(), uri, path);
                    bitmapDataArrayList.add(bitmapData);
                }
                // 全部刷新
                getCameraPictureManager().refreshMultiPhoto(bitmapDataArrayList);
            }
        });
        // 创建权限申请回调
        mRequestPermissionActivityResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != null
                    && result.get(Manifest.permission.READ_EXTERNAL_STORAGE) != null) {
                if (Objects.requireNonNull(result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE)).equals(true)
                        && Objects.requireNonNull(result.get(Manifest.permission.READ_EXTERNAL_STORAGE)).equals(true)) {
                    //权限全部获取到之后的动作
                    getCameraStateManager().pvLayoutCommit();
                }
            }
        });
        getCameraVideoManager().initActivityResult();
        getCameraPictureManager().initActivityResult();
    }

    /**
     * 生命周期onDestroy
     *
     * @param isCommit 是否提交了数据,如果不是提交则要删除冗余文件
     */
    protected void onDestroy(boolean isCommit) {
        try {
            LogUtil.i("CameraLayout destroy");
            getCameraPictureManager().onDestroy(isCommit);
            getPhotoVideoLayout().getViewHolder().btnConfirm.reset();
            cameraManage.onDestroy();
            // 记忆模式
            flashSaveCache();
            cameraSpec.setOnCaptureListener(null);
            cameraSpec.setOnInitCameraManager(null);
        } catch (NullPointerException ignored) {

        }
    }

    /**
     * 提交图片成功后，返回数据给上一个页面
     *
     * @param newFiles 新的文件
     */
    @Override
    public void commitPictureSuccess(ArrayList<LocalMedia> newFiles) {
        Log.d(TAG, "mMovePictureFileTask onSuccess");
        isCommit = true;
        Intent result = new Intent();
        result.putParcelableArrayListExtra(SelectedData.STATE_SELECTION, newFiles);
        mainActivity.setResult(RESULT_OK, result);
        mainActivity.finish();
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

    @Override
    public void cancel() {
        setUiEnableTrue();
    }

    /**
     * 提交视频成功后，返回数据给上一个页面
     *
     * @param intentPreviewVideo 从预览视频界面返回来的数据intent
     */
    @Override
    public void commitVideoSuccess(Intent intentPreviewVideo) {
        ArrayList<LocalMedia> localMedias = new ArrayList<>();
        LocalMedia localMedia = intentPreviewVideo.getParcelableExtra(PreviewVideoActivity.LOCAL_FILE);
        localMedias.add(localMedia);
        isCommit = true;
        // 获取视频路径
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(SelectedData.STATE_SELECTION, localMedias);
        mainActivity.setResult(RESULT_OK, intent);
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
        getCameraStateManager().setState(getCameraStateManager().getPreview());

        showBottomMenu();
    }

    /**
     * 提示过短
     */
    public void setShortTip() {
        // 提示过短
        getPhotoVideoLayout().setTipAlphaAnimation(getResources().getString(R.string.z_multi_library_the_recording_time_is_too_short));
        // 显示右上角菜单
        setMenuVisibility(View.VISIBLE);
    }

    /**
     * 长按继续录制
     */
    public void setShortTipLongRecording() {
        if (!isShowContinueTip) {
            // 长按继续录制
            getPhotoVideoLayout().setTipAlphaAnimation(getResources().getString(R.string.z_long_press_to_continue_recording));
            isShowContinueTip = true;
        }
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
        ThreadUtils.executeByIo(getCameraPictureManager().getMovePictureFileTask(cameraSpec.getSandboxDir()));
    }

    /**
     * 针对单图进行相关UI变化
     *
     * @param bitmapData 显示单图数据源
     * @param file       显示单图的文件
     * @param uri        显示单图的uri
     */
    @Override
    public void showSinglePicture(BitmapData bitmapData, File file, String uri) {
        // 拍照  隐藏 闪光灯、右上角的切换摄像头
        setMenuVisibility(View.INVISIBLE);
        // 这样可以重置
        getSinglePhotoView().setZoomable(true);
        getSinglePhotoView().setVisibility(View.VISIBLE);
        globalSpec.getImageEngine().loadUriImage(myContext, getSinglePhotoView(), bitmapData.getAbsolutePath());
        getPhotoVideoLayout().startTipAlphaAnimation();
        getPhotoVideoLayout().startShowLeftRightButtonsAnimator(true);

        // 判断是否要编辑
        if (globalSpec.getImageEditEnabled()) {
            getPhotoVideoLayout().getViewHolder().rlEdit.setVisibility(View.VISIBLE);
            getPhotoVideoLayout().getViewHolder().rlEdit.setTag(uri);
        } else {
            getPhotoVideoLayout().getViewHolder().rlEdit.setVisibility(View.INVISIBLE);
        }

        // 隐藏拍照按钮
        getPhotoVideoLayout().getViewHolder().btnClickOrLong.setVisibility(View.INVISIBLE);

        // 设置当前模式是图片模式
        getCameraStateManager().setState(getCameraStateManager().getPictureComplete());
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
        getPhotoVideoLayout().startOperationBtnAnimatorMulti();

        // 重置按钮，因为每次点击，都会自动关闭
        getPhotoVideoLayout().getViewHolder().btnClickOrLong.resetState();
        // 显示右上角
        setMenuVisibility(View.VISIBLE);

        // 禁用长按事件，即禁止录像
        getPhotoVideoLayout().setButtonFeatures(BUTTON_STATE_ONLY_CLICK);

        // 设置当前模式是图片休闲并存模式
        getCameraStateManager().setState(getCameraStateManager().getPictureMultiple());
    }

    /**
     * 获取当前view的状态
     *
     * @return 状态
     */
    public IState getState() {
        return getCameraStateManager().getState();
    }

    /**
     * 取消单图后的重置
     */
    public void cancelOnResetBySinglePicture() {
        getCameraPictureManager().clearBitmapDataList();

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

        // 恢复底部
        showBottomMenu();

        // 隐藏大图
        getSinglePhotoView().setVisibility(View.GONE);

        // 隐藏编辑按钮
        getPhotoVideoLayout().getViewHolder().rlEdit.setVisibility(View.GONE);

        // 恢复底部按钮
        getPhotoVideoLayout().reset();
        // 恢复底部按钮操作模式
        initPvLayoutButtonFeatures();
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
        getPhotoVideoLayout().setClickOrLongEnable(true);
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
        getPhotoVideoLayout().setClickOrLongEnable(false);
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
            switch (flashMode) {
                case FLASH_MODE_AUTO:
                    getFlashView().setImageResource(cameraSpec.getImageFlashAuto());
                    break;
                case FLASH_MODE_ON:
                    getFlashView().setImageResource(cameraSpec.getImageFlashOn());
                    break;
                case FLASH_MODE_OFF:
                    getFlashView().setImageResource(cameraSpec.getImageFlashOff());
                    break;
                default:
                    break;
            }
            cameraManage.setFlashMode(flashMode);
        }
    }

    /**
     * 记忆模式下获取闪光灯缓存的模式
     */
    private void flashGetCache() {
        // 判断闪光灯是否记忆模式，如果是记忆模式则使用上个闪光灯模式
        if (cameraSpec.getEnableFlashMemoryModel()) {
            flashMode = FlashCacheUtils.getFlashModel(getContext());
        }
    }

    /**
     * 记忆模式下缓存闪光灯模式
     */
    private void flashSaveCache() {
        // 判断闪光灯是否记忆模式，如果是记忆模式则存储当前闪光灯模式
        if (cameraSpec.getEnableFlashMemoryModel()) {
            FlashCacheUtils.saveFlashModel(getContext(), flashMode);
        }
    }

    /**
     * 请求权限
     * return false则是请求权限
     * return true则是无权限请求继续下一步
     */
    private boolean requestPermissions() {
        // 判断权限，权限通过才可以初始化相关
        if (globalSpec.isAddAlbumByCamera()) {
            ArrayList<String> needPermissions = getNeedPermissions();
            if (!needPermissions.isEmpty()) {
                // 请求权限
                requestPermissionsDialog();
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * 获取目前需要请求的权限
     */
    protected ArrayList<String> getNeedPermissions() {
        // 需要请求的权限列表
        ArrayList<String> permissions = new ArrayList<>();
        // Android 10 以下需要请求存储权限才能存进相册
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager
                    .PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager
                            .PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        return permissions;
    }

    /**
     * 请求权限 - 如果曾经拒绝过，则弹出dialog
     */
    private void requestPermissionsDialog() {
        // 动态消息
        StringBuilder message = new StringBuilder();
        message.append(getString(R.string.z_multi_library_to_use_this_feature));
        message.append(getString(R.string.z_multi_library_file_read_and_write_permission_to_read_and_store_related_files));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle);
        // 弹窗提示为什么要请求这个权限
        builder.setTitle(getString(R.string.z_multi_library_hint));
        message.append(getString(R.string.z_multi_library_Otherwise_it_cannot_run_normally_and_will_apply_for_relevant_permissions_from_you));
        builder.setMessage(message.toString());
        builder.setPositiveButton(getString(R.string.z_multi_library_ok), (dialog, which) -> {
            dialog.dismiss();
            // 请求权限
            mRequestPermissionActivityResult.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
        });
        builder.setNegativeButton(getString(R.string.z_multi_library_cancel), (dialog, which) -> dialog.dismiss());
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * 暂停录制
     */
    protected void pauseRecord() {
        getCameraStateManager().pauseRecord();
    }

    public int getFlashMode() {
        return flashMode;
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
