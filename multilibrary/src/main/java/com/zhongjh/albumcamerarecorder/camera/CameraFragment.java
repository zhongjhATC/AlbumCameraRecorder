package com.zhongjh.albumcamerarecorder.camera;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.zhongjh.albumcamerarecorder.BaseFragment;
import com.zhongjh.albumcamerarecorder.MainActivity;
import com.zhongjh.albumcamerarecorder.R;
import com.zhongjh.albumcamerarecorder.album.model.SelectedItemCollection;
import com.zhongjh.albumcamerarecorder.camera.common.Constants;
import com.zhongjh.albumcamerarecorder.camera.entity.BitmapData;
import com.zhongjh.albumcamerarecorder.camera.listener.CaptureListener;
import com.zhongjh.albumcamerarecorder.camera.listener.ClickOrLongListener;
import com.zhongjh.albumcamerarecorder.camera.listener.ErrorListener;
import com.zhongjh.albumcamerarecorder.camera.listener.OperateCameraListener;
import com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity;
import com.zhongjh.albumcamerarecorder.utils.ViewBusinessUtils;
import com.zhongjh.imageedit.ImageEditActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import com.zhongjh.common.entity.MultiMedia;
import com.zhongjh.common.enums.MultimediaTypes;

import static android.app.Activity.RESULT_OK;
import static com.zhongjh.albumcamerarecorder.preview.BasePreviewActivity.REQ_IMAGE_EDIT;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_MULTIMEDIA_CHOICE;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_MULTIMEDIA_TYPES;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_SELECTION;
import static com.zhongjh.albumcamerarecorder.constants.Constant.EXTRA_RESULT_SELECTION_PATH;
import static com.zhongjh.albumcamerarecorder.constants.Constant.REQUEST_CODE_PREVIEW_CAMRRA;
import static com.zhongjh.albumcamerarecorder.constants.Constant.REQUEST_CODE_PREVIEW_VIDEO;

/**
 * 拍摄视频
 *
 * @author zhongjh
 * @date 2018/8/22
 */
public class CameraFragment extends BaseFragment {

    private MainActivity mActivity;

    private CameraLayout mCameraLayout;
    private final static int MILLISECOND = 2000;

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

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(@NotNull Activity activity) {
        super.onAttach(activity);
        this.mActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean isReturn = mCameraLayout.onActivityResult(resultCode);
        if (isReturn) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_PREVIEW_CAMRRA:
                // 如果在预览界面点击了确定
                if (data.getBooleanExtra(BasePreviewActivity.EXTRA_RESULT_APPLY, false)) {
                    // 请求的预览界面
                    Bundle resultBundle = data.getBundleExtra(BasePreviewActivity.EXTRA_RESULT_BUNDLE);
                    // 获取选择的数据
                    ArrayList<MultiMedia> selected = resultBundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
                    if (selected == null) {
                        return;
                    }
                    // 重新赋值
                    ArrayList<BitmapData> bitmapDatas = new ArrayList<>();
                    for (MultiMedia item : selected) {
                        BitmapData bitmapData = new BitmapData(item.getPath(), item.getUri());
                        bitmapDatas.add(bitmapData);
                    }
                    // 全部刷新
                    mCameraLayout.refreshMultiPhoto(bitmapDatas);
                }
                break;
            case REQUEST_CODE_PREVIEW_VIDEO:
                // 视频界面
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(data.getStringExtra("path"));
                ArrayList<Uri> arrayListUri = new ArrayList<>();
                arrayListUri.add(data.getParcelableExtra("uri"));
                // 获取视频路径
                Intent result = new Intent();
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, arrayList);
                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, arrayListUri);
                result.putExtra(EXTRA_MULTIMEDIA_TYPES, MultimediaTypes.VIDEO);
                result.putExtra(EXTRA_MULTIMEDIA_CHOICE, false);
                mActivity.setResult(RESULT_OK, result);
                mIsCommit = true;
                mActivity.finish();
                break;
            case REQ_IMAGE_EDIT:
                // 编辑图片界面
                mCameraLayout.refreshEditPhoto();
                break;
            default:
                break;
        }
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
                ViewBusinessUtils.setTabLayoutScroll(true, mActivity, mCameraLayout.mViewHolder.pvLayout);
            }

            @Override
            public void captureSuccess(ArrayList<String> paths, ArrayList<Uri> uris) {
                Intent result = new Intent();
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, paths);
                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, uris);
                result.putExtra(EXTRA_MULTIMEDIA_TYPES, MultimediaTypes.PICTURE);
                result.putExtra(EXTRA_MULTIMEDIA_CHOICE, false);
                mActivity.setResult(RESULT_OK, result);
                mIsCommit = true;
                mActivity.finish();

            }

            @Override
            public void recordSuccess(String path, Uri uri) {
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(path);
                ArrayList<Uri> arrayListUri = new ArrayList<>();
                arrayListUri.add(uri);
                // 获取视频路径
                Intent result = new Intent();
                result.putStringArrayListExtra(EXTRA_RESULT_SELECTION_PATH, arrayList);
                result.putParcelableArrayListExtra(EXTRA_RESULT_SELECTION, arrayListUri);
                result.putExtra(EXTRA_MULTIMEDIA_TYPES, MultimediaTypes.VIDEO);
                result.putExtra(EXTRA_MULTIMEDIA_CHOICE, false);
                mActivity.setResult(RESULT_OK, result);
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
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_URI, uri);
            intent.putExtra(ImageEditActivity.EXTRA_IMAGE_SAVE_PATH, newPath);
            this.startActivityForResult(intent, REQ_IMAGE_EDIT);
        });
    }

}
