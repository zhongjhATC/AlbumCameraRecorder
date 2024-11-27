package com.zhongjh.common.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.zhongjh.common.enums.MimeType
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


/**
 * @author：luck
 * @date：2022/11/30 3:33 下午
 * @describe：BitmapUtils
 */
object BitmapUtils {
    private const val ARGB_8888_MEMORY_BYTE = 4
    private const val MAX_BITMAP_SIZE = 100 * 1024 * 1024
    private const val UNSET = -1

    /**
     * 判断拍照 图片是否旋转
     * 如果旋转则纠正
     *
     * @param context 上下文
     * @param path    图片路径
     */
    fun rotateImage(context: Context, path: String) {
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null
        var bitmap: Bitmap? = null
        try {
            val degree: Int = readPictureDegree(context, path)
            if (degree > 0) {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                if (MimeType.isContent(path)) {
                    inputStream = context.contentResolver.openInputStream(Uri.parse(path))
                    BitmapFactory.decodeStream(inputStream, null, options)
                } else {
                    BitmapFactory.decodeFile(path, options)
                }
                // 算出比例,随后按照比例缩小
                options.inSampleSize = computeSize(options.outWidth, options.outHeight)
                options.inJustDecodeBounds = false
                if (MimeType.isContent(path)) {
                    inputStream = context.contentResolver.openInputStream(Uri.parse(path))
                    bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                } else {
                    bitmap = BitmapFactory.decodeFile(path, options)
                }
                if (bitmap != null) {
                    // 对图片进行旋转
                    bitmap = rotatingImage(bitmap, degree)
                    outputStream = if (MimeType.isContent(path)) {
                        context.contentResolver.openOutputStream(Uri.parse(path)) as FileOutputStream
                    } else {
                        FileOutputStream(path)
                    }
                    saveBitmapFile(bitmap, outputStream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream?.close()
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    /**
     * 计算图像大小
     * @param imageWidth 图片宽度
     * @param imageHeight 图片高度
     */
    fun getComputeImageSize(imageWidth: Int, imageHeight: Int): IntArray {
        var maxWidth: Int = UNSET
        var maxHeight: Int = UNSET
        if (imageWidth == 0 && imageHeight == 0) {
            return intArrayOf(maxWidth, maxHeight)
        }
        var inSampleSize: Int = computeSize(imageWidth, imageHeight)
        val totalMemory: Long = getTotalMemory()
        var decodeAttemptSuccess = false
        while (!decodeAttemptSuccess) {
            maxWidth = imageWidth / inSampleSize
            maxHeight = imageHeight / inSampleSize
            val bitmapSize: Int = maxWidth * maxHeight * ARGB_8888_MEMORY_BYTE
            if (bitmapSize > totalMemory) {
                inSampleSize *= 2
                continue
            }
            decodeAttemptSuccess = true
        }
        return intArrayOf(maxWidth, maxHeight)
    }

    /**
     * 计算尺寸
     * @param width 图片宽度
     * @param height 图片高度
     */
    private fun computeSize(width: Int, height: Int): Int {
        var srcWidth = width
        var srcHeight = height
        srcWidth = if (srcWidth % 2 == 1) srcWidth + 1 else srcWidth
        srcHeight = if (srcHeight % 2 == 1) srcHeight + 1 else srcHeight
        val longSide = max(srcWidth, srcHeight)
        val shortSide = min(srcWidth, srcHeight)
        val scale = shortSide.toFloat() / longSide
        return if (scale <= 1 && scale > 0.5625) {
            when {
                longSide < 1664 -> {
                    1
                }

                longSide < 4990 -> {
                    2
                }

                longSide in 4991..10239 -> {
                    4
                }

                else -> {
                    longSide / 1280
                }
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (longSide / 1280 == 0) 1 else longSide / 1280
        } else {
            ceil(longSide / (1280.0 / scale)).toInt()
        }
    }

    private fun getTotalMemory(): Long {
        val totalMemory = Runtime.getRuntime().totalMemory()
        return if (totalMemory > MAX_BITMAP_SIZE) MAX_BITMAP_SIZE.toLong() else totalMemory
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param context
     * @param filePath 图片绝对路径
     * @return degree旋转的角度
     */
    private fun readPictureDegree(context: Context, filePath: String): Int {
        var exifInterface: ExifInterface? = null
        var inputStream: InputStream? = null
        try {
            if (MimeType.isContent(filePath)) {
                inputStream = context.contentResolver.openInputStream(Uri.parse(filePath))
                inputStream?.let {
                    exifInterface = ExifInterface(inputStream)
                }
            } else {
                exifInterface = ExifInterface(filePath)
            }
            val orientation =
                exifInterface?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) ?: 0
            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return 0
        } finally {
            inputStream?.close()
        }
    }

    /**
     * 旋转Bitmap
     *
     * @param bitmap
     * @param angle
     * @return
     */
    private fun rotatingImage(bitmap: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * 保存Bitmap至本地
     *
     * @param bitmap
     * @param fos
     */
    private fun saveBitmapFile(bitmap: Bitmap, fos: FileOutputStream) {
        var stream: ByteArrayOutputStream? = null
        try {
            stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.write(stream.toByteArray())
            fos.flush()
            fos.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            fos.close()
            stream?.close()
        }
    }

}