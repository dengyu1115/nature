package org.nature.func.workday.mapper;


import org.nature.common.db.annotation.Delete;
import org.nature.common.db.annotation.Param;
import org.nature.common.db.annotation.QueryList;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.BatchSave;
import org.nature.common.db.function.FindById;
import org.nature.func.workday.model.Workday;

import java.util.List;

/**
 * 工作日
 * @author Nature
 * @version 1.0.0
 * @since 2024/2/1
 */
@TableModel(Workday.class)
public interface WorkdayMapper extends BatchSave<Workday>, FindById<Workday, String> {

    /**
     * 根据年份查询工作日
     * @param year 年
     * @return list
     */
    @QueryList(where = "date like #{year}||'%'")
    List<Workday> listByYear(@Param("year") String year);

    /**
     * 删除指定年份的工作日
     * @param year 年
     * @return int
     */
    @Delete(where = "date like #{year}||'%'")
    int deleteByYear(@Param("year") String year);

}
