package org.nature.biz.mapper;


import org.nature.biz.model.Group;
import org.nature.common.db.annotation.TableModel;
import org.nature.common.db.function.*;

@TableModel(Group.class)
public interface GroupMapper extends FindById<Group, String>, ListAll<Group>, Save<Group>, Merge<Group>, DeleteById<String> {

}
