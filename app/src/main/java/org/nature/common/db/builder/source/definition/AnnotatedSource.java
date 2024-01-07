package org.nature.common.db.builder.source.definition;


import java.lang.reflect.Method;

/**
 * 注解标注的source
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public interface AnnotatedSource {

    /**
     * 执行方法
     * @param cls    类
     * @param where  where条件
     * @param method 方法
     * @param args   参数
     * @return Object
     */
    Object execute(Class<?> cls, String where, Method method, Object... args);

}
