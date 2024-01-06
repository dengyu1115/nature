package org.nature.biz.mapper;


import org.nature.biz.model.Kline;
import org.nature.common.db.annotation.*;
import org.nature.common.db.function.BatchMerge;
import org.nature.common.db.function.BatchSave;

import java.util.List;

@TableModel(Kline.class)
public interface KlineMapper extends BatchSave<Kline>, BatchMerge<Kline> {

    @QueryOne(where = "code=#{code} and type=#{type} order by date desc limit 1")
    Kline findLatest(@Param("code") String code, @Param("type") String type);

    @QueryOne(where = "code=#{code} and type=#{type} order by date limit 1")
    Kline findFirst(@Param("code") String code, @Param("type") String type);

    @QueryList(where = "code=#{code} and type=#{type} order by date desc")
    List<Kline> listByItem(@Param("code") String code, @Param("type") String type);

    @Delete(where = "code=#{code} and type=#{type}")
    int deleteByItem(@Param("code") String code, @Param("type") String type);
}
