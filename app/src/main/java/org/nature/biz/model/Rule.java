package org.nature.biz.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

import java.math.BigDecimal;

@Model(db = "nature/biz.db", table = "rule")
@Getter
@Setter
public class Rule extends BaseModel {

    @Id(order = 1)
    private String code;
    @Id(order = 2)
    private String type;
    @Id(order = 3)
    private String name;

    private String ruleType;
    private String date;
    private BigDecimal base;
    private BigDecimal ratio;
    private BigDecimal expansion;
    private String status;

}
