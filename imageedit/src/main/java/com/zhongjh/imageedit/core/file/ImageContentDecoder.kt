package com.zhongjh.imageedit.core.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.FileDescriptor

/**
 * Content URI图像解码器，继承自BaseImageDecoder
 * 专门用于从Android ContentProvider中解码图像资源
 * 通过ContentResolver访问content://类型URI的图像，支持从相册、文件管理器等应用中选择图像
 * 在图像编辑器中负责处理从非直接文件路径获取的图像数据
 *
 * @param context 应用上下文，用于访问ContentResolver
 * @param uri 要解码的图像URI，必须是content://类型的URI
 *
 * @author zhongjh
 */
class ImageContentDecoder(private val context: Context, uri: Uri) : BaseImageDecoder(uri) {
    /**
     * 解码图像文件，从ContentProvider获取图像数据
     * 实现了BaseImageDecoder中定义的抽象方法，专门用于从Android ContentProvider解码图像资源
     * 该方法使用ContentResolver访问content://类型URI的图像数据，支持从相册、文件管理器等应用中选择图像
     *
     * @param options 解码选项，可用于控制解码后的图像尺寸、格式等
     * 通过设置inSampleSize、inPreferredConfig等参数可以优化内存使用
     * @return 解码后的位图对象，如果解码失败则返回null
     */
    override fun decode(options: BitmapFactory.Options): Bitmap? {
        try {
            // 第一步：通过ContentResolver打开文件描述符
            // ContentResolver是Android系统提供的访问ContentProvider的标准接口
            // "r"表示以只读模式打开文件描述符
            // 这是访问ContentProvider中图像资源的标准方式
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")


            // 第二步：获取底层的FileDescriptor对象
            // FileDescriptor是表示文件、管道或套接字的抽象句柄
            // 它是BitmapFactory.decodeFileDescriptor方法所需的参数类型
            var fileDescriptor: FileDescriptor? = null
            if (parcelFileDescriptor != null) {
                fileDescriptor = parcelFileDescriptor.fileDescriptor
            }


            // 第三步：使用BitmapFactory从文件描述符中解码图像
            // BitmapFactory.decodeFileDescriptor是从文件描述符解码图像的标准方法
            // 传入options参数可以控制解码过程，如采样率、图像格式等，有助于优化内存使用
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options)


            // 第四步：确保关闭文件描述符，防止资源泄漏
            // 这是资源管理的重要步骤，必须在完成操作后关闭文件描述符
            parcelFileDescriptor?.close()


            // 返回解码成功的Bitmap对象
            return image
        } catch (e: Exception) {
            // 记录解码过程中的异常
            // 可能的异常包括FileNotFoundException（文件不存在）、SecurityException（无权限访问）等
            Log.e(TAG, "decode" + e.message)
        }


        // 如果解码过程中出现任何问题，返回null
        return null
    }

    companion object {
        /**
         * 日志标签，用于调试和错误日志输出
         */
        private const val TAG = "ImageContentDecoder"
    }
}
