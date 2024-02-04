package org.nature.common.util;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

/**
 * 多线程计数器
 * @author nature
 * @version 1.0.0
 * @since 2019/12/8 10:58
 */
public class Counter {

    @SuppressLint("UseSparseArrays")
    private final Map<Long, Integer> map = new HashMap<>();

    /**
     * 计数
     * @param inc 增量值
     */
    public void count(int inc) {
        long id = Thread.currentThread().getId();
        Integer count = map.get(id);
        if (count == null) {
            synchronized (map) {
                if ((count = map.get(id)) == null) {
                    map.put(id, inc);
                } else {
                    map.put(id, count + inc);
                }
            }
        } else map.put(id, count + inc);
    }

    /**
     * 获取计数结果
     * @return int
     */
    public int get() {
        int total = 0;
        for (Integer i : map.values()) {
            total += i;
        }
        return total;
    }
}
