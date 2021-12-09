package com.zhongjh.common.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.IntRange
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2018/05/08
 * desc  : utils about thread
 * update: zhongjh 优化代码
 * update time: zhongjh 转化kotlin 2021/11/23
</pre> *
 * @author Blankj
 */
object ThreadUtils {

    val handler = Handler(Looper.getMainLooper())
    private val TYPE_PRIORITY_POOLS: MutableMap<Int, MutableMap<Int, ExecutorService?>> = HashMap()
    private val TASK_POOL_MAP: MutableMap<BaseTask<*>, ExecutorService?> = ConcurrentHashMap()
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()

    /**
     * 稳定的定时器任务
     */
    private val mExecutorService: ScheduledExecutorService = ScheduledThreadPoolExecutor(1, ThreadFactory { target: Runnable? -> Thread(target) })
    private const val TYPE_SINGLE: Int = -1
    private const val TYPE_CACHED: Int = -2
    private const val TYPE_IO: Int = -4
    private const val TYPE_CPU: Int = -8
    private var sDeliver: Executor? = null

    /**
     * 判断当前是否主线程
     */
    @JvmStatic
    fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    /**
     * 执行ui线程
     * @param runnable 事件
     */
    @JvmStatic
    fun runOnUiThread(runnable: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // 如果当前线程就是ui线程直接执行事件
            runnable.run()
        } else {
            handler.post(runnable)
        }
    }

    /**
     * 延时执行ui线程
     * @param runnable 事件
     * @param delayMillis 时间毫秒
     */
    @JvmStatic
    fun runOnUiThreadDelayed(runnable: Runnable, delayMillis: Long) {
        handler.postDelayed(runnable, delayMillis)
    }

    /**
     * 获取固定线程池
     * 返回一个可以复用并且有固定数量的线程池
     * 使用ThreadFactory创建新线程
     *
     * @param size 线程池的大小
     * @return 返回一个固定的线程池
     */
    @JvmStatic
    fun getFixedPool(@IntRange(from = 1) size: Int): ExecutorService? {
        return getPoolByTypeAndPriority(size)
    }

    /**
     * 获取固定线程池
     * 返回一个可以复用并且有固定数量的线程池
     * 使用ThreadFactory创建新线程
     *
     * @param size 线程池的大小
     * @param priority: 线程在轮询中的优先级。
     * @return 返回一个固定的线程池
     */
    @JvmStatic
    fun getFixedPool(@IntRange(from = 1) size: Int,
                     @IntRange(from = 1, to = 10) priority: Int): ExecutorService? {
        return getPoolByTypeAndPriority(size, priority)
    }

    /**
     * 返回使用单个工作线程操作的线程池
     * 关闭一个无界队列，并使用提供的ThreadFactory来需要时创建一个新线程。
     *
     * @return 单个线程池
     */
    val singlePool: ExecutorService?
        get() = getPoolByTypeAndPriority(TYPE_SINGLE)

    /**
     * 获取单线程池
     * 返回一个可以复用并且有固定数量的线程池
     * 使用ThreadFactory创建新线程
     *
     * @param priority: 线程在轮询中的优先级。
     * @return 单个线程池
     */
    @JvmStatic
    fun getSinglePool(@IntRange(from = 1, to = 10) priority: Int): ExecutorService? {
        return getPoolByTypeAndPriority(TYPE_SINGLE, priority)
    }

    /**
     * 返回一个缓存的线程池，如果没有将创建新的
     *
     * @return 缓存的线程池
     */
    val cachedPool: ExecutorService?
        get() = getPoolByTypeAndPriority(TYPE_CACHED)

    /**
     * 获取缓冲线程池
     * 返回一个缓存的线程池，如果没有将创建新的
     *
     * @param priority 线程在轮询中的优先级。
     * @return 缓存的线程池
     */
    @JvmStatic
    fun getCachedPool(@IntRange(from = 1, to = 10) priority: Int): ExecutorService? {
        return getPoolByTypeAndPriority(TYPE_CACHED, priority)
    }

    /**
     * 创建一个(2 * CPU_COUNT + 1)线程的线程池
     * 操作一个大小为128的队列.
     *
     * @return IO线程池
     */
    val ioPool: ExecutorService?
        get() = getPoolByTypeAndPriority(TYPE_IO)

    /**
     * 获取 IO 线程池
     * 创建一个(2 * CPU_COUNT + 1)线程的线程池
     * 操作一个大小为128的队列.
     *
     * @param priority 线程在轮询中的优先级。
     * @return IO线程池
     */
    @JvmStatic
    fun getIoPool(@IntRange(from = 1, to = 10) priority: Int): ExecutorService? {
        return getPoolByTypeAndPriority(TYPE_IO, priority)
    }

    /**
     * 返回创建(CPU_COUNT + 1)线程的线程池
     * 操作一个最大长度为128的队列
     * 线程数等于(2 * CPU_COUNT + 1)。
     *
     * @return 一个CPU线程池
     */
    val cpuPool: ExecutorService?
        get() = getPoolByTypeAndPriority(TYPE_CPU)

    /**
     * 获取 CPU 线程池
     * 返回创建(CPU_COUNT + 1)线程的线程池
     * 操作一个最大长度为128的队列
     * 线程数等于(2 * CPU_COUNT + 1)。
     *
     * @param priority 线程在轮询中的优先级。
     * @return 一个CPU线程池
     */
    @JvmStatic
    fun getCpuPool(@IntRange(from = 1, to = 10) priority: Int): ExecutorService? {
        return getPoolByTypeAndPriority(TYPE_CPU, priority)
    }

    /**
     * 在固定的线程池中执行给定的任务。
     *
     * @param size 固定线程池中线程的大小。
     * @param baseTask 要执行的任务。
     * @param T  任务结果的类型。
     */
    @JvmStatic
    fun <T> executeByFixed(@IntRange(from = 1) size: Int, baseTask: BaseTask<T>) {
        execute(getPoolByTypeAndPriority(size), baseTask)
    }

    /**
     * 在固定的线程池中执行给定的任务。
     *
     * @param size 固定线程池中线程的大小。
     * @param baseTask 要执行的任务。
     * @param priority 线程在轮询中的优先级。
     * @param T  任务结果的类型。
     */
    @JvmStatic
    fun <T> executeByFixed(@IntRange(from = 1) size: Int,
                           baseTask: BaseTask<T>,
                           @IntRange(from = 1, to = 10) priority: Int) {
        execute(getPoolByTypeAndPriority(size, priority), baseTask)
    }

    /**
     * 在固定线程池延时执行任务
     *
     * @param size  固定线程池中线程的大小。
     * @param baseTask  要执行的任务。
     * @param delay 从现在开始延迟执行的时间。
     * @param unit  延迟参数的时间单位。
     * @param T   任务结果的类型。
     */
    @JvmStatic
    fun <T> executeByFixedWithDelay(@IntRange(from = 1) size: Int,
                                    baseTask: BaseTask<T>,
                                    delay: Long,
                                    unit: TimeUnit) {
        executeWithDelay(getPoolByTypeAndPriority(size), baseTask, delay, unit)
    }

    /**
     * 在固定线程池延时执行任务
     *
     * @param size  固定线程池中线程的大小。
     * @param baseTask  要执行的任务。
     * @param delay 从现在开始延迟执行的时间。
     * @param unit  延迟参数的时间单位。
     * @param priority 线程在轮询中的优先级。
     * @param T   任务结果的类型。
     * */
    @JvmStatic
    fun <T> executeByFixedWithDelay(@IntRange(from = 1) size: Int,
                                    baseTask: BaseTask<T>,
                                    delay: Long,
                                    unit: TimeUnit,
                                    @IntRange(from = 1, to = 10) priority: Int) {
        executeWithDelay(getPoolByTypeAndPriority(size, priority), baseTask, delay, unit)
    }

    /**
     * 在固定线程池按固定频率执行任务
     *
     * @param size   The size of thread in the fixed thread pool.
     * @param baseTask   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param T    The type of the task's result.
     * */
    @JvmStatic
    fun <T> executeByFixedAtFixRate(@IntRange(from = 1) size: Int,
                                    baseTask: BaseTask<T>,
                                    period: Long,
                                    unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(size), baseTask, 0, period, unit)
    }

    /**
     * 在固定线程池按固定频率执行任务
     *
     * @param size     The size of thread in the fixed thread pool.
     * @param baseTask     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param T      The type of the task's result.
     */
    @JvmStatic
    fun <T> executeByFixedAtFixRate(@IntRange(from = 1) size: Int,
                                    baseTask: BaseTask<T>,
                                    period: Long,
                                    unit: TimeUnit,
                                    @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(getPoolByTypeAndPriority(size, priority), baseTask, 0, period, unit)
    }

    /**
     * 在单线程池执行任务
     *
     * @param baseTask The task to execute.
     * @param T  The type of the task's result.
     */
    @JvmStatic
    fun <T> executeBySingle(baseTask: BaseTask<T>) {
        execute(getPoolByTypeAndPriority(TYPE_SINGLE), baseTask)
    }

    /**
     * 在单线程池执行任务
     *
     * @param baseTask     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeBySingle(baseTask: BaseTask<T>,
                            @IntRange(from = 1, to = 10) priority: Int) {
        execute(getPoolByTypeAndPriority(TYPE_SINGLE, priority), baseTask)
    }

    /**
     * 在单线程池延时执行任务
     *
     * @param baseTask  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeBySingleWithDelay(baseTask: BaseTask<T>,
                                     delay: Long,
                                     unit: TimeUnit) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_SINGLE), baseTask, delay, unit)
    }

    /**
     * 在单线程池延时执行任务
     *
     * @param baseTask     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeBySingleWithDelay(baseTask: BaseTask<T>,
                                     delay: Long,
                                     unit: TimeUnit,
                                     @IntRange(from = 1, to = 10) priority: Int) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_SINGLE, priority), baseTask, delay, unit)
    }

    /**
     * 在单线程池按固定频率执行任务
     *
     * @param baseTask   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeBySingleAtFixRate(baseTask: BaseTask<T>,
                                     period: Long,
                                     unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_SINGLE), baseTask, 0, period, unit)
    }

    /**
     * 在单线程池按固定频率执行任务
     *
     * @param baseTask     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeBySingleAtFixRate(baseTask: BaseTask<T>,
                                     period: Long,
                                     unit: TimeUnit,
                                     @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_SINGLE.toInt(), priority), baseTask, 0, period, unit)
    }

    /**
     * Executes the given task in a single thread pool at fix rate.
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeBySingleAtFixRate(baseTask: BaseTask<T>,
                                     initialDelay: Long,
                                     period: Long,
                                     unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_SINGLE.toInt()), baseTask, initialDelay, period, unit)
    }

    /**
     * Executes the given task in a single thread pool at fix rate.
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeBySingleAtFixRate(baseTask: BaseTask<T>,
                                     initialDelay: Long,
                                     period: Long,
                                     unit: TimeUnit,
                                     @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(
                getPoolByTypeAndPriority(TYPE_SINGLE.toInt(), priority), baseTask, initialDelay, period, unit
        )
    }

    /**
     * 在缓冲线程池执行任务
     *
     * @param baseTask The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCached(baseTask: BaseTask<T>) {
        execute(getPoolByTypeAndPriority(TYPE_CACHED.toInt()), baseTask)
    }

    /**
     * 在缓冲线程池执行任务
     *
     * @param baseTask     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCached(baseTask: BaseTask<T>,
                            @IntRange(from = 1, to = 10) priority: Int) {
        execute(getPoolByTypeAndPriority(TYPE_CACHED.toInt(), priority), baseTask)
    }

    /**
     * 在缓冲线程池延时执行任务
     *
     * @param baseTask  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCachedWithDelay(baseTask: BaseTask<T>,
                                     delay: Long,
                                     unit: TimeUnit) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_CACHED.toInt()), baseTask, delay, unit)
    }

    /**
     * 在缓冲线程池延时执行任务
     *
     * @param baseTask     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCachedWithDelay(baseTask: BaseTask<T>,
                                     delay: Long,
                                     unit: TimeUnit,
                                     @IntRange(from = 1, to = 10) priority: Int) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_CACHED.toInt(), priority), baseTask, delay, unit)
    }

    /**
     * 在缓冲线程池按固定频率执行任务
     *
     * @param baseTask   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCachedAtFixRate(baseTask: BaseTask<T>,
                                     period: Long,
                                     unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_CACHED.toInt()), baseTask, 0, period, unit)
    }

    /**
     * 在缓冲线程池按固定频率执行任务
     *
     * @param baseTask     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCachedAtFixRate(baseTask: BaseTask<T>,
                                     period: Long,
                                     unit: TimeUnit,
                                     @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_CACHED.toInt(), priority), baseTask, 0, period, unit)
    }

    /**
     * 在缓冲线程池按固定频率执行任务
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCachedAtFixRate(baseTask: BaseTask<T>,
                                     initialDelay: Long,
                                     period: Long,
                                     unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_CACHED.toInt()), baseTask, initialDelay, period, unit)
    }

    /**
     * 在缓冲线程池按固定频率执行任务
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCachedAtFixRate(baseTask: BaseTask<T>,
                                     initialDelay: Long,
                                     period: Long,
                                     unit: TimeUnit,
                                     @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(
                getPoolByTypeAndPriority(TYPE_CACHED.toInt(), priority), baseTask, initialDelay, period, unit
        )
    }

    /**
     * 在 IO 线程池执行任务
     *
     * @param baseTask The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByIo(baseTask: BaseTask<T>) {
        execute(getPoolByTypeAndPriority(TYPE_IO.toInt()), baseTask)
    }

    /**
     * 在 IO 线程池执行任务
     *
     * @param baseTask     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByIo(baseTask: BaseTask<T>,
                        @IntRange(from = 1, to = 10) priority: Int) {
        execute(getPoolByTypeAndPriority(TYPE_IO.toInt(), priority), baseTask)
    }

    /**
     * 在 IO 线程池延时执行任务
     *
     * @param baseTask  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByIoWithDelay(baseTask: BaseTask<T>,
                                 delay: Long,
                                 unit: TimeUnit) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_IO.toInt()), baseTask, delay, unit)
    }

    /**
     * 在 IO 线程池延时执行任务
     *
     * @param baseTask     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByIoWithDelay(baseTask: BaseTask<T>,
                                 delay: Long,
                                 unit: TimeUnit,
                                 @IntRange(from = 1, to = 10) priority: Int) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_IO.toInt(), priority), baseTask, delay, unit)
    }

    /**
     * 在 IO 线程池按固定频率执行任务
     *
     * @param baseTask   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByIoAtFixRate(baseTask: BaseTask<T>,
                                 period: Long,
                                 unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_IO.toInt()), baseTask, 0, period, unit)
    }

    /**
     * 在 IO 线程池按固定频率执行任务
     *
     * @param baseTask     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByIoAtFixRate(baseTask: BaseTask<T>,
                                 period: Long,
                                 unit: TimeUnit,
                                 @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_IO.toInt(), priority), baseTask, 0, period, unit)
    }

    /**
     * 在 IO 线程池按固定频率执行任务
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByIoAtFixRate(baseTask: BaseTask<T>,
                                 initialDelay: Long,
                                 period: Long,
                                 unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_IO.toInt()), baseTask, initialDelay, period, unit)
    }

    /**
     * 在 IO 线程池按固定频率执行任务
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByIoAtFixRate(baseTask: BaseTask<T>,
                                 initialDelay: Long,
                                 period: Long,
                                 unit: TimeUnit,
                                 @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(
                getPoolByTypeAndPriority(TYPE_IO.toInt(), priority), baseTask, initialDelay, period, unit
        )
    }

    /**
     * 在 CPU 线程池执行任务
     *
     * @param baseTask The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCpu(baseTask: BaseTask<T>) {
        execute(getPoolByTypeAndPriority(TYPE_CPU.toInt()), baseTask)
    }

    /**
     * 在 CPU 线程池执行任务
     *
     * @param baseTask     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCpu(baseTask: BaseTask<T>,
                         @IntRange(from = 1, to = 10) priority: Int) {
        execute(getPoolByTypeAndPriority(TYPE_CPU.toInt(), priority), baseTask)
    }

    /**
     * 在 CPU 线程池延时执行任务
     *
     * @param baseTask  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCpuWithDelay(baseTask: BaseTask<T>,
                                  delay: Long,
                                  unit: TimeUnit) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_CPU.toInt()), baseTask, delay, unit)
    }

    /**
     * 在 CPU 线程池延时执行任务
     *
     * @param baseTask     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCpuWithDelay(baseTask: BaseTask<T>,
                                  delay: Long,
                                  unit: TimeUnit,
                                  @IntRange(from = 1, to = 10) priority: Int) {
        executeWithDelay(getPoolByTypeAndPriority(TYPE_CPU.toInt(), priority), baseTask, delay, unit)
    }

    /**
     * 在 CPU 线程池按固定频率执行任务
     *
     * @param baseTask   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCpuAtFixRate(baseTask: BaseTask<T>,
                                  period: Long,
                                  unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_CPU.toInt()), baseTask, 0, period, unit)
    }

    /**
     * 在 CPU 线程池按固定频率执行任务
     *
     * @param baseTask     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCpuAtFixRate(baseTask: BaseTask<T>,
                                  period: Long,
                                  unit: TimeUnit,
                                  @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_CPU.toInt(), priority), baseTask, 0, period, unit)
    }

    /**
     * Executes the given task in a cpu thread pool at fix rate.
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCpuAtFixRate(baseTask: BaseTask<T>,
                                  initialDelay: Long,
                                  period: Long,
                                  unit: TimeUnit) {
        executeAtFixedRate(getPoolByTypeAndPriority(TYPE_CPU.toInt()), baseTask, initialDelay, period, unit)
    }

    /**
     * Executes the given task in a cpu thread pool at fix rate.
     *
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCpuAtFixRate(baseTask: BaseTask<T>,
                                  initialDelay: Long,
                                  period: Long,
                                  unit: TimeUnit,
                                  @IntRange(from = 1, to = 10) priority: Int) {
        executeAtFixedRate(
                getPoolByTypeAndPriority(TYPE_CPU.toInt(), priority), baseTask, initialDelay, period, unit
        )
    }

    /**
     * 在自定义线程池执行任务
     *
     * @param pool The custom thread pool.
     * @param baseTask The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCustom(pool: ExecutorService?, baseTask: BaseTask<T>) {
        execute(pool, baseTask)
    }

    /**
     * 在自定义线程池延时执行任务
     *
     * @param pool  The custom thread pool.
     * @param baseTask  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCustomWithDelay(pool: ExecutorService?,
                                     baseTask: BaseTask<T>,
                                     delay: Long,
                                     unit: TimeUnit) {
        executeWithDelay(pool, baseTask, delay, unit)
    }

    /**
     * 在自定义线程池按固定频率执行任务
     *
     * @param pool   The custom thread pool.
     * @param baseTask   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCustomAtFixRate(pool: ExecutorService?,
                                     baseTask: BaseTask<T>,
                                     period: Long,
                                     unit: TimeUnit) {
        executeAtFixedRate(pool, baseTask, 0, period, unit)
    }

    /**
     * 在自定义线程池按固定频率执行任务
     *
     * @param pool         The custom thread pool.
     * @param baseTask         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    @JvmStatic
    fun <T> executeByCustomAtFixRate(pool: ExecutorService?,
                                     baseTask: BaseTask<T>,
                                     initialDelay: Long,
                                     period: Long,
                                     unit: TimeUnit) {
        executeAtFixedRate(pool, baseTask, initialDelay, period, unit)
    }


    /**
     * 取消任务的执行
     *
     * @param baseTask The task to cancel.
     */
    @JvmStatic
    fun cancel(baseTask: BaseTask<*>?) {
        if (baseTask == null) {
            return
        }
        baseTask.cancel()
    }

    /**
     * 取消任务的执行
     *
     * @param baseTasks The tasks to cancel.
     */
    @JvmStatic
    fun cancel(vararg baseTasks: BaseTask<*>?) {
        if (baseTasks.isEmpty()) {
            return
        }
        for (baseTask in baseTasks) {
            if (baseTask == null) {
                continue
            }
            baseTask.cancel()
        }
    }

    /**
     * 取消任务的执行
     *
     * @param baseTasks The tasks to cancel.
     */
    @JvmStatic
    fun cancel(baseTasks: List<BaseTask<*>?>?) {
        if (baseTasks == null || baseTasks.size == 0) {
            return
        }
        for (baseTask in baseTasks) {
            if (baseTask == null) {
                continue
            }
            baseTask.cancel()
        }
    }

    /**
     * 取消任务的执行
     *
     * @param executorService The pool.
     */
    @JvmStatic
    fun cancel(executorService: ExecutorService) {
        if (executorService is ThreadPoolExecutor4Util) {
            for ((key, value) in TASK_POOL_MAP) {
                if (value === executorService) {
                    cancel(key)
                }
            }
        } else {
            Log.e("ThreadUtils", "The executorService is not ThreadUtils's pool.")
        }
    }

    /**
     * Set the deliver.
     *
     * @param deliver The deliver.
     */
    fun setDeliver(deliver: Executor?) {
        sDeliver = deliver
    }

    private fun <T> execute(pool: ExecutorService?, baseTask: BaseTask<T>) {
        execute(pool, baseTask, 0, 0, null)
    }

    private fun <T> executeWithDelay(pool: ExecutorService?,
                                     baseTask: BaseTask<T>,
                                     delay: Long,
                                     unit: TimeUnit) {
        execute(pool, baseTask, delay, 0, unit)
    }

    private fun <T> executeAtFixedRate(pool: ExecutorService?,
                                       baseTask: BaseTask<T>,
                                       delay: Long,
                                       period: Long,
                                       unit: TimeUnit) {
        execute(pool, baseTask, delay, period, unit)
    }

    /**
     *
     * @param pool ExecutorService 线程池
     * @param delay 延时
     * @param period 间隔时间
     * @param unit 时间单位
     *
     */
    private fun <T> execute(pool: ExecutorService?, baseTask: BaseTask<T>,
                            delay: Long, period: Long, unit: TimeUnit?) {
        synchronized(TASK_POOL_MAP) {
            if (TASK_POOL_MAP[baseTask] != null) {
                Log.e("ThreadUtils", "Task can only be executed once.")
                return
            }
            TASK_POOL_MAP.put(baseTask, pool)
        }
        // 如果间隔时间=0
        if (period == 0L) {
            // 如果延时=0
            if (delay == 0L) {
                // 直接执行
                pool!!.execute(baseTask)
            } else {
                // 使用mExecutorService执行延时的任务
                val timerTask: TimerTask = object : TimerTask() {
                    override fun run() {
                        pool!!.execute(baseTask)
                    }
                }
                mExecutorService.schedule(timerTask, unit!!.toMillis(delay), TimeUnit.MILLISECONDS)
            }
        } else {
            // 设置循环定时任务
            baseTask.setSchedule(true)
            val timerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    pool!!.execute(baseTask)
                }
            }
            // 使用mExecutorService执行延时的任务
            mExecutorService.scheduleAtFixedRate(timerTask, unit!!.toMillis(delay), unit.toMillis(period), TimeUnit.MILLISECONDS)
        }
    }

    private fun getPoolByTypeAndPriority(type: Int): ExecutorService {
        return getPoolByTypeAndPriority(type, Thread.NORM_PRIORITY)
    }

    private fun getPoolByTypeAndPriority(type: Int, priority: Int): ExecutorService {
        // 同步锁
        synchronized(TYPE_PRIORITY_POOLS) {
            val pool: ExecutorService
            var priorityPools = TYPE_PRIORITY_POOLS[type]
            if (priorityPools == null) {
                // 创建 ConcurrentHashMap
                priorityPools = ConcurrentHashMap()
                pool = ThreadPoolExecutor4Util.createPool(type, priority)
                priorityPools[priority] = pool
                TYPE_PRIORITY_POOLS[type] = priorityPools
            } else {
                if (priorityPools[priority] == null) {
                    pool = ThreadPoolExecutor4Util.createPool(type, priority)
                    priorityPools[priority] = pool
                } else {
                    pool = priorityPools[priority]!!
                }
            }
            return pool
        }
    }

    private val globalDeliver: Executor?
        get() {
            if (sDeliver == null) {
                sDeliver = Executor { command -> runOnUiThread(command) }
            }
            return sDeliver
        }

    /**
     * 创建线程池
     */
    internal class ThreadPoolExecutor4Util private constructor(corePoolSize: Int, maximumPoolSize: Int,
                                                               keepAliveTime: Long, unit: TimeUnit?,
                                                               workQueue: LinkedBlockingQueue4Util,
                                                               threadFactory: ThreadFactory?) : ThreadPoolExecutor(corePoolSize, maximumPoolSize,
            keepAliveTime, unit,
            workQueue,
            threadFactory
    ) {
        /**
         * 原子操作Integer类
         */
        private val mSubmittedCount = AtomicInteger()
        private val mWorkQueue: LinkedBlockingQueue4Util

        init {
            workQueue.mPool = this
            mWorkQueue = workQueue
        }

        /**
         * 返回的是 原值 - 1
         */
        override fun afterExecute(r: Runnable, t: Throwable) {
            mSubmittedCount.decrementAndGet()
            super.afterExecute(r, t)
        }

        /**
         * 执行
         */
        override fun execute(command: Runnable) {
            // 如果被中断就直接返回
            if (this.isShutdown) {
                return
            }
            // 原值 + 1
            mSubmittedCount.incrementAndGet()
            try {
                super.execute(command)
            } catch (ignore: RejectedExecutionException) {
                Log.e("ThreadUtils", "This will not happen!")
                // 如果出现RejectedExecutionException异常，就把command插入到队列的尾部。但是这个异常不会出现，因为要么调用shutdown()，或者线程池的线程数量已经达到了maximumPoolSize的时候
                mWorkQueue.offer(command)
            } catch (t: Throwable) {
                // 别的异常 原值 - 1
                mSubmittedCount.decrementAndGet()
            }
        }

        companion object {
            fun createPool(type: Int, priority: Int): ExecutorService {
                return when (type) {
                    TYPE_SINGLE -> {
                        ThreadPoolExecutor4Util(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                LinkedBlockingQueue4Util(),
                                UtilsThreadFactory("single", priority)
                        )
                    }
                    TYPE_CACHED -> {
                        ThreadPoolExecutor4Util(0, 128,
                                60L, TimeUnit.SECONDS,
                                LinkedBlockingQueue4Util(true),
                                UtilsThreadFactory("cached", priority)
                        )
                    }
                    TYPE_IO -> {
                        ThreadPoolExecutor4Util(2 * CPU_COUNT + 1, 2 * CPU_COUNT + 1,
                                30, TimeUnit.SECONDS,
                                LinkedBlockingQueue4Util(),
                                UtilsThreadFactory("io", priority)
                        )
                    }
                    TYPE_CPU -> {
                        ThreadPoolExecutor4Util(CPU_COUNT + 1, 2 * CPU_COUNT + 1,
                                30, TimeUnit.SECONDS,
                                LinkedBlockingQueue4Util(true),
                                UtilsThreadFactory("cpu", priority)
                        )
                    }
                    else -> ThreadPoolExecutor4Util(type, type,
                            0L, TimeUnit.MILLISECONDS,
                            LinkedBlockingQueue4Util(),
                            UtilsThreadFactory("fixed($type)", priority)
                    )
                }
            }
        }
    }

    /**
     * LinkedBlockingQueue是一个单向链表实现的阻塞队列,先进先出的顺序
     */
    private class LinkedBlockingQueue4Util : LinkedBlockingQueue<Runnable> {

        /**
         * 使用
         */
        @Volatile
        var mPool: ThreadPoolExecutor4Util? = null
        private var mCapacity = Int.MAX_VALUE

        internal constructor() : super()

        internal constructor(isAddSubThreadFirstThenAddQueue: Boolean) : super() {
            if (isAddSubThreadFirstThenAddQueue) {
                mCapacity = 0
            }
        }

        internal constructor(capacity: Int) : super() {
            mCapacity = capacity
        }

        override fun offer(runnable: Runnable?): Boolean {
            if (mCapacity <= size &&
                    mPool != null && mPool!!.poolSize < mPool!!.maximumPoolSize) {
                // create a non-core thread
                return false
            }
            return super.offer(runnable)
        }
    }

    /**
     * 线程工厂
     * @param prefix: 前缀，类型
     * @param priority: 线程在轮询中的优先级。
     */
    internal class UtilsThreadFactory @JvmOverloads constructor(prefix: String, priority: Int, isDaemon: Boolean = false) : ThreadFactory {
        private val namePrefix: String
        private val priority: Int
        private val isDaemon: Boolean

        /**
         * 原子类计算
         */
        private val atomicState = AtomicLong()

        init {
            namePrefix = prefix + "-pool-" +
                    POOL_NUMBER.getAndIncrement() +
                    "-thread-"
            this.priority = priority
            this.isDaemon = isDaemon
        }

        companion object {
            private val POOL_NUMBER = AtomicInteger(1)
            private const val serialVersionUID = -9209200509960368598L
        }

        /**
         * 创建一个线程
         */
        override fun newThread(r: Runnable?): Thread {
            val t: Thread = object : Thread(r, namePrefix + atomicState.andIncrement) {
                override fun run() {
                    try {
                        super.run()
                    } catch (t: Throwable) {
                        Log.e("ThreadUtils", "Request threw uncaught throwable", t)
                    }
                }
            }
            t.isDaemon = isDaemon
            t.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e -> println(e) }
            t.priority = priority
            return t
        }
    }

    abstract class BaseSimpleBaseTask<T> : BaseTask<T>() {
        override fun onCancel() {
            Log.e("ThreadUtils", "onCancel: " + Thread.currentThread())
        }

        override fun onFail(t: Throwable?) {
            Log.e("ThreadUtils", "onFail: ", t)
        }
    }

    /**
     * 抽象基类BaseTask，继承于Runnable
     */
    abstract class BaseTask<T> : Runnable {
        companion object {
            private const val NEW = 0
            private const val RUNNING = 1
            private const val EXCEPTIONAL = 2
            private const val COMPLETING = 3
            private const val CANCELLED = 4
            private const val INTERRUPTED = 5
            private const val TIMEOUT = 6
        }

        private val state = AtomicInteger(NEW)

        /**
         * 是否定期任务
         */
        @Volatile
        private var isSchedule = false

        /**
         * 该任务的线程
         */
        @Volatile
        private var runner: Thread? = null

        /**
         * 一个定时任务，用于服务超时，如果一定时间超时，则启动该定时任务
         */
        private var mExecutorService: ScheduledExecutorService? = null

        /**
         * 超时时间
         */
        private var mTimeoutMillis: Long = 0

        /**
         * 超时事件
         */
        private var mTimeoutListener: OnTimeoutListener? = null

        /**
         * 通过该框架来控制线程的启动,执行,关闭,简化并发编程
         */
        private var deliver: Executor? = null

        val isCanceled: Boolean
            get() = state.get() >= CANCELLED

        val isDone: Boolean
            get() = state.get() > RUNNING

        /**
         * 线程方法
         * @return 实体
         * @throws Throwable 异常
         */
        @Throws(Throwable::class)
        abstract fun doInBackground(): T

        /**
         * 成功
         * @param result 实体
         */
        abstract fun onSuccess(result: T)

        /**
         * 取消
         */
        abstract fun onCancel()

        /**
         * 失败
         * @param t 异常
         */
        abstract fun onFail(t: Throwable?)

        override fun run() {
            // 如果是个循环任务,就意味着每次定时循环的时候，第二次会用到旧的任务
            if (isSchedule) {
                // 判断线程是否为null
                if (runner == null) {
                    // 如果当前状态是 NEW(新线程) 并且改成 RUNNING(线程运行中)，就返回True,否则False
                    if (!state.compareAndSet(NEW, RUNNING)) {
                        // 当前状态不是NEW，直接返回
                        return
                    }
                    // 创建线程
                    runner = Thread.currentThread()
                    if (mTimeoutListener != null) {
                        // 计划任务不支持超时。
                        Log.w("ThreadUtils", "Scheduled task doesn't support timeout.")
                    }
                } else {
                    if (state.get() != RUNNING) {
                        // 如果当前状态不是 RUNNING(线程运行中) 直接返回
                        return
                    }
                }
            } else {
                // 如果当前状态是 NEW(新线程) 并且改成 RUNNING(线程运行中)，就返回True,否则False
                if (!state.compareAndSet(NEW, RUNNING)) {
                    // 当前状态不是NEW，直接返回
                    return
                }
                // 创建线程
                runner = Thread.currentThread()
                if (mTimeoutListener != null) {
                    mExecutorService = ScheduledThreadPoolExecutor(1, ThreadFactory { target: Runnable? -> Thread(target) })
                    (mExecutorService as ScheduledThreadPoolExecutor).schedule(object : TimerTask() {
                        override fun run() {
                            if (!isDone && mTimeoutListener != null) {
                                // 如果时间结束还没完成就是超时了
                                timeout()
                            }
                        }
                    }, mTimeoutMillis, TimeUnit.MILLISECONDS)
                }
            }
            try {
                val result = doInBackground()
                if (isSchedule) {
                    if (state.get() != RUNNING) {
                        return
                    }
                } else {
                    // 如果不是定期任务,就将RUNNING状态改成COMPLETING
                    if (!state.compareAndSet(RUNNING, COMPLETING)) {
                        return
                    }
                    getDeliver()!!.execute {
                        onSuccess(result)
                        onDone()
                    }
                }
            } catch (ignore: InterruptedException) {
                state.compareAndSet(CANCELLED, INTERRUPTED)
            } catch (throwable: Throwable) {
                if (!state.compareAndSet(RUNNING, EXCEPTIONAL)) {
                    return
                }
                getDeliver()!!.execute {
                    onFail(throwable)
                    onDone()
                }
            }
        }

        @JvmOverloads
        fun cancel(mayInterruptIfRunning: Boolean = true) {
            synchronized(state) {
                if (state.get() > RUNNING) {
                    return
                }
                state.set(CANCELLED)
            }
            if (mayInterruptIfRunning) {
                if (runner != null) {
                    runner!!.interrupt()
                }
            }
            getDeliver()!!.execute {
                onCancel()
                onDone()
            }
        }

        private fun timeout() {
            synchronized(state) {
                // 如果步骤已经执行到RUNNING后面了就直接返回
                if (state.get() > RUNNING) {
                    return
                }
                state.set(TIMEOUT)
            }
            if (runner != null) {
                runner!!.interrupt()
            }
        }

        /**
         * 设置是否定期执行
         */
        fun setSchedule(isSchedule: Boolean) {
            this.isSchedule = isSchedule
        }

        private fun getDeliver(): Executor? {
            return if (deliver == null) {
                globalDeliver
            } else {
                deliver
            }
        }

        @CallSuper
        protected fun onDone() {
            TASK_POOL_MAP.remove(this)
            if (mExecutorService != null) {
                mExecutorService!!.shutdownNow()
                mExecutorService = null
                mTimeoutListener = null
            }
        }

        interface OnTimeoutListener {
            /**
             * 超时
             */
            fun onTimeout()
        }

        override fun hashCode(): Int {
            return super.hashCode()
        }

        override fun equals(obj: Any?): Boolean {
            return super.equals(obj)
        }

    }
}