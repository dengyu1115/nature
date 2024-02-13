package org.nature.common.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

/**
 * 排序器
 * @author nature
 * @version 1.0.0
 * @since 2020/7/25 21:53
 */
public class Sorter {

    public static <T, U extends Comparable<? super U>> Comparator<T> nullsLast(
            Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (Comparator<T> & Serializable)
                (c1, c2) -> {
                    U u1 = keyExtractor.apply(c1), u2 = keyExtractor.apply(c2);
                    if (u1 != null && u2 != null) {
                        return u1.compareTo(u2);
                    }
                    if (u1 == null && u2 != null) {
                        return -1;
                    }
                    if (u1 != null) {
                        return 1;
                    }
                    return 0;
                };
    }
}
