package org.nature.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 比率结果
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/9
 */
@AllArgsConstructor
@Getter
public class RateResult {
    /**
     * 最大值
     */
    private double max;
    /**
     * 最小值
     */
    private double min;

}
