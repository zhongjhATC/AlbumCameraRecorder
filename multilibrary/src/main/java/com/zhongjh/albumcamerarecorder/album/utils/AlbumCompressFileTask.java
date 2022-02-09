package com.zhongjh.albumcamerarecorder.album.utils;

import android.content.Context;
import android.util.Log;

import com.zhongjh.albumcamerarecorder.camera.util.FileUtil;
import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;
import com.zhongjh.common.entity.LocalFile;
import com.zhongjh.common.listener.VideoEditListener;
import com.zhongjh.common.utils.MediaStoreCompat;
import com.zhongjh.common.utils.UriUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 这是相册界面和预览界面共用的一个异步线程逻辑
 *
 * @author zhongjh
 * @date 2022/2/9
 */
public class AlbumCompressFileTask {

    private final Context mContext;
    private final String mTag;
    private final Class<?> mClsKey;
    /**
     * 公共配置
     */
    private final GlobalSpec mGlobalSpec;
    /**
     * 图片文件配置路径
     */
    private final MediaStoreCompat mPictureMediaStoreCompat;
    /**
     * 录像文件配置路径
     */
    private final MediaStoreCompat mVideoMediaStoreCompat;

    public AlbumCompressFileTask(Context context, String tag, Class<?> clsKey, GlobalSpec globalSpec,
                                 MediaStoreCompat pictureMediaStoreCompat, MediaStoreCompat videoMediaStoreCompat) {
        mContext = context;
        mTag = tag;
        mClsKey = clsKey;
        mGlobalSpec = globalSpec;
        mPictureMediaStoreCompat = pictureMediaStoreCompat;
        mVideoMediaStoreCompat = videoMediaStoreCompat;
    }

    public ArrayList<LocalFile> compressFileTaskDoInBackground(ArrayList<LocalFile> localFiles) {
        // 将 缓存文件 拷贝到 配置目录
        ArrayList<LocalFile> newLocalFiles = new ArrayList<>();
        for (LocalFile item : localFiles) {
            // 判断是否需要压缩
            if (item.isVideo() && mGlobalSpec.videoCompressCoordinator == null) {
                newLocalFiles.add(item);
                continue;
            } else if (item.isGif()) {
                newLocalFiles.add(item);
                continue;
            } else if (item.isImage() && mGlobalSpec.imageCompressionInterface == null) {
                newLocalFiles.add(item);
                continue;
            }

            // 开始压缩逻辑，获取真实路径
            String path = null;
            if (item.getPath() == null) {
                File file = UriUtils.uriToFile(mContext, item.getUri());
                if (file != null) {
                    path = file.getAbsolutePath();
                }
            } else {
                path = item.getPath();
            }

            if (path != null) {
                // 移动文件,获取文件名称
                String newFileName = path.substring(path.lastIndexOf(File.separator));

                String[] newFileNames = newFileName.split("\\.");
                // 设置压缩后的照片名称，id_CMP
                newFileName = item.getId() + "_CMP";
                if (newFileNames.length > 1) {
                    // 设置后缀名
                    newFileName = newFileName + "." + newFileNames[1];
                }
                File newFile;
                if (item.isImage()) {
                    newFile = mPictureMediaStoreCompat.fineFile(newFileName, 0, false);
                } else if (item.isVideo()) {
                    newFile = mVideoMediaStoreCompat.fineFile(newFileName, 1, false);
                } else {
                    newFile = new File(path);
                }

                if (newFile.exists()) {
                    LocalFile localFile = new LocalFile(mContext, mPictureMediaStoreCompat, item, newFile);
                    newLocalFiles.add(localFile);
                    Log.d(mTag, "存在直接使用");
                } else {
                    if (item.isImage()) {
                        newLocalFiles.add(handleImage(path, newFile, item));
                    } else if (item.isVideo()) {
                        if (mGlobalSpec.isCompressEnable()) {
                            // 压缩视频
                            newFile = mVideoMediaStoreCompat.createFile(newFileName, 1, false);
                            File finalNewFile = newFile;
                            mGlobalSpec.videoCompressCoordinator.setVideoCompressListener(mClsKey, new VideoEditListener() {
                                @Override
                                public void onFinish() {
                                    LocalFile localFile = new LocalFile(mContext, mPictureMediaStoreCompat, item, finalNewFile);
                                    newLocalFiles.add(localFile);
                                    Log.d(mTag, "不存在新建文件");
                                }

                                @Override
                                public void onProgress(int progress, long progressTime) {
                                }

                                @Override
                                public void onCancel() {

                                }

                                @Override
                                public void onError(@NotNull String message) {
                                }
                            });
                            mGlobalSpec.videoCompressCoordinator.compressAsync(mClsKey, path, newFile.getPath());
                        }
                    }
                }
            }
        }
        return newLocalFiles;
    }

    /**
     * 处理图片
     *
     * @param path    图片真实路径
     * @param newFile 新创建的文件
     * @param item    当前需要处理的LocalFile
     * @return 压缩后新的LocalFile
     */
    private LocalFile handleImage(String path, File newFile, LocalFile item) {
        File oldFile = new File(path);
        // 根据类型压缩
        File compressionFile;
        if (mGlobalSpec.imageCompressionInterface != null) {
            // 压缩图片
            try {
                compressionFile = mGlobalSpec.imageCompressionInterface.compressionFile(mContext, oldFile);
            } catch (IOException e) {
                compressionFile = oldFile;
                e.printStackTrace();
            }
        } else {
            compressionFile = oldFile;
        }
        // 移动到新的文件夹
        FileUtil.copy(compressionFile, newFile);
        return new LocalFile(mContext, mPictureMediaStoreCompat, item, newFile);
    }

//    private

}
