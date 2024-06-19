package org.nature.biz.bound.mapper;


import org.nature.biz.bound.model.Rule;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.*;

/**
 * 规则
 * @author Nature
 * @version 1.0.0
 * @since 2024/5/24
 */
@TableModel(Rule.class)
public interface RuleMapper extends FindById<Rule, String>, ListAll<Rule>, Save<Rule>, Merge<Rule>, DeleteById<String>,
        ListByIds<Rule, String> {

}
