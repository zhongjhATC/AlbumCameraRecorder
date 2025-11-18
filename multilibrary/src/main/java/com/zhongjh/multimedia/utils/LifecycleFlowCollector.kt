package com.zhongjh.multimedia.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * 生命周期感知的 Flow 收集工具类
 * 简化 StateFlow/LiveData 收集逻辑，避免重复编写 `launch { repeatOnLifecycle { ... } }` 模板代码
 */
object LifecycleFlowCollector {

    /**
     * 收集 Flow 数据（默认在 STARTED 状态下激活）
     * @param lifecycleOwner 生命周期持有者（如 Fragment/Activity）
     * @param flow 待收集的数据流（如 StateFlow）
     * @param minActiveState 最小活跃生命周期状态（默认 STARTED，避免后台耗电）
     * @param action 数据处理回调（在主线程执行）
     */
    fun <T> collect(lifecycleOwner: LifecycleOwner, flow: Flow<T>, minActiveState: Lifecycle.State = Lifecycle.State.STARTED, action: (T) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(minActiveState) {
                flow.collect { action(it) }
            }
        }
    }

}