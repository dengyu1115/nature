package org.nature.func.job.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

@Model(db = "nature/func.db", table = "job_config_info")
@Getter
@Setter
public class ConfigInfo extends BaseModel {

    @Id(order = 1)
    private String code;
    @Id(order = 2)
    private String type;
    private String startTime;
    private String endTime;
    private Integer period;
    private String unit;
    private String status;

}
