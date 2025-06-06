package com.zhongjh.albumcamerarecorder.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;

import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

/**
 * 动态获取attrs
 *
 * @author zhongjh
 */
public class AttrsUtils {

    /**
     * 获取TypedArray集合，做完相关操作记得释放
     * TypedArray.recycle();
     *
     * @param context 上下文
     * @param attr    资源
     * @return TypedArray
     */
    public static TypedArray getTypedArray(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{attr};
        return context.obtainStyledAttributes(typedValue.resourceId, attribute);
    }

    /**
     * get attrs size
     *
     * @param context 上下文
     * @param attr    资源
     * @return value
     */
    public static float getTypeValueSize(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{attr};
        TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
        float textSize = array.getDimensionPixelSize(0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            array.close();
        }
        array.recycle();
        return textSize;
    }

    /**
     * get attrs size
     *
     * @param context 上下文
     * @param attr    资源
     * @return value
     */
    public static int getTypeValueSizeForInt(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{attr};
        TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
        int textSize = array.getDimensionPixelSize(0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            array.close();
        }
        array.recycle();
        return textSize;
    }

    /**
     * 通过resId获取资源系列，再获取它里面的某个资源
     * attrs drawable
     *
     * @param context 上下文
     * @param resId   资源系列一套
     * @param attr    需要获取资源的id
     * @return 字体大小
     */
    public static int getTypeValueSizeForInt(Context context, @StyleRes int resId, int attr) {
        int[] attribute = new int[]{attr};
        TypedArray array = context.getTheme().obtainStyledAttributes(resId, attribute);
        int textSize = array.getDimensionPixelSize(0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            array.close();
        }
        array.recycle();
        return textSize;
    }

    /**
     * get attrs color
     *
     * @param context 上下文
     * @param attr    资源
     * @return value
     */
    public static int getTypeValueColor(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{attr};
        TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
        int color = array.getColor(0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            array.close();
        }
        array.recycle();
        return color;
    }

    /**
     * 通过resId获取资源系列，再获取它里面的某个资源
     * attrs drawable
     *
     * @param context 上下文
     * @param resId   资源系列一套
     * @param attr    需要获取资源的id
     * @return 颜色id
     */
    public static int getTypeValueColor(Context context, @StyleRes int resId, int attr) {
        int[] attribute = new int[]{attr};
        TypedArray array = context.getTheme().obtainStyledAttributes(resId, attribute);
        int color = array.getColor(0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            array.close();
        }
        array.recycle();
        return color;
    }

    /**
     * get attrs color
     *
     * @param context 上下文
     * @param attr    资源
     * @return value
     */
    public static ColorStateList getTypeValueColorStateList(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{attr};
        TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
        ColorStateList colorStateList = array.getColorStateList(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            array.close();
        }
        array.recycle();
        return colorStateList;
    }

    /**
     * get attrs boolean
     *
     * @param context 上下文
     * @param attr    资源
     * @return value
     */
    public static boolean getTypeValueBoolean(Context context, int attr) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{attr};
        TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
        boolean flag = array.getBoolean(0, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            array.close();
        }
        array.recycle();
        return flag;
    }

    /**
     * attrs drawable
     *
     * @param context 上下文
     * @param attr    资源
     * @return value
     */
    public static Drawable getTypeValueDrawable(Context context, int attr, int defaultResId) {
        TypedValue typedValue = new TypedValue();
        int[] attribute = new int[]{attr};
        TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
        Drawable drawable = array.getDrawable(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            array.close();
        }
        array.recycle();
        return drawable == null ? ContextCompat.getDrawable(context, defaultResId) : drawable;
    }

    /**
     * 通过resId获取资源系列，再获取它里面的某个资源
     * attrs drawable
     *
     * @param context      上下文
     * @param resId        资源系列一套
     * @param attr         需要获取资源的id
     * @param defaultResId 默认图片id
     * @return 图片id
     */
    public static Drawable getTypeValueDrawable(Context context, @StyleRes int resId, int attr, int defaultResId) {
        Drawable drawable;
        int[] attribute = new int[]{attr};
        TypedArray array = context.getTheme().obtainStyledAttributes(resId, attribute);
        drawable = array.getDrawable(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            array.close();
        }
        array.recycle();
        return drawable == null ? ContextCompat.getDrawable(context, defaultResId) : drawable;
    }

    /**
     * getColorStateList
     *
     * @param colors 颜色数组
     * @return ColorStateList
     */
    public static ColorStateList getColorStateList(int[] colors) {
        if (colors.length == 2) {
            int[][] states = new int[2][];
            states[0] = new int[]{-android.R.attr.state_selected};
            states[1] = new int[]{android.R.attr.state_selected};
            return new ColorStateList(states, colors);
        } else {
            return ColorStateList.valueOf(colors[0]);
        }
    }
}
