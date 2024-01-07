package org.nature.common.db.function;

/**
 * 并入
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@FunctionalInterface
public interface Merge<T> {

    /**
     * 并入
     * @param datum 数据
     * @return int
     */
    int merge(T datum);
}
