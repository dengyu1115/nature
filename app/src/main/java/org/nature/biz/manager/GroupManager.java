package org.nature.biz.manager;

import org.nature.biz.mapper.GroupMapper;
import org.nature.biz.model.Group;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;

import java.util.List;

/**
 * 项目分组
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@Component
public class GroupManager {

    @Injection
    private GroupMapper groupMapper;

    public int save(Group item) {
        Group exists = groupMapper.findById(item.getCode());
        if (exists != null) {
            throw new Warn("datum exists");
        }
        return groupMapper.save(item);
    }

    public int edit(Group item) {
        Group exists = groupMapper.findById(item.getCode());
        if (exists == null) {
            throw new Warn("datum not exists");
        }
        return groupMapper.merge(item);
    }

    public List<Group> listAll() {
        return groupMapper.listAll();
    }

    public int delete(String id) {
        return groupMapper.deleteById(id);
    }
}
