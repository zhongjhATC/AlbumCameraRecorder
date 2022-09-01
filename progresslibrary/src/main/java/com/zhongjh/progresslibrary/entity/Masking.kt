package com.zhongjh.progresslibrary.entity

/**
 * 遮罩层相关属性
 * @param maskingColor       有关遮罩层：颜色
 * @param maskingTextSize    有关遮罩层：文字大小
 * @param maskingTextColor   有关遮罩层：文字颜色
 * @param maskingTextContent 有关遮罩层：文字内容
 *
 * @author zhongjh
 * @date 2022/9/1
 */
class Masking(
    var maskingColor: Int, var maskingTextSize: Int,
    var maskingTextColor: Int, var maskingTextContent: String
)
