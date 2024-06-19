package org.nature.biz.etf.mapper;


import org.nature.biz.etf.model.Group;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.*;

/**
 * 项目分组
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@TableModel(Group.class)
public interface GroupMapper extends FindById<Group, String>, ListAll<Group>, Save<Group>, Merge<Group>, DeleteById<String> {

}
