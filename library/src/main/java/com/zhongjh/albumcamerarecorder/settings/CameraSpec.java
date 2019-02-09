package com.zhongjh.albumcamerarecorder.settings;

import com.zhongjh.albumcamerarecorder.album.enums.MimeType;
import com.zhongjh.albumcamerarecorder.utils.constants.ModuleTypes;

import java.util.Set;

public class CameraSpec {

    private CameraSpec() {
    }

    private static final class InstanceHolder {
        private static final CameraSpec INSTANCE = new CameraSpec();
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
        captureStrategy = null;
    }


    // region 属性

    public Set<MimeType> mimeTypeSet; // 选择 视频图片 的类型，MimeType.allOf()
    public boolean supportSingleMediaType; // 仅仅支持一个多媒体类型
    public CaptureStrategy captureStrategy; // 参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置

    /**
     * 仅支持图片
     */
    public boolean onlySupportImages() {
        return supportSingleMediaType && MimeType.ofImage().containsAll(GlobalSpec.getInstance().getMimeTypeSet(ModuleTypes.CAMERA));
    }

    /**
     * 仅支持视频
     */
    public boolean onlySupportVideos() {
        return supportSingleMediaType && MimeType.ofVideo().containsAll(GlobalSpec.getInstance().getMimeTypeSet(ModuleTypes.CAMERA));
    }

    // endregion 属性


}
