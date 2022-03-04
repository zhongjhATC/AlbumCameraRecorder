package com.zhongjh.albumcamerarecorder.album.listener

import com.zhongjh.common.entity.LocalFile

/**
 * 相册item事件
 * @author zhihu
 */
interface OnSelectedListener {
    /**
     * 每次选择的事件
     * @param localFiles 所选项目[com.zhongjh.common.entity.LocalFile] 列表.
     */
    fun onSelected(localFiles: List<LocalFile>)
}