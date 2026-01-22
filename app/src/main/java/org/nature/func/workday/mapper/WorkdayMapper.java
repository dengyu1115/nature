package org.nature.func.workday.mapper;


import org.nature.common.db.annotation.Param;
import org.nature.common.db.annotation.QueryList;
import org.nature.common.db.annotation.QueryOne;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.BatchSave;
import org.nature.common.db.function.FindById;
import org.nature.common.db.function.Merge;
import org.nature.func.workday.model.Workday;

import java.util.List;

/**
 * 工作日
 * @author Nature
 * @version 1.0.0
 * @since 2024/2/1
 */
@TableModel(Workday.class)
public interface WorkdayMapper extends Merge<Workday>, BatchSave<Workday>, FindById<Workday, String> {

    /**
     * 根据年份查询工作日
     * @param year 年
     * @return list
     */
    @QueryList(where = "date like #{year}||'%'")
    List<Workday> listByYear(@Param("year") String year);

    /**
     * 查询最近一个工作日
     * @param date 日期
     * @return Workday
     */
    @QueryOne(where = "date <= #{date} and type='W' order by date desc limit 1")
    Workday findLatestWorkday(@Param("date") String date);

    @QueryList(where = "date < #{date} and type='W' order by date desc limit #{n}")
    List<Workday> listLast(@Param("date") String date, @Param("n") int n);
}
