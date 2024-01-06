package org.nature.biz.mapper;


import org.nature.biz.model.Rule;
import org.nature.common.db.annotation.Param;
import org.nature.common.db.annotation.QueryList;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.*;

import java.util.List;

@TableModel(Rule.class)
public interface RuleMapper extends FindById<Rule, Rule>, ListAll<Rule>, Save<Rule>, Merge<Rule>, DeleteById<Rule> {

    @QueryList(where = "code=#{code} and type=#{type} order by date desc")
    List<Rule> listByItem(@Param("code") String code, @Param("type") String type);

}
