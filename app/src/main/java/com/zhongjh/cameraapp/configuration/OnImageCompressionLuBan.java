package com.zhongjh.cameraapp.configuration;

import android.content.Context;

import androidx.annotation.NonNull;

import com.zhongjh.albumcamerarecorder.listener.OnImageCompressionListener;
import com.zhongjh.albumcamerarecorder.utils.FileMediaUtil;
import com.zhongjh.common.utils.FileUtils;

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
        File compressFile = new Compressor(context).compressToFile(file);
        File newFile = FileMediaUtil.INSTANCE.createTempFile(context, file.getName());
        // 如果想移动到自己想存放的文件夹,则使用这个,否则直接返回compressFile即可
        FileUtils.move(compressFile, newFile);
        return newFile;
    }

}
