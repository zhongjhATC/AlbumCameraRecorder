package com.zhongjh.albumcamerarecorder.camera.ui.camera.manager;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.impl.ICameraVideo;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.albumcamerarecorder.camera.ui.previewvideo.PreviewVideoActivity;

import java.util.ArrayList;

/**
 * 这是专门处理视频的有关逻辑
 *
 * @author zhongjh
 * @date 2022/8/23
 */
public class CameraVideoManager implements ICameraVideo {

    private final static String TAG = "CameraVideoManager";

    public CameraVideoManager(
            BaseCameraFragment<? extends CameraStateManager,
                    ? extends CameraPictureManager,
                    ? extends CameraVideoManager> baseCameraFragment) {
        this.baseCameraFragment = baseCameraFragment;
    }

    protected BaseCameraFragment<? extends CameraStateManager,
            ? extends CameraPictureManager,
            ? extends CameraVideoManager> baseCameraFragment;
    /**
     * 从视频预览界面回来
     */
    ActivityResultLauncher<Intent> previewVideoActivityResult;
    /**
     * 处于分段录制模式下的视频的时间列表
     */
    private final ArrayList<Long> videoTimes = new ArrayList<>();
    /**
     * 初始化有关视频的配置数据
     */
    @Override
    public void initData() {
    }

    /**
     * 初始化Activity的有关视频回调
     */
    public void initActivityResult() {
        // 从视频预览界面回来
        previewVideoActivityResult = baseCameraFragment.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            boolean isReturn = baseCameraFragment.initActivityResult(result.getResultCode());
            if (isReturn) {
                return;
            }
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() == null) {
                    return;
                }
                baseCameraFragment.commitVideoSuccess(result.getData());
            }
        });
    }

    /**
     * 生命周期onDestroy
     */
    @Override
    public void onDestroy() {
    }

    /**
     * 录制视频
     */
    @Override
    public void recordVideo() {
        baseCameraFragment.getCameraManage().takeVideo();
    }

    /**
     * 录像暂停
     *
     * @param recordedDurationNanos 当前视频持续时间：纳米单位
     */
    @Override
    public void onRecordPause(long recordedDurationNanos) {
        baseCameraFragment.setShortTipLongRecording();
        // 如果已经有录像正在录制中，那么就不执行这个动作了
        if (videoTimes.isEmpty()) {
            baseCameraFragment.getPhotoVideoLayout().startShowLeftRightButtonsAnimator(false);
        }
        videoTimes.clear();
        videoTimes.add(recordedDurationNanos / 1000000);
        // 显示当前进度
        baseCameraFragment.getPhotoVideoLayout().setData(videoTimes);
        // 如果是在已经合成的情况下继续拍摄，那就重置状态
        if (!baseCameraFragment.getPhotoVideoLayout().getProgressMode()) {
            baseCameraFragment.getPhotoVideoLayout().resetConfirm();
        }
        baseCameraFragment.getPhotoVideoLayout().setEnabled(true);
    }

    /**
     * 视频录制成功
     */
    @SuppressLint("LongLogTag")
    @Override
    public void onRecordSuccess(String path) {
        Log.d(TAG, "onRecordSuccess: " + path);
        PreviewVideoActivity.startActivity(baseCameraFragment, previewVideoActivityResult, path);
        baseCameraFragment.getPhotoVideoLayout().setEnabled(true);
    }

    public ArrayList<Long> getVideoTimes() {
        return videoTimes;
    }

}
