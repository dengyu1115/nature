package org.nature.common.db.builder.source.definition;

/**
 * 功能接口类source
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public interface FunctionalSource {

    /**
     * 执行方法
     * @param cls  类
     * @param args 参数
     * @return Object
     */
    Object execute(Class<?> cls, Object... args);

}
