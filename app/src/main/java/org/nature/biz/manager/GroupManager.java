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

    /**
     * 保存
     * @param item 分组数据
     * @return int
     */
    public int save(Group item) {
        Group exists = groupMapper.findById(item.getCode());
        // 分组已存在
        if (exists != null) {
            throw new Warn("datum exists");
        }
        // 保存
        return groupMapper.save(item);
    }

    /**
     * 编辑
     * @param item 分组数据
     * @return int
     */
    public int edit(Group item) {
        Group exists = groupMapper.findById(item.getCode());
        // 分组不存在
        if (exists == null) {
            throw new Warn("datum not exists");
        }
        // 并入
        return groupMapper.merge(item);
    }

    /**
     * 查询全部数据
     * @return list
     */
    public List<Group> listAll() {
        return groupMapper.listAll();
    }

    /**
     * 删除
     * @param id id
     * @return int
     */
    public int delete(String id) {
        return groupMapper.deleteById(id);
    }
}
