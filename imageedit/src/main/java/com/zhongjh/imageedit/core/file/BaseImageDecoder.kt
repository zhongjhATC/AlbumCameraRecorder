package com.zhongjh.imageedit.core.file

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

/**
 * 图像解码器的抽象基类，定义了图像解码的基本接口和方法
 * 作为图像编辑器中各类图像解码器的统一基类，提供通用的图像解码框架
 * 派生类需要实现具体的解码逻辑，支持从不同来源（文件、内容URI、资源等）加载图像
 * @param uri 图像文件的Uri路径，表示要解码的图像源
 *
 * @author zhongjh
 * @date 2025/08/27
 */
abstract class BaseImageDecoder(var uri: Uri) {

    /**
     * 使用指定的配置选项解码图像文件
     * 抽象方法，由派生类实现具体的解码逻辑
     * 该方法是整个解码器框架的核心，不同类型的解码器通过实现此方法提供特定的图像加载逻辑
     *
     * @param options 解码配置选项，可以控制解码过程中的各种参数，如采样率、颜色模式等
     * 通过设置这些参数，可以优化内存使用、调整图像质量和控制解码行为
     * 常见的配置包括inSampleSize（采样率）、inPreferredConfig（像素格式）等
     * @return 解码后的Bitmap对象，如果解码失败则返回null
     * 解码失败的原因可能包括文件不存在、格式不支持、权限不足等
     */
    abstract fun decode(options: BitmapFactory.Options): Bitmap?
}
