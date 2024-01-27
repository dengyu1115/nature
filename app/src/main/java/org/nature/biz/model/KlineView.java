package org.nature.biz.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.model.BaseModel;

/**
 * K线展示数据
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@Getter
@Setter
public class KlineView extends BaseModel {
    /**
     * 项目名称
     */
    private String name;
    /**
     * 日期
     */
    private String date;
    /**
     * 最新价格
     */
    private Double latest;
    /**
     * 开盘价格
     */
    private Double open;
    /**
     * 最高价格
     */
    private Double high;
    /**
     * 最低价格
     */
    private Double low;
    /**
     * 份额
     */
    private Double share;
    /**
     * 金额
     */
    private Double amount;
    /**
     * 5日均线
     */
    private Double ma5;
    /**
     * 10日均线
     */
    private Double ma10;
    /**
     * 20日均线
     */
    private Double ma20;
    /**
     * 60日均线
     */
    private Double ma60;

}
