package org.nature.biz.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.nature.common.model.BaseModel;

/**
 * 收益view
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@AllArgsConstructor
@Getter
@Setter
public class ProfitView extends BaseModel {
    /**
     * 标题
     */
    private String title;
    /**
     * 标题1
     */
    private String title1;
    /**
     * 值1
     */
    private String value1;
    /**
     * 标题2
     */
    private String title2;
    /**
     * 值2
     */
    private String value2;
    /**
     * 标题3
     */
    private String title3;
    /**
     * 值3
     */
    private String value3;
}
