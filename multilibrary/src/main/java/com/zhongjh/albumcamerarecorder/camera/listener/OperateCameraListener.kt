package com.zhongjh.albumcamerarecorder.camera.listener

import com.zhongjh.common.entity.LocalFile
import java.util.*

/**
 * 拍照录像
 * 操作按钮的Listener
 *
 * @author zhongjh
 */
interface OperateCameraListener {
    /**
     * 取消事件
     */
    fun cancel()

    /**
     * 拍照成功后点击确认事件
     *
     * @param localFiles 包含文件地址、uri和丰富其他属性
     */
    fun captureSuccess(localFiles: ArrayList<LocalFile>)
}