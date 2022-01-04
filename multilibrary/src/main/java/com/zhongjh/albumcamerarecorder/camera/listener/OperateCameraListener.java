package com.zhongjh.albumcamerarecorder.camera.listener;

import android.net.Uri;

import com.zhongjh.common.entity.LocalFile;

import java.util.ArrayList;

/**
 * 拍照录像
 * 操作按钮的Listener
 *
 * @author zhongjh
 */
public interface OperateCameraListener {

    /**
     * 取消事件
     */
    void cancel();

    /**
     * 拍照成功后点击确认事件
     *
     * @param localFiles 包含文件地址、uri和丰富其他属性
     */
    void captureSuccess(ArrayList<LocalFile> localFiles);

    /**
     * 录像成功后点击确认事件
     *
     * @param path 文件地址
     * @param uri  uri
     * @deprecated 作废，用第三库CameraView来完成
     */
    @Deprecated
    void recordSuccess(String path, Uri uri);

}
