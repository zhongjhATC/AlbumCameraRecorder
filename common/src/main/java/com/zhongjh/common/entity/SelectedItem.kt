package com.zhongjh.common.entity

/**
 * 选中项包装实体，单一容器承载媒体+选中序号
 */
data class SelectedItem(val media: LocalMedia, var order: Int)