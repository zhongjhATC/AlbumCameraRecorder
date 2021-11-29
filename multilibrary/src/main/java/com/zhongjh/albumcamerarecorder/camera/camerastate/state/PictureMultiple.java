package com.zhongjh.albumcamerarecorder.camera.camerastate.state;

import com.zhongjh.albumcamerarecorder.camera.CameraLayout;
import com.zhongjh.albumcamerarecorder.camera.camerastate.CameraStateManagement;
import com.zhongjh.albumcamerarecorder.camera.camerastate.StateMode;

/**
 * 多个图片状态，至少有一张图片情况
 *
 * @author zhongjh
 * @date 2021/11/29
 */
public class PictureMultiple extends StateMode {

    /**
     * @param cameraLayout          主要是多个状态围绕着cameraLayout进行相关处理
     * @param cameraStateManagement 可以让状态更改别的状态
     */
    public PictureMultiple(CameraLayout cameraLayout, CameraStateManagement cameraStateManagement) {
        super(cameraLayout, cameraStateManagement);
    }

    @Override
    public void resetState() {

    }

    @Override
    public Boolean onBackPressed() {
        return null;
    }

    @Override
    public void pvLayoutCommit() {

    }

}
