package org.nature.func.job.model;

import lombok.Getter;
import lombok.Setter;
import org.nature.common.db.annotation.Id;
import org.nature.common.db.annotation.Model;
import org.nature.common.model.BaseModel;

/**
 * 任务配置信息
 * @author Nature
 * @version 1.0.0
 * @since 2024/2/3
 */
@Model(db = "nature/func.db", table = "job_config_info")
@Getter
@Setter
public class ConfigInfo extends BaseModel {
    /**
     * 任务编号
     */
    @Id(order = 1)
    private String code;
    /**
     * 任务配置唯一标识
     */
    @Id(order = 2)
    private String signature;
    /**
     * 年
     */
    private String year;
    /**
     * 月
     */
    private String month;
    /**
     * 日
     */
    private String day;
    /**
     * 时
     */
    private String hour;
    /**
     * 分
     */
    private String minute;
    /**
     * 秒
     */
    private String second;
    /**
     * 状态
     */
    private String status;

}
