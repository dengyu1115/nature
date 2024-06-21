package org.nature.biz.bound.manager;

import org.nature.biz.bound.mapper.ItemMapper;
import org.nature.biz.common.model.KInfo;
import org.nature.biz.common.protocol.KlineItems;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.ExecUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 规则
 * @author Nature
 * @version 1.0.0
 * @since 2024/5/31
 */
@Component
public class RuleManager implements KlineItems {

    @Injection
    private ItemMapper itemMapper;
    @Injection
    private NetManager netManager;

    /**
     * 加载
     * @return int
     */
    public int loadNet() {
        return ExecUtil.batch(itemMapper::listAll, i -> netManager.load(i.getCode()))
                .stream().mapToInt(i -> i).sum();
    }

    /**
     * 重新加载
     * @return int
     */
    public int reloadNet() {
        return ExecUtil.batch(itemMapper::listAll, i -> netManager.reload(i.getCode()))
                .stream().mapToInt(i -> i).sum();
    }

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
}
