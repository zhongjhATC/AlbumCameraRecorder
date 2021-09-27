package com.zhongjh.albumcamerarecorder.listener;

import android.content.Context;

import java.io.File;
import java.io.IOException;

/**
 * 压缩接口
 *
 * @author zhongjh
 * @date 2021/9/26
 */
public interface CompressionInterface {

    File compressionFile(Context context, File file) throws IOException;

}
