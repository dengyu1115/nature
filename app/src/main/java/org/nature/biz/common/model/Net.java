package org.nature.biz.common.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

import java.math.BigDecimal;

/**
 * 基金净值
 * @author Nature
 * @version 1.0.0
 * @since 2024/5/25
 */
@Model(db = "nature/biz/common.db", table = "net")
@Getter
@Setter
public class Net extends BaseModel {
    /**
     * 编号
     */
    @Id(order = 0)
    private String code;
    /**
     * 日期
     */
    @Id(order = 1)
    private String date;
    /**
     * 净值
     */
    private BigDecimal net;
    /**
     * 累计净值
     */
    private BigDecimal lj;
    /**
     * 单位净值
     */
    private BigDecimal dw;
}
