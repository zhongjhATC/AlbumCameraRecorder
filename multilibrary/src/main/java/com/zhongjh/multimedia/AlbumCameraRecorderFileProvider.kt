package com.zhongjh.multimedia

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * @author zhongjh
 *
 * 自定义FileProvider 解决FileProvider冲突问题
 */
class AlbumCameraRecorderFileProvider : FileProvider() {
    companion object {
        /**
         * 获取文件的内容URI（安全的文件访问方式）
         * @param context 上下文对象
         * @param file 需要共享的文件
         * @return 内容URI，格式为 content://authority/path
         */
        fun getUriForFile(context: Context, file: File): Uri {
            // 注意：必须通过FileProvider类名直接调用静态方法，而非子类
            return getUriForFile(context, context.packageName + ".zhongjhProvider", file)
        }
    }
}
