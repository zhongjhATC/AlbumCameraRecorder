package com.zhongjh.albumcamerarecorder.camera.ui.camera.presenter;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.otaliastudios.cameraview.VideoResult;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.BaseCameraFragment;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.impl.ICameraVideo;
import com.zhongjh.albumcamerarecorder.camera.ui.camera.state.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.ui.previewvideo.PreviewVideoActivity;
import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.listener.VideoEditListener;
import com.zhongjh.common.utils.MediaStoreCompat;
import com.zhongjh.common.utils.ThreadUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

/**
 * 这是专门处理视频的有关逻辑
 *
 * @author zhongjh
 * @date 2022/8/23
 */
public class BaseCameraVideoPresenter implements ICameraVideo {

    private final static int PROGRESS_MAX = 100;

    public BaseCameraVideoPresenter(
            BaseCameraFragment<? extends CameraStateManagement,
                    ? extends BaseCameraPicturePresenter,
                    ? extends BaseCameraVideoPresenter> baseCameraFragment) {
        this.baseCameraFragment = baseCameraFragment;
    }

    protected BaseCameraFragment<? extends CameraStateManagement,
            ? extends BaseCameraPicturePresenter,
            ? extends BaseCameraVideoPresenter> baseCameraFragment;
    /**
     * 从视频预览界面回来
     */
    ActivityResultLauncher<Intent> previewVideoActivityResult;
    /**
     * 录像文件配置路径
     */
    private MediaStoreCompat videoMediaStoreCompat;

    /**
     * 合并视频线程
     */
    private final ArrayList<ThreadUtils.SimpleTask<Boolean>> mMergeVideoTasks = new ArrayList<>();
    /**
     * 处于分段录制模式下的视频的文件列表
     */
    private final ArrayList<String> videoPaths = new ArrayList<>();
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
     * 是否分段录制
     */
    private boolean isSectionRecord;
    /**
     * 是否中断录像
     */
    private boolean isBreakOff;

