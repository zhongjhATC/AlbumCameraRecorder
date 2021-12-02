package com.zhongjh.albumcamerarecorder.camera.camerastate.state;

import android.util.Log;

import com.zhongjh.albumcamerarecorder.camera.CameraLayout;
import com.zhongjh.albumcamerarecorder.camera.camerastate.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.camerastate.StateMode;
import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;
import com.zhongjh.albumcamerarecorder.utils.ViewBusinessUtils;

import static android.app.Activity.RESULT_OK;

/**
 * 单视频完成状态的相关处理
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public class VideoComplete extends StateMode {

    /**
     * @param cameraLayout          主要是多个状态围绕着cameraLayout进行相关处理
     * @param cameraStateManagement 可以让状态更改别的状态
     */
    public VideoComplete(CameraLayout cameraLayout, CameraStateManagement cameraStateManagement) {
        super(cameraLayout, cameraStateManagement);
    }

    @Override
    public void resetState() {
        // 取消视频删除文件
        FileUtil.deleteFile(getCameraLayout().mVideoFile);
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
        getCameraLayout().resetStateAll();
    }

    @Override
    public Boolean onBackPressed() {
        return null;
    }

    @Override
    public boolean onActivityResult(int resultCode) {
        if (resultCode != RESULT_OK) {
            getCameraLayout().showBottomMenu();
            // 如果是从视频界面回来，就重置状态
            getCameraStateManagement().setState(getCameraStateManagement().getPreview());
            return true;
        }
        return false;
    }

    @Override
    public void pvLayoutCommit() {
        // 恢复预览状态
        getCameraStateManagement().setState(getCameraStateManagement().getPreview());
    }

    @Override
    public void pvLayoutCancel() {

    }

    @Override
    public void longClickShort(long time) {

    }

    @Override
    public void stopRecord(boolean isShort) {

    }
}
