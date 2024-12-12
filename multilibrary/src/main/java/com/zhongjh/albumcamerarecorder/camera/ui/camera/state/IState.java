package com.zhongjh.albumcamerarecorder.camera.ui.camera.state;

/**
 * 状态事件接口
 * 对于不同状态下，他们各自的实现不一样
 *
 * @author zhongjh
 * @date 2021/11/25
 */
public interface IState {

    /**
     * 方便调试
     */
    String getName();

    /**
     * Activity触发了Pause
     */
    void onActivityPause();

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
     * 右边按钮：提交核心事件
     */
    void pvLayoutCommit();

    /**
     * 右边按钮：中止提交事件
     */
    void stopProgress();

    /**
     * 左边按钮：取消核心事件
     */
    void pvLayoutCancel();

    /**
     * 录像时间过短，目前是单视频和多视频才会使用这个功能
     * @param time 多短的时间
     */
    void longClickShort(final long time);

    /**
     * 暂停录制
     *
     * @param isShort 是否因为视频过短而停止
     */
    void pauseRecord(boolean isShort);

}
