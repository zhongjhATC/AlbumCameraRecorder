package com.zhongjh.multimedia

import android.content.Context
import com.zhongjh.common.utils.FileUtils.deleteDir
import com.zhongjh.common.utils.FileUtils.getSize
import java.io.File

/**
 * 开放的一些公共方法，主要是不依赖于GlobalSetting等设置
 *
 * @author zhongjh
 * @date 2021/9/26
 */
object AlbumCameraRecorderApi {
    /**
     * 获取缓存的文件大小
     *
     * @param context 上下文
     * @return 以 （xx + 单位） 的字符串形式返回，例如13B,13KB,13MB,13GB
     */
    @JvmStatic
    fun getFileSize(context: Context): String {
        val appContext = context.applicationContext
        appContext.externalCacheDir?.let { externalCacheDir ->
            val file = File(externalCacheDir.path)
            return getSize(file)
        } ?: let {
            return ""
        }
    }

    /**
     * 删除所有缓存文件
     *
     * @param context 上下文
     */
    @JvmStatic
    fun deleteCacheDirFile(context: Context) {
        deleteDir(context.externalCacheDir)
    }
}
