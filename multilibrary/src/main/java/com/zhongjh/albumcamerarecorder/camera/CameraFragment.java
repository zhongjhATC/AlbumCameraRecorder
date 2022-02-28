package com.zhongjh.albumcamerarecorder.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.zhongjh.albumcamerarecorder.BaseFragment;
import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.camera.listener.CaptureListener;
import com.zhongjh.albumcamerarecorder.camera.listener.ErrorListener;
import com.zhongjh.albumcamerarecorder.camera.listener.OperateCameraListener;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.imageedit.ImageEditActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_SELECTION_LOCAL_FILE;
import static com.zhongjh.imageedit.ImageEditActivity.EXTRA_HEIGHT;
import static com.zhongjh.imageedit.ImageEditActivity.EXTRA_WIDTH;

/**
 * 拍摄视频
 *
 * @author zhongjh
 * @date 2018/8/22
 */
public class CameraFragment extends BaseFragment {


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
    private CameraLayout mCameraLayout;

    /**
     * 公共配置
     */
    private GlobalSpec mGlobalSpec;
    /**
     * 声明一个long类型变量：用于存放上一点击“返回键”的时刻
     */
    private long mExitTime;
    /**
     * 是否提交,如果不是提交则要删除冗余文件
     */
    private boolean mIsCommit = false;

    public static CameraFragment newInstance() {
        CameraFragment cameraFragment = new CameraFragment();
        Bundle args = new Bundle();
        cameraFragment.setArguments(args);
        return cameraFragment;
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            this.mActivity = (MainActivity) context;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mGlobalSpec = GlobalSpec.INSTANCE;
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

    @Override
    public boolean onBackPressed() {
        Boolean isTrue = mCameraLayout.getCameraStateManagement().onBackPressed();
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
    public void onResume() {
        super.onResume();
        if (mCameraLayout != null) {
            mCameraLayout.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraLayout != null) {
            mCameraLayout.onPause();
        }
    }

    @Override
    public void onDestroy() {
        if (mCameraLayout != null) {
            mCameraLayout.onDestroy(mIsCommit);
        }
        super.onDestroy();
    }

    /**
     * 关闭事件
     */
    private void initCameraLayoutCloseListener() {
        mCameraLayout.setCloseListener(() -> mActivity.finish());
    }

    /**
     * 确认取消事件
     */
    private void initCameraLayoutOperateCameraListener() {
        mCameraLayout.setOperateCameraListener(new OperateCameraListener() {
            @Override
            public void cancel() {
                // 母窗体启动滑动
                mActivity.showHideTableLayout(true);
            }

            @Override
            public void captureSuccess(ArrayList<LocalFile> localFiles) {
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

        });
    }

    /**
     * 拍摄后操作图片的事件
     */
    private void initCameraLayoutCaptureListener() {
        mCameraLayout.setCaptureListener(new CaptureListener() {
            @Override
            public void remove(List<BitmapData> captureData) {
                // 判断如果删除光图片的时候，母窗体启动滑动
                if (captureData.size() <= 0) {
                    mActivity.showHideTableLayout(true);
                }
            }

            @Override
            public void add(List<BitmapData> captureDatas) {
                if (captureDatas.size() > 0) {
                    // 母窗体禁止滑动
                    mActivity.showHideTableLayout(false);
                }
            }
        });
    }

    /**
     * 编辑图片事件
     */
    private void initCameraLayoutEditListener() {
        mCameraLayout.setEditListener((uri, newPath) -> {
            Intent intent = new Intent();
            intent.setClass(getContext(), ImageEditActivity.class);
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SCREEN_ORIENTATION, mActivity.getRequestedOrientation());
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_URI, uri);
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SAVE_PATH, newPath);
            mImageEditActivityResult.launch(intent);
        });
    }

}
