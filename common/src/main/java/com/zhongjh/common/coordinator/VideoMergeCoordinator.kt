package com.zhongjh.common.coordinator

/**
 * 视频合并协调者
 *
 * @author zhongjh
 */
interface VideoMergeCoordinator {

    /**
     * 合并视频
     */
    fun merge(mp4PathList: List<String>, outPutPath: String)

}