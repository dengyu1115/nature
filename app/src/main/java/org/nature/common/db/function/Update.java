package org.nature.common.db.function;

@FunctionalInterface
public interface Update<T> {

    int update(T datum);
}
