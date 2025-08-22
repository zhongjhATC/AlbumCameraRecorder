package com.zhongjh.common.utils

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 协程请求封装类
 * 功能：替代ThreadUtils的doInBackground/Success/Fail/Cancel逻辑
 */
class CoroutineRequest<T> private constructor(
    private val backgroundTask: suspend () -> T, // 后台任务（suspend函数）
    private val lifecycleScope: LifecycleCoroutineScope // 与生命周期绑定的作用域
) {
    // 成功回调（主线程）
    private var onSuccess: ((T) -> Unit)? = null

    // 失败回调（主线程）
    private var onFail: ((Exception) -> Unit)? = null

    // 取消回调（可选，主线程）
    private var onCancel: (() -> Unit)? = null

    /**
     * 设置成功回调
     */
    fun onSuccess(block: (T) -> Unit): CoroutineRequest<T> {
        this.onSuccess = block
        return this
    }

    /**
     * 设置失败回调
     */
    fun onFail(block: (Exception) -> Unit): CoroutineRequest<T> {
        this.onFail = block
        return this
    }

    /**
     * 设置取消回调（可选）
     */
    fun onCancel(block: () -> Unit): CoroutineRequest<T> {
        this.onCancel = block
        return this
    }

    /**
     * 启动请求（核心方法）
     * @return Job 用于手动取消
     */
    fun launch(): Job {
        return lifecycleScope.launch(Dispatchers.Main) {
            try {
                // 1. 切换到IO线程执行后台任务（doInBackground）
                val result = withContext(Dispatchers.IO) {
                    backgroundTask()
                }
                // 2. 主线程回调成功（Success）
                onSuccess?.invoke(result)
            } catch (e: Exception) {
                if (e is CancellationException) {
                    // 3. 取消回调（Cancel）
                    onCancel?.invoke()
                } else {
                    // 4. 主线程回调失败（Fail）
                    onFail?.invoke(e)
                }
            }
        }
    }

    /**
     * 构建者（用于创建CoroutineRequest实例）
     */
    companion object {
        /**
         * 创建请求实例
         * @param lifecycleScope 协程作用域（如Fragment/Activity的lifecycleScope）
         * @param backgroundTask 后台任务（suspend函数，在IO线程执行）
         */
        fun <T> create(
            lifecycleScope: LifecycleCoroutineScope,
            backgroundTask: suspend () -> T
        ): CoroutineRequest<T> {
            return CoroutineRequest(backgroundTask, lifecycleScope)
        }
    }
}

/**
 * Fragment/Activity扩展函数：简化请求创建
 * 直接在组件中调用，自动关联lifecycleScope
 */
fun <T> LifecycleCoroutineScope.request(
    backgroundTask: suspend () -> T
): CoroutineRequest<T> {
    return CoroutineRequest.create(this, backgroundTask)
}
