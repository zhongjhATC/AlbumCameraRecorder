package com.zhongjh.multimedia.listener

/**
 * 日志接口
 * 虽然功能都捕获了相关异常，但是一般开发都是需要记录为何报错，可以让下次修复
 *
 * @author zhongjh
 * @date 2021/9/26
 */
interface OnLogListener {

    /**
     * 输出错误日志
     * @param throwable 错误信息
     */
    fun logError(throwable: Throwable)

}