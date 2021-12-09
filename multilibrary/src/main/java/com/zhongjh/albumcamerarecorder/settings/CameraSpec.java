package com.zhongjh.albumcamerarecorder.settings;

import com.zhongjh.albumcamerarecorder.R;

import com.zhongjh.common.coordinator.VideoEditCoordinator;
import com.zhongjh.common.enums.MimeType;

import com.zhongjh.albumcamerarecorder.camera.listener.OnCameraViewListener;
import com.zhongjh.albumcamerarecorder.constants.ModuleTypes;

import java.util.Set;

/**
 * @author zhongjh
 */
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
        // 切换前置/后置摄像头图标资源
        imageSwitch = R.drawable.ic_camera_zjh;
        // 闪光灯开启状态图标
        imageFlashOn = R.drawable.ic_flash_on;
        // 闪光灯关闭状态图标
        imageFlashOff = R.drawable.ic_flash_off;
        // 闪光灯自动状态图标
        imageFlashAuto = R.drawable.ic_flash_auto;
        // 最长录制时间
        duration = 10;
        // 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
        minDuration = 1500;
        videoEditCoordinator = null;
        watermarkResource = -1;
    }


    // region 属性

    /**
     * 选择 视频图片 的类型，MimeType.allOf()
     */
    public Set<MimeType> mimeTypeSet;
    /**
     * 切换前置/后置摄像头图标资源
     */
    public int imageSwitch = R.drawable.ic_camera_zjh;
    /**
     * 闪光灯开启状态图标
     */
    public int imageFlashOn = R.drawable.ic_flash_on;
    /**
     * 闪光灯关闭状态图标
     */
    public int imageFlashOff = R.drawable.ic_flash_off;
    /**
     * 闪光灯自动状态图标
     */
    public int imageFlashAuto = R.drawable.ic_flash_auto;
    /**
     * 最长录制时间
     */
    public int duration = 10;
    /**
     * 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
     */
    public int minDuration = 1500;
    /**
     * 视频编辑功能
     */
    public VideoEditCoordinator videoEditCoordinator;
    /**
     * 水印资源id
     */
    public int watermarkResource = -1;
    /**
     * 是否点击即录制（点击拍摄图片功能则失效）
     */
    public boolean isClickRecord;
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

    /**
     * CameraView有关事件
     */
    public OnCameraViewListener onCameraViewListener;

}
