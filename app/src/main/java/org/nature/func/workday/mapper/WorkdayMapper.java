package org.nature.func.workday.mapper;


import org.nature.common.db.annotation.Delete;
import org.nature.common.db.annotation.Param;
import org.nature.common.db.annotation.QueryList;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.BatchSave;
import org.nature.common.db.function.FindById;
import org.nature.func.workday.model.Workday;

import java.util.List;

@TableModel(Workday.class)
public interface WorkdayMapper extends BatchSave<Workday>, FindById<Workday, String> {

    @QueryList(where = "date like #{year}||'%'")
    List<Workday> listByYear(@Param("year") String year);

    @Delete(where = "date like #{year}||'%'")
    int deleteByYear(@Param("year") String year);

}
