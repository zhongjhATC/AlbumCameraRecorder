package com.zhongjh.albumcamerarecorder.camera.ui.camera.manager;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.impl.ICameraVideo;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManager;
import com.zhongjh.albumcamerarecorder.camera.ui.previewvideo.PreviewVideoActivity;
import com.zhongjh.common.enums.MediaType;
import com.zhongjh.albumcamerarecorder.utils.FileMediaUtil;
import com.zhongjh.common.utils.ThreadUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import com.zhongjh.common.utils.FileUtils;

/**
 * 这是专门处理视频的有关逻辑
 *
 * @author zhongjh
 * @date 2022/8/23
 */
public class CameraVideoManager implements ICameraVideo {

    private final static int PROGRESS_MAX = 100;
    private final static String TAG = "BaseCameraVideoPresenter";

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
     * 合并视频线程
     */
    private final ArrayList<ThreadUtils.SimpleTask<Boolean>> mMergeVideoTasks = new ArrayList<>();
    /**
     * 处于分段录制模式下的视频的时间列表
     */
    private final ArrayList<Long> videoTimes = new ArrayList<>();
    /**
     * 视频File,用于后面能随时删除
     */
    private File videoFile;
    /**
     * 上一个分段录制的时间
     */
    private long sectionRecordTime;
    /**
     * 处于分段录制模式下合成的新的视频
     */
    private String newSectionVideoPath;
    /**
     * 是否短时间录像
     */
    private boolean isShort;
    /**
     * 是否分段录制,默认分段录制,改版后原生Api支持暂停\恢复录制
     */
    private boolean isSectionRecord = true;
    /**
     * 是否中断录像
     */
    private boolean isBreakOff;

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
     *
     * @param isCommit 是否提交了数据,如果不是提交则要删除冗余文件
     */
    @Override
    public void onDestroy(boolean isCommit) {
        if (!isCommit) {
            if (videoFile != null) {
                // 删除视频
                FileUtils.deleteFile(videoFile);
            }
            // 新合成视频删除
            if (newSectionVideoPath != null) {
                FileUtils.deleteFile(newSectionVideoPath);
            }
        }
        if (baseCameraFragment.getCameraSpec() != null && baseCameraFragment.getCameraSpec().isMergeEnable()) {
            if (baseCameraFragment.getCameraSpec().getVideoMergeCoordinator() != null) {
                baseCameraFragment.getCameraSpec().setVideoMergeCoordinator(null);
            }
        }
        stopVideoMultiple();
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
        // 重置状态
        isShort = false;
        setBreakOff(false);
        baseCameraFragment.getPhotoVideoLayout().setEnabled(true);
    }

    /**
     * 视频录制成功
     */
    @SuppressLint("LongLogTag")
    @Override
    public void onRecordSuccess(String path) {
        videoFile = new File(path);
        // 判断文件是否超过1秒才属于合格的视频
        long mediaDuration = getMediaDuration(path);
        if (mediaDuration < 1000) {
            baseCameraFragment.setShortTip();
            Log.d(TAG, "视频时间低于1秒");
        }
        // 判断是否短时间结束
        if (!isShort && !isBreakOff() && mediaDuration >= 1000) {
            //  如果录制结束，打开该视频。
            PreviewVideoActivity.startActivity(baseCameraFragment, previewVideoActivityResult, path);
        } else {
            FileUtils.deleteFile(videoFile);
        }
        // 重置状态
        isShort = false;
        setBreakOff(false);
        baseCameraFragment.getPhotoVideoLayout().setEnabled(true);
    }

    /**
     * 打开预览视频界面
     */
    @Override
    public void openPreviewVideoActivity() {
        baseCameraFragment.getPhotoVideoLayout().getViewHolder().pbConfirm.setVisibility(View.GONE);
        baseCameraFragment.getPhotoVideoLayout().getViewHolder().btnConfirm.setProgress(100);
        PreviewVideoActivity.startActivity(baseCameraFragment, previewVideoActivityResult, newSectionVideoPath);
    }

    /**
     * 停止所有合并视频线程
     */
    public void stopVideoMultiple() {
        baseCameraFragment.getPhotoVideoLayout().getViewHolder().pbConfirm.setVisibility(View.GONE);
        for (ThreadUtils.SimpleTask<Boolean> item : mMergeVideoTasks) {
            item.cancel();
        }
    }

    /**
     * 获取视频的时间
     *
     * @param filePath 视频文件
     * @return 视频的时间
     */
    private long getMediaDuration(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            String metaData = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long duration = 0L;
            if (metaData != null) {
                duration = Long.parseLong(metaData);
            }
            return duration;
        } catch (Exception exception) {
            return 0;
        } finally {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    retriever.close();
                } else {
                    retriever.release();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public ArrayList<Long> getVideoTimes() {
        return videoTimes;
    }

    public File getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(File videoFile) {
        this.videoFile = videoFile;
    }

    public long getSectionRecordTime() {
        return sectionRecordTime;
    }

    public void setSectionRecordTime(long sectionRecordTime) {
        this.sectionRecordTime = sectionRecordTime;
    }

    public String getNewSectionVideoPath() {
        return newSectionVideoPath;
    }

    public void setNewSectionVideoPath(String newSectionVideoPath) {
        this.newSectionVideoPath = newSectionVideoPath;
    }

    public boolean isShort() {
        return isShort;
    }

    public void setShort(boolean aShort) {
        isShort = aShort;
    }

    public boolean isSectionRecord() {
        return isSectionRecord;
    }

    public void setSectionRecord(boolean sectionRecord) {
        isSectionRecord = sectionRecord;
    }

    public boolean isBreakOff() {
        return isBreakOff;
    }

    public void setBreakOff(boolean breakOff) {
        isBreakOff = breakOff;
    }

}
