package com.zhongjh.albumcamerarecorder.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
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
        float textSize = 0;
        try {
            TypedValue typedValue = new TypedValue();
            int[] attribute = new int[]{attr};
            TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
            textSize = array.getDimensionPixelSize(0, 0);
            array.recycle();
            return textSize;
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        int textSize = 0;
        try {
            TypedValue typedValue = new TypedValue();
            int[] attribute = new int[]{attr};
            TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
            textSize = array.getDimensionPixelSize(0, 0);
            array.recycle();
            return textSize;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return textSize;
    }

    /**
     * 通过resId获取资源系列，再获取它里面的某个资源
     * attrs drawable
     *
     * @param context      上下文
     * @param resId        资源系列一套
     * @param attr         需要获取资源的id
     * @return 字体大小
     */
    public static int getTypeValueSizeForInt(Context context, @StyleRes int resId, int attr) {
        int textSize;
        int[] attribute = new int[]{attr};
        TypedArray array = context.getTheme().obtainStyledAttributes(resId, attribute);
        textSize = array.getDimensionPixelSize(0, 0);
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
        int color = 0;
        try {
            TypedValue typedValue = new TypedValue();
            int[] attribute = new int[]{attr};
            TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
            color = array.getColor(0, 0);
            array.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return color;
    }

    /**
     * 通过resId获取资源系列，再获取它里面的某个资源
     * attrs drawable
     *
     * @param context      上下文
     * @param resId        资源系列一套
     * @param attr         需要获取资源的id
     * @return 颜色id
     */
    public static int getTypeValueColor(Context context, @StyleRes int resId, int attr) {
        int color;
        int[] attribute = new int[]{attr};
        TypedArray array = context.getTheme().obtainStyledAttributes(resId, attribute);
        color = array.getColor(0, 0);
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
        ColorStateList colorStateList = null;
        try {
            TypedValue typedValue = new TypedValue();
            int[] attribute = new int[]{attr};
            TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
            colorStateList = array.getColorStateList(0);
            array.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        boolean flag = false;
        try {
            TypedValue typedValue = new TypedValue();
            int[] attribute = new int[]{attr};
            TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
            flag = array.getBoolean(0, false);
            array.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        Drawable drawable = null;
        try {
            TypedValue typedValue = new TypedValue();
            int[] attribute = new int[]{attr};
            TypedArray array = context.obtainStyledAttributes(typedValue.resourceId, attribute);
            drawable = array.getDrawable(0);
            array.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {
            if (colors.length == 2) {
                int[][] states = new int[2][];
                states[0] = new int[]{-android.R.attr.state_selected};
                states[1] = new int[]{android.R.attr.state_selected};
                return new ColorStateList(states, colors);
            } else {
                return ColorStateList.valueOf(colors[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
