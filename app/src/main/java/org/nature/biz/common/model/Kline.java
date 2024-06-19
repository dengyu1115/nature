package org.nature.biz.common.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

import java.math.BigDecimal;

/**
 * K线
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@Model(db = "nature/biz/common.db", table = "kline")
@Getter
@Setter
public class Kline extends BaseModel {
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
     * 日期
     */
    @Id(order = 3)
    private String date;
    /**
     * 最新价格
     */
    private BigDecimal latest;
    /**
     * 开盘价格
     */
    private BigDecimal open;
    /**
     * 最高价格
     */
    private BigDecimal high;
    /**
     * 最低价格
     */
    private BigDecimal low;
    /**
     * 份额
     */
    private BigDecimal share;
    /**
     * 金额
     */
    private BigDecimal amount;

}
