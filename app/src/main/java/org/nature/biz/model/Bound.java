package org.nature.biz.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

import java.math.BigDecimal;

/**
 * 债券
 * @author Nature
 * @version 1.0.0
 * @since 2024/4/4
 */
@Model(db = "nature/biz.db", table = "bound")
@Getter
@Setter
public class Bound extends BaseModel {
    /**
     * 编号
     */
    @Id(order = 0)
    private String code;
    /**
     * 类型
     */
    @Id(order = 1)
    private String type;
    /**
     * 名称
     */
    private String name;
    /**
     * 类型
     */
    private String bound;
    /**
     * 涨幅比例（满足比例进行提醒）
     */
    private BigDecimal ratio;

}
