package org.nature.biz.manager;

import org.nature.biz.mapper.ItemMapper;
import org.nature.biz.model.Item;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;

import java.util.List;

/**
 * 项目
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@Component
public class ItemManager {

    @Injection
    private ItemMapper itemMapper;

    /**
     * 保存
     * @param item 项目
     * @return int
     */
    public int save(Item item) {
        Item exists = itemMapper.findById(item);
        // 数据已存在
        if (exists != null) {
            throw new RuntimeException("datum exists");
        }
        // 保存
        return itemMapper.save(item);
    }

    /**
     * 编辑
     * @param item 项目
     * @return int
     */
    public int edit(Item item) {
        Item exists = itemMapper.findById(item);
        // 数据不存在
        if (exists == null) {
            throw new RuntimeException("datum not exists");
        }
        // 并入
        return itemMapper.merge(item);
    }

    /**
     * 查询全部项目
     * @return list
     */
    public List<Item> listAll() {
        return itemMapper.listAll();
    }

    /**
     * 删除
     * @param item 项目
     * @return int
     */
    public int delete(Item item) {
        return itemMapper.deleteById(item);
    }
}
