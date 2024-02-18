package org.nature.common.db.function;

import java.util.List;

/**
 * 通过ID集合查询
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@FunctionalInterface
public interface ListByIds<T, I> {

    /**
     * 通过ID集合查询
     * @param ids ID集合
     * @return list
     */
    List<T> listByIds(List<I> ids);
}
