package org.nature.biz.etf.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

import java.math.BigDecimal;

/**
 * 持仓
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@Model(db = "nature/biz/etf.db", table = "hold")
@Getter
@Setter
public class Hold extends BaseModel {
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
     * 策略规则
     */
    @Id(order = 3)
    private String rule;
    /**
     * 买入日期
     */
    @Id(order = 4)
    private String dateBuy;
    /**
     * 卖出日期
     */
    private String dateSell;
    /**
     * 操作次数
     */
    @Id(order = 5)
    private int level;
    /**
     * 买入/卖出原因
     */
    private String reason;
    /**
     * 标记价格
     */
    private BigDecimal mark;
    /**
     * 买入价格
     */
    private BigDecimal priceBuy;
    /**
     * 卖出价格
     */
    private BigDecimal priceSell;
    /**
     * 买入份额
     */
    private BigDecimal shareBuy;
    /**
     * 卖出份额
     */
    private BigDecimal shareSell;
    /**
     * 收益金额
     */
    private BigDecimal profit;

}
