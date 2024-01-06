package org.nature.func.job.mapper;


import org.nature.common.db.annotation.Param;
import org.nature.common.db.annotation.QueryList;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.*;
import org.nature.func.job.model.ConfigInfo;

import java.util.List;

@TableModel(ConfigInfo.class)
public interface ConfigInfoMapper extends Save<ConfigInfo>, Merge<ConfigInfo>,
        DeleteById<ConfigInfo>, ListAll<ConfigInfo>, FindById<ConfigInfo, ConfigInfo> {

    @QueryList(where = "type=#{type}")
    List<ConfigInfo> listByType(@Param("type") String type);

}
