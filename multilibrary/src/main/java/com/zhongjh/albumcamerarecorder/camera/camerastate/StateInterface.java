package com.zhongjh.albumcamerarecorder.camera.camerastate;

import com.zhongjh.albumcamerarecorder.camera.CameraLayout;

/**
 * 事件接口
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public interface StateInterface {

    /**
     * 结束所有当前活动，重置状态
     * 一般指完成了当前活动，或者清除所有活动的时候调用
     */
    void resetState();

    /**
     * 设置CameraFragment的返回逻辑
     *
     * @return 可为null，如果是null则跳过返回逻辑，如果是有值，则执行下去
     */
    Boolean onBackPressed();

    /**
     * 提交核心事件
     */
    void pvLayoutCommit();

    /**
     * 取消核心事件
     */
    void pvLayoutCancel();

}
