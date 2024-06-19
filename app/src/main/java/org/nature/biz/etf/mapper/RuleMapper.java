package org.nature.biz.etf.mapper;


import org.nature.biz.etf.model.Rule;
import org.nature.common.db.annotation.Param;
import org.nature.common.db.annotation.QueryList;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.*;

import java.util.List;

/**
 * 策略规则
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@TableModel(Rule.class)
public interface RuleMapper extends FindById<Rule, Rule>, ListAll<Rule>, Save<Rule>, Merge<Rule>, DeleteById<Rule> {

    /**
     * 按项目查询
     * @param code 项目编号
     * @param type 项目类型
     * @return list
     */
    @QueryList(where = "code=#{code} and type=#{type} order by date desc")
    List<Rule> listByItem(@Param("code") String code, @Param("type") String type);

}
