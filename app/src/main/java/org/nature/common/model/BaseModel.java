package org.nature.common.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * model 基类
 * @author nature
 * @version 1.0.0
 * @since 2019/8/6 12:41
 */
public class BaseModel {

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
