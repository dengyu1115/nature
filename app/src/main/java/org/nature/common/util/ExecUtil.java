package org.nature.common.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 远程执行工具类
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public class ExecUtil {

    private static final int SIZE_CORE = 32, SIZE_MAX = 64, ALIVE_TIME = 1;

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(SIZE_CORE, SIZE_MAX, ALIVE_TIME,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    /**
     * 执行
     * @param item 数据获取逻辑
     * @param run  执行逻辑
     * @return 执行后结果集
     */
    public static <I, O> O single(Supplier<I> item, Function<I, O> run) {
        // 获取数据集合
        I i = item.get();
        // 任务结果获取集合
        Future<O> future = EXECUTOR.submit(() -> doExec(run, i, 0));
        // 结果获取，执行无结果返回null
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行
     * @param call 执行逻辑
     * @return 执行后结果集
     */
    public static <O> O single(Callable<O> call) {
        // 任务结果获取集合
        Future<O> future = EXECUTOR.submit(call);
        // 结果获取，执行无结果返回null
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行
     * @param list 数据集合获取逻辑
     * @param run  执行逻辑
     * @return 执行后结果集
     */
    public static <I, O> List<O> batch(Supplier<List<I>> list, Function<I, O> run) {
        // 获取数据集合
        List<I> items = list.get();
        // 任务结果获取集合
        List<Future<O>> cl = new LinkedList<>();
        // 提交任务
        items.forEach(i -> {
            cl.add(EXECUTOR.submit(() -> doExec(run, i, 0)));
        });
        // 结果获取，执行无结果返回null
        return cl.stream().map(i -> {
            try {
                return i.get();
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 提交任务
     * @param callable 执行逻辑
     * @return Future
     */
    public static <O> Future<O> submit(Callable<O> callable) {
        return EXECUTOR.submit(callable);
    }

    /**
     * 提交任务
     * @param runnable 执行逻辑
     */
    public static void submit(Runnable runnable) {
        EXECUTOR.submit(runnable);
    }

    /**
     * 执行处理
     * @param run   执行逻辑
     * @param i     待处理数据
     * @param count 已执行次数
     * @return 结果数据
     */
    private static <I, O> O doExec(Function<I, O> run, I i, int count) {
        if (count++ == 3) {
            return null;   // 失败可重试2次
        }
        int counted = count;
        try {
            return run.apply(i);
        } catch (Exception e) {    // ignore
            e.printStackTrace();
            return doExec(run, i, counted);
        }
    }
}
