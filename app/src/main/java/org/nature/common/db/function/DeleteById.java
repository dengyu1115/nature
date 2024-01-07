package org.nature.common.db.function;

/**
 * 根据id删除
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@FunctionalInterface
public interface DeleteById<T> {

    /**
     * 根据id删除
     * @param id id
     * @return int
     */
    int deleteById(T id);
}
