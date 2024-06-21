package org.nature.biz.common.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.model.BaseModel;

/**
 * 净值展示
 * @author Nature
 * @version 1.0.0
 * @since 2024/6/21
 */
@Getter
@Setter
public class NView extends BaseModel {
    /**
     * 项目名称
     */
    private String name;
    /**
     * 日期
     */
    private String date;
    /**
     * 单位净值
     */
    private Double dw;
    /**
     * 累计净值
     */
    private Double lj;
    /**
     * 复权净值
     */
    private Double net;
}
