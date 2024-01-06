package org.nature.biz.manager;

import org.nature.biz.mapper.GroupMapper;
import org.nature.biz.model.Group;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;

import java.util.List;

@Component
public class GroupManager {

    @Injection
    private GroupMapper groupMapper;

    public int save(Group item) {
        Group exists = groupMapper.findById(item.getCode());
        if (exists != null) {
            throw new RuntimeException("datum exists");
        }
        return groupMapper.save(item);
    }

    public int edit(Group item) {
        Group exists = groupMapper.findById(item.getCode());
        if (exists == null) {
            throw new RuntimeException("datum not exists");
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
