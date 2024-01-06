package org.nature.biz.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.model.BaseModel;

import java.math.BigDecimal;

@Getter
@Setter
public class Profit extends BaseModel {

    private String code;
    private String name;
    private String type;
    private String rule;
    private String date;
    private String dateStart;
    private String dateEnd;
    private int timesBuy;
    private int timesSell;
    private BigDecimal shareTotal = BigDecimal.ZERO;
    private BigDecimal paidTotal = BigDecimal.ZERO;
    private BigDecimal paidMax = BigDecimal.ZERO;
    private BigDecimal paidLeft = BigDecimal.ZERO;
    private BigDecimal returned = BigDecimal.ZERO;
    private BigDecimal profitTotal = BigDecimal.ZERO;
    private BigDecimal profitHold = BigDecimal.ZERO;
    private BigDecimal profitSold = BigDecimal.ZERO;
    private BigDecimal profitRatio = BigDecimal.ZERO;

}
