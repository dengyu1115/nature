package org.nature.html.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

/**
 * 页面配置
 * @author Nature
 * @version 1.0.0
 * @since 2025/11/06
 */
@Model(db = "nature/html.db", table = "page_config")
@Getter
@Setter
public class PageConfig extends BaseModel {
    /**
     * ID
     */
    @Id
    private String id;
    /**
     * 配置内容
     */
    private String config;

}
