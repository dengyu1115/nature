package org.nature.biz.bound.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

import java.math.BigDecimal;

/**
 * 规则项目
 * @author Nature
 * @version 1.0.0
 * @since 2024/5/24
 */
@Model(db = "nature/biz/bound.db", table = "item")
@Getter
@Setter
public class Item extends BaseModel {
    /**
     * 规则
     */
    @Id(order = 0)
    private String rule;
    /**
     * 编号
     */
    @Id(order = 1)
    private String code;
    /**
     * 类型
     */
    @Id(order = 2)
    private String type;
    /**
     * 名称
     */
    private String name;
    /**
     * 对应的基金编号
     */
    private String fund;
    /**
     * 比例系数
     */
    private BigDecimal ratio;

}
