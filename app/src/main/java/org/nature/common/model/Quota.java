package org.nature.common.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 指标
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/9
 */
@Getter
@Setter
public class Quota extends BaseModel {

    private String dateStart;
    private String dateEnd;
    private Double latest;
    private Double open;
    private Double high;
    private Double low;
    private Double avg;
    private Double rateOpen;
    private Double rateHigh;
    private Double rateLow;
    private Double rateAvg;
    private Double rateHL;
    private Double rateLH;
    private Double ratioLatest;
    private Double ratioAvg;

}
