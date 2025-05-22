package org.nature.biz.etf.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.model.BaseModel;

import java.math.BigDecimal;

/**
 * 收益
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@Getter
@Setter
public class Profit extends BaseModel {
    /**
     * 项目编号
     */
    private String code;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 项目类型
     */
    private String type;
    /**
     * 策略规则
     */
    private String rule;
    /**
     * 日期
     */
    private String date;
    /**
     * 开始日期
     */
    private String dateStart;
    /**
     * 结束日期
     */
    private String dateEnd;
    /**
     * 买入次数
     */
    private int timesBuy;
    /**
     * 卖出次数
     */
    private int timesSell;
    /**
     * 总份额
     */
    private BigDecimal shareTotal = BigDecimal.ZERO;
    /**
     * 总投入
     */
    private BigDecimal paidTotal = BigDecimal.ZERO;
    /**
     * 最大投入
     */
    private BigDecimal paidMax = BigDecimal.ZERO;
    /**
     * 剩余投入
     */
    private BigDecimal paidLeft = BigDecimal.ZERO;
    /**
     * 回收金额
     */
    private BigDecimal returned = BigDecimal.ZERO;
    /**
     * 总收益
     */
    private BigDecimal profitTotal = BigDecimal.ZERO;
    /**
     * 持有收益
     */
    private BigDecimal profitHold = BigDecimal.ZERO;
    /**
     * 卖出收益
     */
    private BigDecimal profitSold = BigDecimal.ZERO;
    /**
     * 收益率
     */
    private BigDecimal profitRatio = BigDecimal.ZERO;
    /**
     * 当前金额
     */
    private BigDecimal amountCurr = BigDecimal.ZERO;

}
