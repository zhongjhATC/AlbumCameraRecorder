package com.zhongjh.multimedia.camera.listener

interface OnCameraManageListener {
    /**
     * 拍照成功返回
     *
     * @param path 图片地址
     */
    fun onPictureSuccess(path: String)

    /**
     * 录像成功返回
     *
     * @param path 文件地址
     */
    fun onRecordSuccess(path: String, uri: String)

    /**
     * 录像暂停
     *
     * @param recordedDurationNanos 当前视频持续时间：纳米单位
     */
    fun onRecordPause(recordedDurationNanos:Long)

    /**
     * 录像开始
     */
    fun onRecordStart()

    /**
     * Activity触发了Pause
     */
    fun onActivityPause()

    /**
     * 使用相机出错拍照或者录像出错
     *
     * @param errorCode 错误码
     * @param message   错误文本
     * @param cause     错误类
     */
    fun onError(errorCode: Int, message: String?, cause: Throwable?)

    /**
     * Camera绑定模式成功后触发
     */
    fun bindSucceed()
}