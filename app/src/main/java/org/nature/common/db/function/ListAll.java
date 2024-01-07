package org.nature.common.db.function;

import java.util.List;

/**
 * 查询所有
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@FunctionalInterface
public interface ListAll<T> {

    /**
     * 查询所有
     * @return list
     */
    List<T> listAll();
}
