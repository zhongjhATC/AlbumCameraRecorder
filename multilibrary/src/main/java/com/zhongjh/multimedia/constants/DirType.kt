package com.zhongjh.multimedia.constants;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 文件夹类型
 *
 * @author zhongjh
 * @date 2024/11/28
 */
@StringDef({DirType.CACHE, DirType.TEMP, DirType.COMPRESS})
@Retention(RetentionPolicy.SOURCE)
public @interface DirType {

    /**
     * 拍照、录制的临时文件夹。提交前创建的文件存放于这里
     */
    String CACHE = "AlbumCameraRecorderCache";
    /**
     * 拍照、录制的临时文件夹。提交后的文件存放于这里 Android Q提交后会把文件复制添加到公共文件夹Pictures,Android Q以下则是刷新或者复制到某个文件夹
     */
    String TEMP = "AlbumCameraRecorderTemp";
    /**
     * 存放压缩的文件夹
     */
    String COMPRESS = "AlbumCameraRecorderCompress";

}
