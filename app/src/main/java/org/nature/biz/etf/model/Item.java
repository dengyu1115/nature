package org.nature.biz.etf.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Column;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

/**
 * 项目
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
@Model(db = "nature/biz/etf.db", table = "item")
@Getter
@Setter
public class Item extends BaseModel {

    @Id
    private String code;
    @Id
    private String type;
    /**
     * 名称
     */
    private String name;
    /**
     * 分组
     */
    @Column("`group`")
    private String group;

}
