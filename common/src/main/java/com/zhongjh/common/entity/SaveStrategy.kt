package com.zhongjh.common.entity

/**
 * 设置保存目录的实体
 * @author zhongjh
 * @param isPublic true表示拍照存储在共有目录，false表示存储在私有目录；
 * @param authority AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
 * @param directory 子文件夹的具体路径
 */
class SaveStrategy(var isPublic: Boolean, var authority: String?, var directory: String) {
    init {
        checkNotNull(authority) { "Authority cannot be null" }
    }
}