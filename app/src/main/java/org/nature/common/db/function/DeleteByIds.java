package org.nature.common.db.function;

import java.util.List;

@FunctionalInterface
public interface DeleteByIds<T> {

    int deleteByIds(List<T> ids);
}
