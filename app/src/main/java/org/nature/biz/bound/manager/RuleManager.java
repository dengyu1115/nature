package org.nature.biz.bound.manager;

import org.nature.biz.bound.mapper.ItemMapper;
import org.nature.biz.common.manager.NetManager;
import org.nature.biz.common.model.KInfo;
import org.nature.biz.common.model.NInfo;
import org.nature.biz.common.protocol.KlineItems;
import org.nature.biz.common.protocol.NetItems;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 规则
 * @author Nature
 * @version 1.0.0
 * @since 2024/5/31
 */
@Component
public class RuleManager implements KlineItems, NetItems {

    @Injection
    private ItemMapper itemMapper;
    @Injection
    private NetManager netManager;

    @Override
    public List<KInfo> kItems() {
        return itemMapper.listAll().stream().map(i -> {
            KInfo info = new KInfo();
            info.setCode(i.getCode());
            info.setType(i.getType());
            info.setName(i.getName());
            return info;
        }).collect(Collectors.toList());
    }

    @Override
    public List<NInfo> nItems() {
        return itemMapper.listAll().stream().map(i -> {
            NInfo info = new NInfo();
            info.setCode(i.getFund());
            info.setName(i.getName());
            return info;
        }).collect(Collectors.toList());
    }
}
