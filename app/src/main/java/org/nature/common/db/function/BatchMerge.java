package org.nature.common.db.function;

import java.util.List;

@FunctionalInterface
public interface BatchMerge<T> {

    int batchMerge(List<T> list);
}
