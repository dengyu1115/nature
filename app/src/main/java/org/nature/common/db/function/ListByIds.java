package org.nature.common.db.function;

import java.util.List;

@FunctionalInterface
public interface ListByIds<T, I> {

    List<T> findByIds(List<I> ids);
}
