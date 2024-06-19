package org.nature.biz.bound.mapper;


import org.nature.biz.bound.model.Net;
import org.nature.common.db.annotation.*;
import org.nature.common.db.function.BatchMerge;
import org.nature.common.db.function.ListByIds;

import java.util.List;

/**
 * 项目
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
@TableModel(Net.class)
public interface NetMapper extends ListByIds<Net, Net>, BatchMerge<Net> {

    /**
     * 查询最新
     * @param code 项目编号
     * @return Kline
     */
    @QueryOne(where = "code=#{code} order by date desc limit 1")
    Net findLatest(@Param("code") String code);

    /**
     * 按code删除
     * @param code 编号
     * @return int
     */
    @Delete(where = "code=#{code}")
    int deleteByCode(@Param("code") String code);

    /**
     * 查询一段时间内的
     * @param codes code集合
     * @param start 开始日期
     * @param end   结束日期
     * @return list
     */
    @QueryList(where = "code in(#{codes}) and date>=#{start} and date<=#{end}")
    List<Net> list(@Param("codes") List<String> codes, @Param("start") String start, @Param("end") String end);

}
