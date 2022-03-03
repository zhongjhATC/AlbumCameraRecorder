package com.zhongjh.albumcamerarecorder.camera.listener

import android.net.Uri

/**
 * 编辑事件
 * @author zhongjh
 */
interface EditListener {
    /**
     * 编辑图片
     *
     * @param uri 当前需要编辑图片
     * @param newPath 编辑后的图片path
     */
    fun onImageEdit(uri: Uri, newPath: String)
}