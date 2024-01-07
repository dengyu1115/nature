package org.nature.common.db.function;

import java.util.List;

/**
 * 批量保存
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@FunctionalInterface
public interface BatchSave<T> {

    /**
     * 批量保存
     * @param list 数据集
     * @return int
     */
    int batchSave(List<T> list);
}
