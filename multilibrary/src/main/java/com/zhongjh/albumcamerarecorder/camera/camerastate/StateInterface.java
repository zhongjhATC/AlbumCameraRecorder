package com.zhongjh.albumcamerarecorder.camera.camerastate;

import com.zhongjh.albumcamerarecorder.camera.CameraLayout;

/**
 * 事件接口
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public interface StateInterface {

    void resetState();

    /**
     * 设置CameraFragment的返回逻辑
     * @return 可为null，如果是null则跳过返回逻辑，如果是有值，则执行下去
     */
    Boolean onBackPressed();

    /**
     * 提交核心事件
     */
    void pvLayoutCommit();

}
