package com.zhongjh.albumcamerarecorder.camera.constants;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 闪光灯的类型
 * @author zhongjh
 * @date 2021/12/22
 */
@IntDef({
        FlashModels.TYPE_FLASH_AUTO,
        FlashModels.TYPE_FLASH_ON,
        FlashModels.TYPE_FLASH_OFF
})
@Retention(RetentionPolicy.SOURCE)
public @interface FlashModels {

    /**
     * 闪关灯自动
     */
    int TYPE_FLASH_AUTO = 0x101;
    /**
     * 闪关灯开启
     */
    int TYPE_FLASH_ON = 0x102;
    /**
     * 闪关灯关闭
     */
    int TYPE_FLASH_OFF = 0x103;

}
