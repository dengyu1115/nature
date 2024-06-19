package org.nature.biz.bound.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

import java.math.BigDecimal;

/**
 * 规则
 * @author Nature
 * @version 1.0.0
 * @since 2024/5/24
 */
@Model(db = "nature/biz/bound.db", table = "rule")
@Getter
@Setter
public class Rule extends BaseModel {
    /**
     * 编号
     */
    @Id
    private String code;
    /**
     * 名称
     */
    private String name;
    /**
     * 开始日期
     */
    private String dateStart;
    /**
     * 结束日期
     */
    private String dateEnd;
    /**
     * 取多少天之前的数
     */
    private int days;
    /**
     * 对比差值
     */
    private BigDecimal diff;
    /**
     * 状态：1-有效，0-无效
     */
    private String status;

}
