package org.nature.biz.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.nature.common.model.BaseModel;

import java.math.BigDecimal;

/**
 * 债券涨幅
 * @author Nature
 * @version 1.0.0
 * @since 2024/3/18
 */
@AllArgsConstructor
@Getter
@Setter
public class BoundRate extends BaseModel {
    /**
     * 比对方code
     */
    private String code1;
    /**
     * 比对方name
     */
    private String name1;
    /**
     * 对比方code
     */
    private String code2;
    /**
     * 对比方name
     */
    private String name2;
    /**
     * 涨幅
     */
    private BigDecimal ratio;
}
