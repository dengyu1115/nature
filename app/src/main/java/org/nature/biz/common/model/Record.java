package org.nature.biz.common.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

/**
 * 记录
 * @author Nature
 * @version 1.0.0
 * @since 2024/7/15
 */
@Model(db = "nature/biz/common.db", table = "record")
@Getter
@Setter
public class Record extends BaseModel {
    /**
     * 项目编号
     */
    @Id(order = 1)
    private String code;
    /**
     * 日期
     */
    @Id(order = 2)
    private String date;
    /**
     * 内容（通常为json串）
     */
    private String content;

}