    /**
     * 初始化有关视频的配置数据
     */
    @Override
    public void initData() {
        // 设置视频路径
        if (baseCameraFragment.getGlobalSpec().getVideoStrategy() != null) {
            // 如果设置了视频的文件夹路径，就使用它的
            videoMediaStoreCompat = new MediaStoreCompat(baseCameraFragment.getMyContext(), baseCameraFragment.getGlobalSpec().getVideoStrategy());
        } else {
            // 否则使用全局的
            if (baseCameraFragment.getGlobalSpec().getSaveStrategy() == null) {
                throw new RuntimeException("Don't forget to set SaveStrategy.");
            } else {
                videoMediaStoreCompat = new MediaStoreCompat(baseCameraFragment.getMyContext(), baseCameraFragment.getGlobalSpec().getSaveStrategy());
            }
        }
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
                FileUtil.deleteFile(videoFile);
            }
            // 删除多个视频
            for (String item : videoPaths) {
                FileUtil.deleteFile(item);
            }
            // 新合成视频删除
            if (newSectionVideoPath != null) {
                FileUtil.deleteFile(newSectionVideoPath);
            }
        } else {
            // 如果是提交的，删除合成前的视频
            for (String item : videoPaths) {
                FileUtil.deleteFile(item);
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
        // 用于播放的视频file
        if (videoFile == null) {
            videoFile = videoMediaStoreCompat.createFile(1, true, "mp4");
        }
        if (baseCameraFragment.getCameraSpec().getEnableVideoHighDefinition()) {
            baseCameraFragment.getCameraView().takeVideo(videoFile);
        } else {
            baseCameraFragment.getCameraView().takeVideoSnapshot(videoFile);
        }
    }

    /**
     * 视频录制结束后
     */
    @Override
    public void onVideoTaken(VideoResult result) {
        // 判断是否短时间结束
        if (!isShort && !isBreakOff()) {
            if (!isSectionRecord) {
                //  如果录制结束，打开该视频。打开底部菜单
                PreviewVideoActivity.startActivity(baseCameraFragment, previewVideoActivityResult, result.getFile().getPath());
            } else {
                videoTimes.add(sectionRecordTime);
                // 如果已经有录像缓存，那么就不执行这个动作了
                if (videoPaths.size() <= 0) {
                    baseCameraFragment.getPhotoVideoLayout().startShowLeftRightButtonsAnimator();
                    baseCameraFragment.getPhotoVideoLayout().getViewHolder().tvSectionRecord.setVisibility(View.GONE);
                }
                // 加入视频列表
                videoPaths.add(result.getFile().getPath());
                // 显示当前进度
                baseCameraFragment.getPhotoVideoLayout().setData(videoTimes);
                // 创建新的file
                videoFile = videoMediaStoreCompat.createFile(1, true, "mp4");
                // 如果是在已经合成的情况下继续拍摄，那就重置状态
                if (!baseCameraFragment.getPhotoVideoLayout().getProgressMode()) {
                    baseCameraFragment.getPhotoVideoLayout().resetConfirm();
                }
            }
        } else {
            FileUtil.deleteFile(videoFile);
        }
        isShort = false;
        setBreakOff(false);
        baseCameraFragment.getPhotoVideoLayout().setEnabled(true);
    }

    /**
     * 删除视频 - 多个模式
     */
    @Override
    public void removeVideoMultiple() {
        // 每次删除，后面都要重新合成,新合成的也删除
        baseCameraFragment.getPhotoVideoLayout().resetConfirm();
        if (newSectionVideoPath != null) {
            FileUtil.deleteFile(newSectionVideoPath);
        }
        // 删除最后一个视频和视频文件
        FileUtil.deleteFile(videoPaths.get(videoPaths.size() - 1));
        videoPaths.remove(videoPaths.size() - 1);
        videoTimes.remove(videoTimes.size() - 1);

        // 显示当前进度
        baseCameraFragment.getPhotoVideoLayout().setData(videoTimes);
        baseCameraFragment.getPhotoVideoLayout().invalidateClickOrLongButton();
        if (videoPaths.size() == 0) {
            baseCameraFragment.getCameraStateManagement().resetState();
        }
    }

    /**
     * 打开预览视频界面
     */
    @Override
    public void openPreviewVideoActivity() {
        if (isSectionRecord && baseCameraFragment.getCameraSpec().getVideoMergeCoordinator() != null) {
            // 创建合并视频后的路径
            newSectionVideoPath = videoMediaStoreCompat.createFile(1, true, "mp4").getPath();

            // 显示loading
            baseCameraFragment.getPhotoVideoLayout().getViewHolder().pbConfirm.setVisibility(View.VISIBLE);
            // 开始进行合并线程
            baseCameraFragment.getPhotoVideoLayout().getViewHolder().btnConfirm.setProgress(50);
            ThreadUtils.SimpleTask<Boolean> simpleTask = new ThreadUtils.SimpleTask<Boolean>() {

                @Override
                public Boolean doInBackground() {
                    Objects.requireNonNull(baseCameraFragment.getCameraSpec().getVideoMergeCoordinator()).merge(videoPaths, newSectionVideoPath);
                    return true;
                }

                @Override
                public void onSuccess(Boolean result) {
                    baseCameraFragment.getPhotoVideoLayout().getViewHolder().pbConfirm.setVisibility(View.GONE);
                    baseCameraFragment.getPhotoVideoLayout().getViewHolder().btnConfirm.setProgress(100);
                    PreviewVideoActivity.startActivity(baseCameraFragment, previewVideoActivityResult, newSectionVideoPath);
                }

                @Override
                public void onFail(Throwable t) {
                    baseCameraFragment.getPhotoVideoLayout().getViewHolder().pbConfirm.setVisibility(View.GONE);
                    baseCameraFragment.getPhotoVideoLayout().getViewHolder().btnConfirm.reset();
                    super.onFail(t);
                }
            };
            mMergeVideoTasks.add(simpleTask);
            ThreadUtils.executeByIo(simpleTask);
        }
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

    public MediaStoreCompat getVideoMediaStoreCompat() {
        return videoMediaStoreCompat;
    }

    public ArrayList<String> getVideoPaths() {
        return videoPaths;
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
