package com.zhongjh.albumcamerarecorder.camera.listener;

import android.net.Uri;

/**
 * 编辑事件
 * @author zhongjh
 */
public interface EditListener {

    /**
     * 编辑图片
     *
     * @param uri 当前需要编辑图片
     * @param newPath 编辑后的图片path
     */
    void onImageEdit(Uri uri, String newPath);

}
