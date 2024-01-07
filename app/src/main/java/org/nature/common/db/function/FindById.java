package org.nature.common.db.function;

/**
 * 通过id查找对象
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@FunctionalInterface
public interface FindById<T, I> {

    /**
     * 通过id查找对象
     * @param id id
     * @return T
     */
    T findById(I id);
}
