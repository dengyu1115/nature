package org.nature.util

import java.util.LinkedList
import java.util.concurrent.*
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors

/**
 * 远程执行工具类
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
object ExecUtil {

    private const val SIZE_CORE = 32
    private const val SIZE_MAX = 64
    private const val ALIVE_TIME = 1

    private val EXECUTOR = ThreadPoolExecutor(
        SIZE_CORE, SIZE_MAX, ALIVE_TIME.toLong(),
        TimeUnit.SECONDS, LinkedBlockingDeque()
    )

    /**
     * 执行
     * @param item 数据获取逻辑
     * @param run  执行逻辑
     * @return 执行后结果集
     */
    @JvmStatic
    fun <I, O> single(item: Supplier<I>, run: Function<I, O>): O? {
        // 获取数据集合
        val i = item.get()
        // 任务结果获取集合
        val future = EXECUTOR.submit(Callable { doExec(run, i, 0) })
        // 结果获取，执行无结果返回null
        return try {
            future.get()
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 执行
     * @param call 执行逻辑
     * @return 执行后结果集
     */
    @JvmStatic
    fun <O> single(call: Callable<O>): O {
        // 任务结果获取集合
        val future = EXECUTOR.submit(call)
        // 结果获取，执行无结果返回null
        return try {
            future.get()
        } catch (e: ExecutionException) {
            throw RuntimeException(e)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    /**
     * 执行
     * @param list 数据集合获取逻辑
     * @param run  执行逻辑
     * @return 执行后结果集
     */
    @JvmStatic
    fun <I, O> batch(list: Supplier<List<I>>, run: Function<I, O>): List<O?> {
        // 获取数据集合
        val items = list.get()
        // 任务结果获取集合
        val cl: MutableList<Future<O?>> = LinkedList()
        // 提交任务
        items.forEach { i ->
            cl.add(EXECUTOR.submit(Callable { doExec(run, i, 0) }))
        }
        // 结果获取，执行无结果返回null
        return cl.stream().map { i ->
            try {
                i.get()
            } catch (e: Exception) {
                null
            }
        }.collect(Collectors.toList())
    }

    /**
     * 提交任务
     * @param callable 执行逻辑
     * @return Future
     */
    @JvmStatic
    fun <O> submit(callable: Callable<O>): Future<O> {
        return EXECUTOR.submit(callable)
    }

    /**
     * 提交任务
     * @param runnable 执行逻辑
     */
    @JvmStatic
    fun submit(runnable: Runnable) {
        EXECUTOR.submit(runnable)
    }

    /**
     * 执行处理
     * @param run   执行逻辑
     * @param i     待处理数据
     * @param count 已执行次数
     * @return 结果数据
     */
    private fun <I, O> doExec(run: Function<I, O>, i: I, count: Int): O? {
        var counted = count
        if (counted++ == 3) {
            return null   // 失败可重试2次
        }
        return try {
            run.apply(i)
        } catch (e: Exception) {    // ignore
            e.printStackTrace()
            doExec(run, i, counted)
        }
    }
}
