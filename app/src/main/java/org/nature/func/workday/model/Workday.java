package org.nature.func.workday.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

@Model(db = "nature/func.db", table = "workday")
@Getter
@Setter
public class Workday extends BaseModel {

    @Id
    private String date;
    private String type;

}
