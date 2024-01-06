package org.nature.common.db.function;

@FunctionalInterface
public interface FindById<T, I> {

    T findById(I id);
}
