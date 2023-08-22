package com.zhongjh.albumcamerarecorder.album.listener

import com.zhongjh.common.entity.LocalMedia

/**
 * 相册item事件
 * @author zhihu
 */
interface OnSelectedListener {
    /**
     * 每次选择的事件
     * @param localFiles 所选项目[com.zhongjh.common.entity.LocalMedia] 列表.
     */
    fun onSelected(localFiles: List<LocalMedia>)
}