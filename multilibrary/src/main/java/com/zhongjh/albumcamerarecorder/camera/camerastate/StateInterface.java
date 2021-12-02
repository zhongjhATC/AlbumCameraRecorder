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
     * 返回true的时候即是纸条跳过了后面的ActivityResult事件
     *
     * @param resultCode Activity的返回码
     * @return 返回true是跳过，返回false则是继续
     */
    boolean onActivityResult(int resultCode);

    /**
     * 提交核心事件
     */
    void pvLayoutCommit();

    /**
     * 取消核心事件
     */
    void pvLayoutCancel();

    /**
     * 录像时间过短，目前是单视频和多视频才会使用这个功能
     * @param time 多短的时间
     */
    void longClickShort(final long time);

    /**
     * 停止录像并且完成它，如果是因为视频过短则清除冗余数据
     *
     * @param isShort 是否因为视频过短而停止
     */
    void stopRecord(boolean isShort);

}
