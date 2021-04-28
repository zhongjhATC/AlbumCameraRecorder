package com.zhongjh.albumcamerarecorder.camera.listener;

import android.net.Uri;

import java.util.ArrayList;

/**
 * 拍照录像
 * 操作按钮的Listener
 */
public interface OperateCameraListener {

    /**
     * 取消事件
     */
    void cancel();

    /**
     * 拍照成功后点击确认事件
     *
     * @param paths 文件地址
     * @param uris  文件uri
     */
    void captureSuccess(ArrayList<String> paths, ArrayList<Uri> uris);

    /**
     * 录像成功后点击确认事件
     *
     * @param path 文件地址
     * @param uri  uri
     */
    void recordSuccess(String path, Uri uri);

}
