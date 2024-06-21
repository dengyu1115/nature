package org.nature.biz.common.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.model.BaseModel;

/**
 * 项目
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
@Getter
@Setter
public class NInfo extends BaseModel {

    /**
     * code
     */
    private String code;
    /**
     * 名称
     */
    private String name;

}
