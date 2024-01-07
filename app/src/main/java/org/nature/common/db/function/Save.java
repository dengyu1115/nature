package org.nature.common.db.function;

/**
 * 保存数据
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@FunctionalInterface
public interface Save<T> {

    /**
     * 保存数据
     * @param datum 数据
     * @return int
     */
    int save(T datum);
}
