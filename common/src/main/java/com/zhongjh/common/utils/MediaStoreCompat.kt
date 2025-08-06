package com.zhongjh.common.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

/**
 * 有关多媒体的文件Uri操作
 *
 * @author zhongjh
 * @date 2018/8/23
 * @updateData 2025/8/6
 */
object MediaStoreCompat {

    fun getUri(context: Context, path: String): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val appContext = context.applicationContext
            val authority = appContext.packageName + ".zhongjhProvider"
            return FileProvider.getUriForFile(appContext, authority, File(path))
        } else {
            Uri.fromFile(File(path))
        }
    }

}