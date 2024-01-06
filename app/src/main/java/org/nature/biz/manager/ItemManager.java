package org.nature.biz.manager;

import org.nature.biz.mapper.ItemMapper;
import org.nature.biz.model.Item;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;

import java.util.List;

@Component
public class ItemManager {

    @Injection
    private ItemMapper itemMapper;

    public int save(Item item) {
        Item exists = itemMapper.findById(item);
        if (exists != null) {
            throw new RuntimeException("datum exists");
        }
        return itemMapper.save(item);
    }

    public int edit(Item item) {
        Item exists = itemMapper.findById(item);
        if (exists == null) {
            throw new RuntimeException("datum not exists");
        }
        return itemMapper.merge(item);
    }

    public List<Item> listAll() {
        return itemMapper.listAll();
    }

    public int delete(Item item) {
        return itemMapper.deleteById(item);
    }
}
