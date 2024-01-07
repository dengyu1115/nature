package org.nature.common.db.builder.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 映射
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@Getter
@AllArgsConstructor
public class Mapping {
    /**
     * java对象字段
     */
    private String property;
    /**
     * 数据库字段
     */
    private String column;
    /**
     * 字段类型
     */
    private Class<?> type;
    /**
     * 主键序号
     */
    private Integer idOrder;
}
