package com.zhongjh.multimedia.camera.ui.camera.state.type.impl

/**
 * 状态事件接口
 * 对于不同状态下，他们各自的实现不一样
 *
 * @author zhongjh
 * @date 2021/11/25
 */
interface IState {
    /**
     * 方便调试
     */
    fun getName(): String?

    /**
     * Activity触发了Pause
     * @noinspection unused
     */
    fun onActivityPause()

    /**
     * 设置CameraFragment的返回逻辑
     *
     * @return 可为null，如果是null则跳过返回逻辑，如果是有值，则执行下去
     */
    fun onBackPressed(): Boolean?

    /**
     * 右边按钮：提交核心事件
     */
    fun pvLayoutCommit()

    /**
     * 右边按钮：中止提交事件
     */
    fun stopProgress()

    /**
     * 中间按钮：长按完毕
     */
    fun onLongClickFinish()

    /**
     * 左边按钮：取消核心事件
     */
    fun pvLayoutCancel()

    /**
     * 暂停录制
     * @noinspection unused
     */
    fun pauseRecord()
}
