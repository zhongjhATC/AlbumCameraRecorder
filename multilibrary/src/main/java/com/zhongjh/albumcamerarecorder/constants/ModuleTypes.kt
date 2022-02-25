package com.zhongjh.albumcamerarecorder.constants

import androidx.annotation.IntDef

/**
 * 模块类型，区分相册，摄像机，录音机
 *
 * @author zhongjh
 * @date 2019/1/18
 */
// @IntDef 限定常量不允许重复
@IntDef(ModuleTypes.ALBUM, ModuleTypes.CAMERA, ModuleTypes.RECORDER)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class ModuleTypes {
    companion object {
        const val ALBUM = 0
        const val CAMERA = 1
        const val RECORDER = 2
    }
}