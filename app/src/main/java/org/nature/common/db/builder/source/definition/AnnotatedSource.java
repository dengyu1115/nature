package org.nature.common.db.builder.source.definition;


import java.lang.reflect.Method;

public interface AnnotatedSource {

    Object execute(Class<?> cls, String where, Method method, Object... args);

}
