package org.nature.func.workday.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

/**
 * 工作日
 * @author Nature
 * @version 1.0.0
 * @since 2024/2/1
 */
@Model(db = "nature/func.db", table = "workday")
@Getter
@Setter
public class Workday extends BaseModel {
    /**
     * 日期
     */
    @Id
    private String date;
    /**
     * 类型
     */
    private String type;

}
