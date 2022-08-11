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

import com.zhongjh.albumcamerarecorder.BaseFragment;
import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.camera.listener.ErrorListener;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.settings.CameraSpec;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.entity.MultiMedia;

import java.util.ArrayList;

/**
 * 一个父类的拍摄Fragment，用于开放出来给开发自定义，但是同时也需要遵守一些规范
 *
 * @author zhongjh
 * @date 2022/8/11
 */
public class BaseCameraFragment extends BaseFragment {

    private final static int MILLISECOND = 2000;

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
     * 声明一个long类型变量：用于存放上一点击“返回键”的时刻
     */
    private long mExitTime;
    /**
     * 是否提交,如果不是提交则要删除冗余文件
     */
    private boolean mIsCommit = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mGlobalSpec = GlobalSpec.INSTANCE;
        mCameraSpec = CameraSpec.INSTANCE;
        onActivityResult();
        View view = inflater.inflate(R.layout.fragment_camera_zjh, container, false);
        view.setOnKeyListener((v, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK);

        mCameraLayout = view.findViewById(R.id.cameraLayout);
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
     * 针对回调
     */
    private void onActivityResult() {
        // 在图廊预览界面点击了确定
        mAlbumPreviewActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            boolean isReturn = mCameraLayout.onActivityResult(result.getResultCode());
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
                    mCameraLayout.refreshMultiPhoto(bitmapDatas);
                }
            }
        });

        // 从视频预览界面回来
        mPreviewVideoActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            boolean isReturn = mCameraLayout.onActivityResult(result.getResultCode());
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
            boolean isReturn = mCameraLayout.onActivityResult(result.getResultCode());
            if (isReturn) {
                return;
            }
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() == null) {
                    return;
                }
                // 编辑图片界面
                mCameraLayout.refreshEditPhoto(result.getData().getIntExtra(EXTRA_WIDTH, 0),
                        result.getData().getIntExtra(EXTRA_HEIGHT, 0));
            }
        });
    }

}
