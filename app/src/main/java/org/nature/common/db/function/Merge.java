package org.nature.common.db.function;

@FunctionalInterface
public interface Merge<T> {

    int merge(T datum);
}
