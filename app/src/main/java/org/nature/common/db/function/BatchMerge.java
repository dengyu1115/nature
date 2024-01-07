package org.nature.common.db.function;

import java.util.List;

/**
 * 批量并入
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@FunctionalInterface
public interface BatchMerge<T> {

    /**
     * 批量并入
     * @param list 数据集
     * @return int
     */
    int batchMerge(List<T> list);
}
