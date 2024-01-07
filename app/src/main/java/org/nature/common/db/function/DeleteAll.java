package org.nature.common.db.function;

/**
 * 删除所有数据
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@FunctionalInterface
public interface DeleteAll {

    /**
     * 删除所有数据
     * @return int
     */
    int deleteAll();
}
