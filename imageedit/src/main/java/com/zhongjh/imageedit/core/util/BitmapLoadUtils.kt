package com.zhongjh.imageedit.core.util;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

/**
 * Bitmap加载工具类，提供计算Bitmap最大尺寸和采样大小的静态方法
 * 主要用于优化图像加载过程，确保图像不会超过设备硬件限制，同时节省内存使用
 * 
 * @author Oleksii Shliama (<a href="https://github.com/shliama">...</a>)
 */
public class BitmapLoadUtils {

    /**
     * 日志标签，用于调试和日志输出
     */
    private static final String TAG = "BitmapLoadUtils";

    /**
     * 计算Bitmap的最大尺寸，同时考虑设备屏幕尺寸、Canvas和OpenGL的最大纹理尺寸限制
     * 默认实现中，最大尺寸为设备屏幕对角线的两倍，以确保有足够的质量进行图像缩放
     * 该方法综合考虑了多种硬件限制，确保生成的Bitmap能够被设备正常处理
     * 
     * @param context 应用上下文对象，用于获取窗口管理器服务和设备显示信息
     * @return Bitmap的最大尺寸（像素），取设备屏幕对角线两倍、Canvas最大尺寸和OpenGL最大纹理尺寸中的最小值
     */
    public static int calculateMaxBitmapSize(@NonNull Context context) {
        WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display;
        int width, height;
        Point size = new Point();

        if (wm != null) {
            display = wm.getDefaultDisplay();
            display.getSize(size);
        }

        width = size.x;
        height = size.y;

        // Twice the device screen diagonal as default
        int maxBitmapSize = (int) Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));

        // Check for max texture size via Canvas
        Canvas canvas = new Canvas();
        final int maxCanvasSize = Math.min(canvas.getMaximumBitmapWidth(), canvas.getMaximumBitmapHeight());
        if (maxCanvasSize > 0) {
            maxBitmapSize = Math.min(maxBitmapSize, maxCanvasSize);
        }

        // Check for max texture size via GL
        final int maxTextureSize = EglUtils.getMaxTextureSize();
        if (maxTextureSize > 0) {
            maxBitmapSize = Math.min(maxBitmapSize, maxTextureSize);
        }

        Log.d(TAG, "maxBitmapSize: " + maxBitmapSize);
        return maxBitmapSize;
    }

    /**
     * 计算Bitmap的采样大小(inSampleSize)，用于高效加载大图像
     * 采样大小表示图像的宽高将被缩小的倍数，该值必须是2的幂次
     * 
     * @param options BitmapFactory.Options对象，包含原始图像的宽高信息
     * @param reqWidth 需要的图像宽度
     * @param reqHeight 需要的图像高度
     * @return 计算出的采样大小，确保图像宽高都不超过所需尺寸
     */
    public static int calculateInSampleSize(@NonNull BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width lower or equal to the requested height and width.
            while ((height / inSampleSize) > reqHeight || (width / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

}