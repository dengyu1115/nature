package org.nature.biz.mapper;


import org.nature.biz.model.Kline;
import org.nature.common.db.annotation.*;
import org.nature.common.db.function.BatchMerge;
import org.nature.common.db.function.BatchSave;

import java.util.List;

/**
 * K线
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@TableModel(Kline.class)
public interface KlineMapper extends BatchSave<Kline>, BatchMerge<Kline> {

    /**
     * 查询最新的K线
     * @param code 项目编号
     * @param type 项目类型
     * @return Kline
     */
    @QueryOne(where = "code=#{code} and type=#{type} order by date desc limit 1")
    Kline findLatest(@Param("code") String code, @Param("type") String type);

    /**
     * 查询第一条数据
     * @param code 项目编号
     * @param type 项目类型
     * @return Kline
     */
    @QueryOne(where = "code=#{code} and type=#{type} order by date limit 1")
    Kline findFirst(@Param("code") String code, @Param("type") String type);

    /**
     * 按项目查询
     * @param code 项目编号
     * @param type 项目类型
     * @return list
     */
    @QueryList(where = "code=#{code} and type=#{type} order by date desc")
    List<Kline> listByItem(@Param("code") String code, @Param("type") String type);

    /**
     * 按项目删除
     * @param code 项目编号
     * @param type 项目类型
     * @return int
     */
    @Delete(where = "code=#{code} and type=#{type}")
    int deleteByItem(@Param("code") String code, @Param("type") String type);
}
