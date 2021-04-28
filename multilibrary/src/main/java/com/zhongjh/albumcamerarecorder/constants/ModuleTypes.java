package com.zhongjh.albumcamerarecorder.constants;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 模块类型，区分相册，摄像机，录音机
 *
 * @author zhongjh
 * @date 2019/1/18
 */
// @IntDef 来限定常量不允许重复
@IntDef({
        ModuleTypes.ALBUM,
        ModuleTypes.CAMERA,
        ModuleTypes.RECORDER
})
@Retention(RetentionPolicy.SOURCE)
public @interface ModuleTypes {

    int ALBUM = 0;
    int CAMERA = 1;
    int RECORDER = 2;

}
