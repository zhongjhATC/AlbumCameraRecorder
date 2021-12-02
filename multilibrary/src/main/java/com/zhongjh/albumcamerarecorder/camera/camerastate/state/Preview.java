package com.zhongjh.albumcamerarecorder.camera.camerastate.state;

import android.util.Log;

import com.zhongjh.albumcamerarecorder.camera.CameraLayout;
import com.zhongjh.albumcamerarecorder.camera.camerastate.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.camerastate.StateMode;

/**
 * 预览状态的相关处理，默认状态
 *
 * @author zhongjh
 * @date 2021/11/26
 */
public class Preview extends StateMode {

    /**
     * @param cameraLayout          主要是多个状态围绕着cameraLayout进行相关处理
     * @param cameraStateManagement 可以让状态更改别的状态
     */
    public Preview(CameraLayout cameraLayout, CameraStateManagement cameraStateManagement) {
        super(cameraLayout, cameraStateManagement);
    }

    @Override
    public void resetState() {

    }

    @Override
    public Boolean onBackPressed() {
        // 如果是预览状态直接退出当前界面
        return false;
    }

    @Override
    public boolean onActivityResult(int resultCode) {
        return true;
    }

    @Override
    public void pvLayoutCommit() {

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
