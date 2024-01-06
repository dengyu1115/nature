package org.nature.biz.mapper;


import org.nature.biz.model.Hold;
import org.nature.common.db.annotation.Delete;
import org.nature.common.db.annotation.Param;
import org.nature.common.db.annotation.QueryList;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.BatchMerge;
import org.nature.common.db.function.BatchSave;

import java.util.List;

@TableModel(Hold.class)
public interface HoldMapper extends BatchSave<Hold>, BatchMerge<Hold> {

    @QueryList(where = "code=#{code} and type=#{type} and rule=#{rule}")
    List<Hold> listByRule(@Param("code") String code, @Param("type") String type, @Param("rule") String rule);

    @Delete(where = "code=#{code} and type=#{type} and rule=#{rule}")
    int deleteByRule(@Param("code") String code, @Param("type") String type, @Param("rule") String rule);

}
