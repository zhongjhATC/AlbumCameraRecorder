package com.zhongjh.albumcamerarecorder.camera;

import static android.app.Activity.RESULT_OK;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_SELECTION_LOCAL_FILE;
import static com.zhongjh.imageedit.ImageEditActivity.EXTRA_HEIGHT;
import static com.zhongjh.imageedit.ImageEditActivity.EXTRA_WIDTH;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.otaliastudios.cameraview.CameraView;
import com.zhongjh.albumcamerarecorder.BaseFragment;
import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.camera.adapter.PhotoAdapter;
import com.zhongjh.albumcamerarecorder.camera.adapter.PhotoAdapterListener;
import com.zhongjh.albumcamerarecorder.camera.camerastate.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.camera.listener.ErrorListener;
import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.settings.CameraSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.albumcamerarecorder.utils.SelectableUtils;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.entity.MultiMedia;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个父类的拍摄Fragment，用于开放出来给开发自定义，但是同时也需要遵守一些规范
 *
 * @author zhongjh
 * @date 2022/8/11
 */
public abstract class BaseCameraFragment extends BaseFragment implements PhotoAdapterListener {

    private final static int MILLISECOND = 2000;

    /**
     * 状态管理
     */
    public final CameraStateManagement mCameraStateManagement;

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

    private MainActivity mActivity;

    /**
     * 公共配置
     */
    private GlobalSpec mGlobalSpec;
    private CameraSpec mCameraSpec;

    /**
     * 拍照的图片集合适配器
     */
    private PhotoAdapter mPhotoAdapter;
    /**
     * 图片,单图或者多图都会加入该列表
     */
    List<BitmapData> mBitmapData = new ArrayList<>();

    /**
     * 声明一个long类型变量：用于存放上一点击“返回键”的时刻
     */
    private long mExitTime;
    /**
     * 是否提交,如果不是提交则要删除冗余文件
     */
    private boolean mIsCommit = false;

    public BaseCameraFragment() {
        mGlobalSpec = GlobalSpec.INSTANCE;
        mCameraSpec = CameraSpec.INSTANCE;
        mCameraStateManagement = new CameraStateManagement(this);
        onActivityResult();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(setContentView(), container, false);
        view.setOnKeyListener((v, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);
        initView(view, savedInstanceState);
        setView();
        init();
        initMultiplePhotoAdapter();
        mCameraLayout.init(mActivity, this);
        mCameraLayout.setErrorListener(new ErrorListener() {
            @Override
            public void onError() {
                //错误监听
                Log.i("CameraActivity", "camera error");
            }

            @Override
            public void onAudioPermissionError() {
            }
        });

        initCameraLayoutCloseListener();
        initCameraLayoutOperateCameraListener();
        initCameraLayoutCaptureListener();
        initCameraLayoutEditListener();

        return view;
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
        initMultiplePhotoAdapter();
    }

    /**
     * 设置CameraView
     *
     * @return 返回CameraView，主要用于拍摄、录制，里面包含水印
     */
    @NonNull
    public abstract CameraView getCameraView();

    /**
     * 当想使用已经定义好的多图显示控件，请设置它
     *
     * @return 返回多图的Recycler显示控件
     */
    public abstract RecyclerView getRecyclerViewPhoto();

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
        if (SelectableUtils.getImageMaxCount() > 1) {
            mViewHolder.rlPhoto.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
            mViewHolder.rlPhoto.setAdapter(mPhotoAdapter);
            mViewHolder.rlPhoto.setVisibility(View.VISIBLE);
        } else {
            mViewHolder.rlPhoto.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(Intent intent) {

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

        // 回调接口：删除图片后剩下的相关数据
        mOnCaptureListener.remove(mBitmapData, position);

        // 当列表全部删掉隐藏列表框的UI
        Log.d(TAG, "onDelete " + mBitmapData.size());
        if (mBitmapData.size() <= 0) {
            hideViewByMultipleZero();
        }
    }

    /**
     * 当多个图片删除到没有图片时候，隐藏相关View
     */
    public void hideViewByMultipleZero() {
        // 隐藏横版列表
        if (getRecyclerViewPhoto() != null) {
            getRecyclerViewPhoto().setVisibility(View.GONE);
        }

        // 隐藏横版列表的线条空间
        mViewHolder.vLine1.setVisibility(View.GONE);
        mViewHolder.vLine2.setVisibility(View.GONE);

        // 隐藏左右侧按钮
        mViewHolder.pvLayout.getViewHolder().btnCancel.setVisibility(View.GONE);
        mViewHolder.pvLayout.getViewHolder().btnConfirm.setVisibility(View.GONE);

        // 恢复长按事件，即重新启用录像
        mViewHolder.pvLayout.getViewHolder().btnClickOrLong.setVisibility(View.VISIBLE);
        initPvLayoutButtonFeatures();

        // 设置空闲状态
        mCameraStateManagement.setState(mCameraStateManagement.getPreview());

        // 如果是单图编辑情况下
        mViewHolder.rlEdit.setVisibility(View.GONE);

        // 恢复底部
        showBottomMenu();
    }

}
