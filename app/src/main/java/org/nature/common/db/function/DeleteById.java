package org.nature.common.db.function;

@FunctionalInterface
public interface DeleteById<T> {

    int deleteById(T id);
}
