package com.zhongjh.cameraapp.configuration;

import android.content.Context;

import androidx.annotation.NonNull;

import com.zhongjh.albumcamerarecorder.listener.OnImageCompressionListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

/**
 * luban压缩
 *
 * @author zhongjh
 * @date 2021/9/26
 */
public class OnImageCompressionLuBan implements OnImageCompressionListener {

    @NonNull
    @Override
    public File compressionFile(@NotNull Context context, @NotNull File file) throws IOException {
        // 这是luban压缩
//        return Luban.with(context).load(file).get().get(0);
        // 这是Compressor压缩，选择你平常使用的压缩
        return new Compressor(context).compressToFile(file);
    }

}
