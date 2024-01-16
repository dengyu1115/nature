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
    private String signature;
    private String type;
    private String year;
    private String month;
    private String day;
    private String hour;
    private String minute;
    private String second;
    private String status;

}
