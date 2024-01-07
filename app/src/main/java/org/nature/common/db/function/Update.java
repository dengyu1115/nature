package org.nature.common.db.function;

/**
 * 更新操作
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@FunctionalInterface
public interface Update<T> {

    /**
     * 更新操作
     * @param datum 数据
     * @return int
     */
    int update(T datum);
}
