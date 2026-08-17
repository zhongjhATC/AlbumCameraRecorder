package com.zhongjh.multimedia.camera.ui.camera.motion.colorspace

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import com.zhongjh.multimedia.camera.ui.camera.motion.model.ColorSpaceInfo
import android.graphics.ColorSpace as AndroidColorSpace

/**
 * HDR转SDR色调映射工具
 * 导出JPEG/PNG这类普通图片格式的时候，必须把HDR高动态画面转成SDR普通色域
 * 如果不做转换，导出的图片会发白、高光细节丢失、色彩异常
 * 注意：依赖Android8.0(API26)以上ColorSpace相关系统API，低版本不能调用这个类
 */
@RequiresApi(Build.VERSION_CODES.O)
object HdrToneMapper {

    /**
     * 系统标准sRGB色域对象，手机相册、JPEG图片通用的普通色彩空间，懒加载只初始化一次
     */
    private val srgbColorSpace by lazy {
        AndroidColorSpace.get(AndroidColorSpace.Named.SRGB)
    }

    /**
     * 对外入口方法：处理导出用的位图，按需执行HDR转SDR
     * JPEG、PNG、WebP全部都是SDR格式，存不下HDR画面；
     * 如果原图本身就是SDR，直接原封不动返回原图，不做额外拷贝
     *
     * @param bitmap 输入原图，有可能是HDR高动态位图
     * @param colorSpaceInfo 图片色彩空间信息，标记是否为HDR
     * @return 处理完毕可以直接导出的位图；SDR输入直接返回原对象，HDR输入返回新生成的Bitmap副本
     */
    fun process(bitmap: Bitmap, colorSpaceInfo: ColorSpaceInfo): Bitmap {
        // 两个条件任意不满足直接返回原图：系统低于26 或者 非HDR图片
        if (!colorSpaceInfo.isHdr) {
            return bitmap
        }

        return toneMapToSdr(bitmap, colorSpaceInfo)
    }

    /**
     * 真正执行HDR → SDR sRGB转换
     * 原理：新建一张sRGB色域普通ARGB_8888画布，把HDR图片绘制上去，Android系统Canvas自动完成色域转换与色调映射
     * @param bitmap 原始HDR位图
     * @param colorSpaceInfo 色彩空间描述信息
     * @return 全新生成的SDR sRGB位图，调用方用完必须手动recycle回收内存
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun toneMapToSdr(bitmap: Bitmap, colorSpaceInfo: ColorSpaceInfo): Bitmap {
        // 目标输出色彩空间：标准sRGB
        val targetColorSpace = srgbColorSpace

        // 输出图片固定使用ARGB_8888
        // 原因：10位的RGBA_1010102高深度格式，Bitmap.compress压缩JPEG/PNG/WebP不支持，保存会出问题
        val config = Bitmap.Config.ARGB_8888

        // 创建一张软件位图（false代表不是HARDWARE硬件位图，需要支持Canvas绘图），指定sRGB色彩空间
        val output = createBitmap(
            bitmap.width,
            bitmap.height,
            config,
            false,
            targetColorSpace
        )

        // 将HDR原图绘制到新建的SDR画布上
        // Android Canvas底层自动处理色彩空间转换、HDR色调映射，不需要自己写复杂算法
        // 每次方法内部新建Paint对象，不要用全局静态Paint，避免多线程并发导致绘图异常
        val canvas = Canvas(output)
        val paint = Paint().apply {
            // 开启图片滤波，绘图缩放时画面更平滑
            isFilterBitmap = true
            // 关闭抖动，HDR转SDR场景不需要抖动
            isDither = false
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        // 返回转换完成的SDR位图，注意：这是新对象，外部使用结束要recycle释放内存
        return output
    }
}
