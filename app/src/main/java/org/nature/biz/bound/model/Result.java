package org.nature.biz.bound.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.model.BaseModel;

import java.math.BigDecimal;

/**
 * 计算结果
 * @author Nature
 * @version 1.0.0
 * @since 2024/6/4
 */
@Getter
@Setter
public class Result extends BaseModel {
    /**
     * 规则
     */
    private String rule;
    /**
     * 编号
     */
    private String code;
    /**
     * 类型
     */
    private String type;
    /**
     * 名称
     */
    private String name;
    /**
     * 涨幅
     */
    private BigDecimal ratio;
}
