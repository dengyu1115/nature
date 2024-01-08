package org.nature.biz.mapper;


import org.nature.biz.model.Hold;
import org.nature.common.db.annotation.Delete;
import org.nature.common.db.annotation.Param;
import org.nature.common.db.annotation.QueryList;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.BatchMerge;
import org.nature.common.db.function.BatchSave;

import java.util.List;

/**
 * 持有数据
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@TableModel(Hold.class)
public interface HoldMapper extends BatchSave<Hold>, BatchMerge<Hold> {

    /**
     * 按规则查询
     * @param code 项目编号
     * @param type 项目类型
     * @param rule 规则
     * @return list
     */
    @QueryList(where = "code=#{code} and type=#{type} and rule=#{rule}")
    List<Hold> listByRule(@Param("code") String code, @Param("type") String type, @Param("rule") String rule);

    /**
     * 按规则删除
     * @param code 项目编号
     * @param type 项目类型
     * @param rule 规则
     * @return list
     */
    @Delete(where = "code=#{code} and type=#{type} and rule=#{rule}")
    int deleteByRule(@Param("code") String code, @Param("type") String type, @Param("rule") String rule);

}
