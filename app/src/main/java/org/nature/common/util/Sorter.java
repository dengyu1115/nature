package org.nature.common.util;

import java.util.Comparator;
import java.util.function.Function;

/**
 * 排序器
 * @author nature
 * @version 1.0.0
 * @since 2020/7/25 21:53
 */
public class Sorter {

    /**
     * 创建一个比较器，将null值排在最后
     * @param keyExtractor 从对象中提取比较键的函数
     * @param <T>          被比较的对象类型
     * @param <U>          可比较的键类型
     * @return Comparator<T> 比较器
     */
    public static <T, U extends Comparable<? super U>> Comparator<T> nullsLast(
            Function<? super T, ? extends U> keyExtractor) {
        return Comparator.comparing(keyExtractor, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    /**
     * 创建一个比较器，将null值排在最前
     * @param keyExtractor 从对象中提取比较键的函数
     * @param <T>          被比较的对象类型
     * @param <U>          可比较的键类型
     * @return Comparator<T> 比较器
     */
    public static <T, U extends Comparable<? super U>> Comparator<T> nullsFirst(
            Function<? super T, ? extends U> keyExtractor) {
        return Comparator.comparing(keyExtractor, Comparator.nullsFirst(Comparator.naturalOrder()));
    }
}
