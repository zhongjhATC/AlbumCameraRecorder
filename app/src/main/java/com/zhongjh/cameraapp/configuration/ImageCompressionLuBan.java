package com.zhongjh.cameraapp.configuration;

import android.content.Context;

import com.zhongjh.albumcamerarecorder.listener.ImageCompressionInterface;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import top.zibin.luban.Luban;

/**
 * luban压缩
 *
 * @author zhongjh
 * @date 2021/9/26
 */
public class ImageCompressionLuBan implements ImageCompressionInterface {

    @Override
    public File compressionFile(@NotNull Context context, @NotNull File file) throws IOException {
        return Luban.with(context).load(file).get().get(0);
    }

}
