package com.zhongjh.cameraviewsoundrecorder.settings;

import com.zhongjh.cameraviewsoundrecorder.album.enums.MimeType;

import java.util.Set;

public class CameraSpec {

    private CameraSpec() {
    }

    public static CameraSpec getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static CameraSpec getCleanInstance() {
        CameraSpec cameraSpec = getInstance();
        cameraSpec.reset();
        return cameraSpec;
    }

    /**
     * 重置
     */
    private void reset() {
        mimeTypeSet = null;
        supportSingleMediaType = false;
        videoPath = null;
        photoPath = null;
        captureStrategy = null;
        isMultiPicture = false;
        pictureMaxNumber = 6;
    }


    // region 属性
    public Set<MimeType> mimeTypeSet; // 选择 视频图片 的类型，MimeType.allOf()
    public boolean supportSingleMediaType; // 仅仅支持一个多媒体类型
    public String videoPath;            // 保存视频文件的路径
    public String photoPath;            // 保存图片文件的路径
    public CaptureStrategy captureStrategy; // 参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
    public boolean isMultiPicture = false;// 是否一次性可以拍摄多张图片
    public int pictureMaxNumber = 6; // 默认6张图片

    /**
     * 仅支持图片
     */
    public boolean onlySupportImages() {
        return supportSingleMediaType && MimeType.ofImage().containsAll(mimeTypeSet);
    }

    /**
     * 仅支持视频
     */
    public boolean onlySupportVideos() {
        return supportSingleMediaType && MimeType.ofVideo().containsAll(mimeTypeSet);
    }

    private static final class InstanceHolder {
        private static final CameraSpec INSTANCE = new CameraSpec();
    }


}
