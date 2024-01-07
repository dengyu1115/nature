package org.nature.common.db.function;

import java.util.List;

/**
 * 根据id批量删除
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@FunctionalInterface
public interface DeleteByIds<T> {

    /**
     * 根据id批量删除
     * @param ids ID集合
     * @return int
     */
    int deleteByIds(List<T> ids);
}
