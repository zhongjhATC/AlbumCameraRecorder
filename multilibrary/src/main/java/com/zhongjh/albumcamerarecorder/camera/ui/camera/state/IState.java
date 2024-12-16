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
     * 返回true的时候即是跳过了后面的ActivityResult事件
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
     * 中间按钮：长按完毕
     */
    void onLongClickFinish();

    /**
     * 左边按钮：取消核心事件
     */
    void pvLayoutCancel();

    /**
     * 暂停录制
     */
    void pauseRecord();

}
