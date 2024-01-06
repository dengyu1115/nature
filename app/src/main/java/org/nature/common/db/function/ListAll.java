package org.nature.common.db.function;

import java.util.List;

@FunctionalInterface
public interface ListAll<T> {

    List<T> listAll();
}
