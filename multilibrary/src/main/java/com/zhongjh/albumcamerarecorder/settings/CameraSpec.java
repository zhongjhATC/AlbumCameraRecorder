package com.zhongjh.albumcamerarecorder.settings;

import com.zhongjh.albumcamerarecorder.R;
import gaode.zhongjh.com.common.enums.MimeType;
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
    }


    // region 属性

    public Set<MimeType> mimeTypeSet; // 选择 视频图片 的类型，MimeType.allOf()
    public int imageSwitch = R.drawable.ic_camera;    // 切换前置/后置摄像头图标资源
    public int imageFlashOn = R.drawable.ic_flash_on;      // 闪光灯开启状态图标
    public int imageFlashOff = R.drawable.ic_flash_off;       // 闪光灯关闭状态图标
    public int imageFlashAuto = R.drawable.ic_flash_auto;      // 闪光灯自动状态图标
    public int duration = 10;    // 最长录制时间
    public int minDuration = 1500;// 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制


    /**
     * 仅支持图片
     */
    public boolean onlySupportImages() {
        return MimeType.ofImage().containsAll(GlobalSpec.getInstance().getMimeTypeSet(ModuleTypes.CAMERA));
    }

    /**
     * 仅支持视频
     */
    public boolean onlySupportVideos() {
        return MimeType.ofVideo().containsAll(GlobalSpec.getInstance().getMimeTypeSet(ModuleTypes.CAMERA));
    }

    // endregion 属性


}
