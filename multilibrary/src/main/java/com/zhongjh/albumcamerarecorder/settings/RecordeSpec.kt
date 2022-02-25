package com.zhongjh.albumcamerarecorder.settings

/**
 * @author zhongjh
 */
object RecordeSpec {

    // region start 属性

    /**
     * 最长录制时间
     */
    var duration = 10

    /**
     * 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
     */
    var minDuration = 1500

    // region end 属性

    val cleanInstance = RecordeSpec
        get() {
            val recordeSpec: RecordeSpec = field
            recordeSpec.reset()
            return recordeSpec
        }

    /**
     * 重置
     */
    private fun reset() {
        // 最长录制时间
        duration = 10
        // 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
        minDuration = 1500
    }
}