package com.zhongjh.albumcamerarecorder.camera.constants;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 拍摄界面标记的两个类型
 *
 * @author zhongjh
 * @date 2021/12/22
 */
@IntDef({MediaTypes.TYPE_PICTURE, MediaTypes.TYPE_VIDEO, MediaTypes.TYPE_AUDIO})
@Retention(RetentionPolicy.SOURCE)
public @interface MediaTypes {

    /**
     * 图片
     */
    int TYPE_PICTURE = 0x001;
    /**
     * 视频
     */
    int TYPE_VIDEO = 0x002;

    /**
     * 音频
     */
    int TYPE_AUDIO = 0x003;

}
