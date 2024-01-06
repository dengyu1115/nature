package org.nature.common.db.function;

import java.util.List;

@FunctionalInterface
public interface BatchSave<T> {

    int batchSave(List<T> list);
}
