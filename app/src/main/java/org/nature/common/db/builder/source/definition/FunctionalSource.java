package org.nature.common.db.builder.source.definition;


public interface FunctionalSource {

    Object execute(Class<?> cls, Object... args);

}
