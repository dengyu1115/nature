package org.nature.biz.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

import java.math.BigDecimal;

/**
 * 债券操作规则
 * @author Nature
 * @version 1.0.0
 * @since 2024/4/4
 */
@Model(db = "nature/biz.db", table = "bound_rule")
@Getter
@Setter
public class BoundRule extends BaseModel {
    /**
     * 规则名称
     */
    @Id(order = 1)
    private String name;
    /**
     * 类型（1、达到指定比例提醒，2、对比达到指定比例提醒）
     */
    private String type;
    /**
     * 操作涨幅
     */
    private BigDecimal ratio;

}
