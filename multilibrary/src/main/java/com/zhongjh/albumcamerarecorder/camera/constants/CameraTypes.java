package com.zhongjh.albumcamerarecorder.camera.constants;

import androidx.annotation.IntDef;

import com.zhongjh.albumcamerarecorder.constants.ModuleTypes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 拍摄界面标记的两个类型
 * @author zhongjh
 * @date 2021/12/22
 */
@IntDef({
        CameraTypes.TYPE_PICTURE,
        CameraTypes.TYPE_VIDEO
})
@Retention(RetentionPolicy.SOURCE)
public @interface CameraTypes {

    /**
     * 图片
     */
    int TYPE_PICTURE = 0x001;
    /**
     * 视频
     */
    int TYPE_VIDEO = 0x002;

}
