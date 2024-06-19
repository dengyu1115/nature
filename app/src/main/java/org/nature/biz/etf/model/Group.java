package org.nature.biz.etf.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

/**
 * 项目分组
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@Model(db = "nature/biz/etf.db", table = "`group`")
@Getter
@Setter
public class Group extends BaseModel {

    @Id
    private String code;
    /**
     * 名称
     */
    private String name;

}
