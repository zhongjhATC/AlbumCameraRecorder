package com.zhongjh.multimedia.settings

/**
 * @author zhongjh
 */
object RecordeSpec {

    // region start 属性

    /**
     * 最长录制时间，单位为毫秒
     */
    var maxDuration = 11000

    /**
     * 最短录制时间限制，单位为毫秒，如果录制期间低于2000毫秒，均不算录制
     * 值不能低于2000，如果低于2000还是以2000为准
     */
    var minDuration = 2000

    /**
     * 长按准备时间，单位为毫秒，即是如果长按在1000毫秒内，都暂时不开启录制
     */
    var readinessDuration = 1000

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
        maxDuration = 11000
        // 最短录制时间限制，单位为毫秒，如果录制期间低于2000毫秒，均不算录制
        minDuration = 2000
        readinessDuration = 1000
    }
}