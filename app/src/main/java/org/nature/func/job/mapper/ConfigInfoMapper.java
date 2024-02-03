package org.nature.func.job.mapper;


import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.*;
import org.nature.func.job.model.ConfigInfo;

/**
 * 任务配置信息
 * @author Nature
 * @version 1.0.0
 * @since 2024/2/3
 */
@TableModel(ConfigInfo.class)
public interface ConfigInfoMapper extends Save<ConfigInfo>, Merge<ConfigInfo>,
        DeleteById<ConfigInfo>, ListAll<ConfigInfo>, FindById<ConfigInfo, ConfigInfo> {

}
