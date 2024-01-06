package org.nature.biz.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

import java.math.BigDecimal;

/**
 * 策略规则
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@Model(db = "nature/biz.db", table = "rule")
@Getter
@Setter
public class Rule extends BaseModel {
    /**
     * 项目编号
     */
    @Id(order = 1)
    private String code;
    /**
     * 项目类型
     */
    @Id(order = 2)
    private String type;
    /**
     * 项目名称
     */
    @Id(order = 3)
    private String name;
    /**
     * 规则类型
     */
    private String ruleType;
    /**
     * 日期
     */
    private String date;
    /**
     * 基础金额
     */
    private BigDecimal base;
    /**
     * 操作涨幅
     */
    private BigDecimal ratio;
    /**
     * 扩大幅度
     */
    private BigDecimal expansion;
    /**
     * 状态：生效、暂停
     */
    private String status;

}
