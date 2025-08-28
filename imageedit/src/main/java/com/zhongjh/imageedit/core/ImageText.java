package com.zhongjh.imageedit.core;

import android.text.TextUtils;

import androidx.annotation.NonNull;

/**
 * 图像文本类，用于存储和管理图像上显示的文本内容和颜色信息
 * 是图像编辑器中实现文字编辑功能的基础数据模型类
 * 提供文本的获取、设置、判空和长度计算等基本操作
 * 支持文本内容的动态更新和颜色调整，为文字添加到图像提供数据支持
 * 
 * @author felix
 * @date 2017/12/1 下午2:43
 */
public class ImageText {

    /**
     * 文本内容字符串
     * 存储图像上显示的具体文字信息
     */
    private String text;

    /**
     * 文本颜色值，使用Android颜色整数表示
     * 格式为ARGB，如0xFFFFFFFF表示白色，0xFF000000表示黑色
     */
    private int color;

    /**
     * 构造函数，创建图像文本对象
     * 
     * @param text 文本内容字符串，要在图像上显示的文字
     * @param color 文本颜色值，ARGB格式的颜色整数
     */
    public ImageText(String text, int color) {
        // 初始化文本内容和颜色
        this.text = text;
        this.color = color;
    }

    /**
     * 获取文本内容
     * 
     * @return 文本内容字符串
     */
    public String getText() {
        // 返回当前存储的文本内容
        return text;
    }

    /**
     * 设置文本内容
     * 
     * @param text 新的文本内容字符串
     */
    public void setText(String text) {
        // 更新存储的文本内容
        this.text = text;
    }

    /**
     * 获取文本颜色
     * 
     * @return 文本颜色值
     */
    public int getColor() {
        // 返回当前存储的文本颜色
        return color;
    }

    /**
     * 设置文本颜色
     * 
     * @param color 新的文本颜色值
     */
    public void setColor(int color) {
        // 更新存储的文本颜色
        this.color = color;
    }

    /**
     * 检查文本是否为空
     * 
     * @return 如果文本为空或null则返回true，否则返回false
     */
    public boolean isEmpty() {
        // 使用Android的TextUtils工具类检查文本是否为空或null
        return TextUtils.isEmpty(text);
    }

    /**
     * 获取文本长度
     * 
     * @return 文本的字符长度，如果文本为空则返回0
     */
    public int length() {
        // 如果文本为空返回0，否则返回文本的字符长度
        return isEmpty() ? 0 : text.length();
    }

    /**
     * 返回文本对象的字符串表示
     * 包含文本内容和颜色信息，方便调试和日志输出
     * 
     * @return 包含文本内容和颜色信息的字符串
     */
    @NonNull
    @Override
    public String toString() {
        // 构建并返回包含文本内容和颜色信息的字符串表示
        return "IMGText{" +
                "text='" + text + '\'' +
                ", color=" + color +
                '}';
    }
}
