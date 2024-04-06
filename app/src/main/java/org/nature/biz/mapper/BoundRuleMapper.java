package org.nature.biz.mapper;


import org.nature.biz.model.BoundRule;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.*;

/**
 * 债券规则
 * @author Nature
 * @version 1.0.0
 * @since 2024/4/4
 */
@TableModel(BoundRule.class)
public interface BoundRuleMapper extends FindById<BoundRule, String>, ListAll<BoundRule>, Save<BoundRule>, Merge<BoundRule>, DeleteById<String> {

}
