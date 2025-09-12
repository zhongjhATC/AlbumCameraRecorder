package com.zhongjh.imageedit.core.file

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

/**
 * 文件系统图像解码器，继承自BaseImageDecoder
 * 专门用于从设备文件系统中解码图像文件
 * 支持加载本地存储中的图像资源，是图像编辑器中最常用的图像加载方式
 * 在图像编辑器中负责处理用户从本地文件系统选择的图像
 *
 * @param uri 图像文件的Uri路径
 *
 * @author zhongjh
 * @date 2025/09/12
 */
class ImageFileDecoder(uri: Uri) : BaseImageDecoder(uri) {
    /**
     * 解码图像文件，使用指定的配置选项
     * 实现了BaseImageDecoder中定义的抽象方法，专门用于从设备文件系统路径解码图像
     * 该方法首先验证URI和文件路径的有效性，然后检查文件是否存在，最后使用BitmapFactory进行实际解码
     *
     * @param options 解码配置选项，可以控制解码过程中的各种参数，如inSampleSize、inPreferredConfig等
     * 通过这些配置可以优化内存使用和图像质量
     * @return 解码后的Bitmap对象，如果解码失败或文件不存在则返回null
     */
    override fun decode(options: BitmapFactory.Options): Bitmap? {
        // 第一步：获取图像文件的Uri
        // 通过调用父类BaseImageDecoder的getUri()方法获取存储在解码器中的URI
        val uri = uri

        // 第二步：从Uri中提取文件路径
        // 注意：这种方法只适用于file://类型的URI，不适用于content://类型的URI
        val path = uri.path
        path?.let {
            // 第三步：创建File对象并检查文件是否存在
            // 这是一个重要的预检查步骤，可以避免尝试解码不存在的文件
            val file = File(path)
            if (file.exists()) {
                // 第四步：文件存在，使用BitmapFactory从文件路径解码图像
                // BitmapFactory.decodeFile是Android框架提供的从文件路径解码图像的标准方法
                // 传入options参数可以控制解码过程，如采样率、图像格式等，有助于优化内存使用
                return BitmapFactory.decodeFile(path, options)
            }
        }

        // 第四步：文件不存在，返回null
        return null
    }
}
