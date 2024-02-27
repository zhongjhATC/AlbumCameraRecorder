package com.zhongjh.albumcamerarecorder.camera.listener

interface OnCameraManageListener {
    /**
     * 拍照成功返回
     *
     * @param path 文件地址
     */
    fun onPictureSuccess(path: String)

    /**
     * 录像成功返回
     *
     * @param path 文件地址
     */
    fun onRecordSuccess(path: String)

    /**
     * 使用相机出错拍照或者录像出错
     *
     * @param errorCode 错误码
     * @param message   错误文本
     * @param cause     错误类
     */
    fun onError(errorCode: Int, message: String?, cause: Throwable?)
}