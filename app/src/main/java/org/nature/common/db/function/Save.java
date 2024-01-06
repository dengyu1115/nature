package org.nature.common.db.function;

@FunctionalInterface
public interface Save<T> {

    int save(T datum);
}
