package org.nature.func.job.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

@Model(db = "nature/func.db", table = "task_exec_record")
@Getter
@Setter
public class ExecRecord extends BaseModel {

    private String code;
    private String name;
    private String startTime;
    private String endTime;
    private String exception;

}
