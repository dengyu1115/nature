package org.nature.common.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RemoteExeUtil {

    private static final int SIZE_CORE = 32, SIZE_MAX = 64, ALIVE_TIME = 1;

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(SIZE_CORE, SIZE_MAX, ALIVE_TIME,
            TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    public static <I, O> List<O> exec(Supplier<List<I>> list, Function<I, O> run) {
        List<I> items = list.get();
        List<Future<O>> cl = new LinkedList<>();
        items.forEach(i -> {
            cl.add(EXECUTOR.submit(() -> doExec(run, i, 0)));
        });
        return cl.stream().map(i -> {
            try {
                return i.get();
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static <O> Future<O> submit(Callable<O> callable) {
        return EXECUTOR.submit(callable);
    }

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
