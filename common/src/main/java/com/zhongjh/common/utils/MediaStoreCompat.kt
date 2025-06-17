package com.zhongjh.common.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.zhongjh.common.entity.SaveStrategy
import java.io.File

/**
 * 有关多媒体的文件操作
 *
 * @author zhongjh
 * @date 2018/8/23
 *
 * @param context 上下文，因为有的是Activity,所以使用弱引用
 * @param saveStrategy 设置目录
 */
class MediaStoreCompat(private val context: Context, var saveStrategy: SaveStrategy) {

    fun getUri(path: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val authority = context.packageName + ".zhongjhProvider"
            FileProvider.getUriForFile(context, authority, File(path))
        } else {
            Uri.fromFile(File(path))
        }
    }

    fun getUri(): Uri {
        return FileProvider.getUriForFile(context, saveStrategy.authority!!, File(saveStrategy.directory))
    }

}