package com.zhongjh.cameraapp.configuration;

import android.content.Context;
import android.graphics.Picture;

import com.zhongjh.albumcamerarecorder.listener.CompressionInterface;

import java.io.File;
import java.io.IOException;

import top.zibin.luban.Luban;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * luban压缩
 *
 * @author zhongjh
 * @date 2021/9/26
 */
public class CompressionLuBan implements CompressionInterface {

    @Override
    public File compressionFile(Context context, File file) throws IOException {
        return Luban.with(context).load(file).get().get(0);
    }

}
