package org.nature.biz.bound.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.model.BaseModel;

import java.math.BigDecimal;

/**
 * 债券涨幅
 * @author Nature
 * @version 1.0.0
 * @since 2024/3/18
 */
@Getter
@Setter
public class Rate extends BaseModel {
    /**
     * 规则code
     */
    private String ruleCode;
    /**
     * 规则名称
     */
    private String ruleName;
    /**
     * 比对方code
     */
    private String code1;
    /**
     * 比对方name
     */
    private String name1;
    /**
     * 比对方价格
     */
    private BigDecimal price1;
    /**
     * 对比方code
     */
    private String code2;
    /**
     * 对比方name
     */
    private String name2;
    /**
     * 对比方价格
     */
    private BigDecimal price2;
    /**
     * 涨幅
     */
    private BigDecimal ratio;

}
