package com.zhongjh.albumcamerarecorder.utils.constants;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 多媒体类型，区分图片，视频，音频
 * Created by zhongjh on 2019/1/18.
 */
// @IntDef 来限定常量不允许重复
@IntDef({
        MultimediaTypes.PICTURE,
        MultimediaTypes.VIDEO,
        MultimediaTypes.AUDIO,
        MultimediaTypes.BLEND
})
@Retention(RetentionPolicy.SOURCE) // 表示告诉编译器，该注解是源代码级别的，生成 class 文件的时候这个注解就被编译器自动去掉了。
public @interface MultimediaTypes {

    /**
     * 图片
     */
    int PICTURE = 0;
    int VIDEO = 1;
    int AUDIO = 2;
    /**
     * 混合类型
     */
    int BLEND = 3;
}
