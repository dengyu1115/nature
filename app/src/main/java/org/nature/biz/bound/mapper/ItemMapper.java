package org.nature.biz.bound.mapper;


import org.nature.biz.bound.model.Item;
import org.nature.common.db.annotation.Param;
import org.nature.common.db.annotation.QueryList;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.*;

import java.util.List;

/**
 * 项目
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
@TableModel(Item.class)
public interface ItemMapper extends FindById<Item, Item>, ListAll<Item>, Save<Item>, Merge<Item>, DeleteById<Item> {

    /**
     * 按规则查询
     * @param rule 规则
     * @return list
     */
    @QueryList(where = "rule=#{rule}")
    List<Item> listByRule(@Param("rule") String rule);

    @QueryList(where = "rule in(#{rules})")
    List<Item> listByRules(@Param("rules") List<String> rules);
}
