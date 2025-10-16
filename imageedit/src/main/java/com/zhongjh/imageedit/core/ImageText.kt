package com.zhongjh.imageedit.core

import android.text.TextUtils

/**
 * 图像文本类，用于存储和管理图像上显示的文本内容和颜色信息
 * 是图像编辑器中实现文字编辑功能的基础数据模型类
 * 提供文本的获取、设置、判空和长度计算等基本操作
 * 支持文本内容的动态更新和颜色调整，为文字添加到图像提供数据支持
 *
 * @param text 文本颜色值，使用Android颜色整数表示
 * @param color 格式为ARGB，如0xFFFFFFFF表示白色，0xFF000000表示黑色
 *
 * @author zhongjh
 * @date 2025/10/16
 */
class ImageText(var text: String, var color: Int) {

    val isEmpty: Boolean
        /**
         * 检查文本是否为空
         *
         * @return 如果文本为空或null则返回true，否则返回false
         */
        get() {
            // 使用Android的TextUtils工具类检查文本是否为空或null
            return TextUtils.isEmpty(text)
        }

    /**
     * 获取文本长度
     *
     * @return 文本的字符长度，如果文本为空则返回0
     */
    fun length(): Int {
        // 如果文本为空返回0，否则返回文本的字符长度
        return if (this.isEmpty) 0 else text.length
    }

    /**
     * 返回文本对象的字符串表示
     * 包含文本内容和颜色信息，方便调试和日志输出
     *
     * @return 包含文本内容和颜色信息的字符串
     */
    override fun toString(): String {
        // 构建并返回包含文本内容和颜色信息的字符串表示
        return "IMGText{" +
                "text='" + text + '\'' +
                ", color=" + color +
                '}'
    }
}
