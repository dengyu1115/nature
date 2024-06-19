package org.nature.biz.bound.manager;

import org.nature.biz.bound.mapper.ItemMapper;
import org.nature.biz.bound.mapper.RuleMapper;
import org.nature.biz.common.manager.KlineManager;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.ExecUtil;

/**
 * 规则
 * @author Nature
 * @version 1.0.0
 * @since 2024/5/31
 */
@Component
public class RuleManager {

    @Injection
    private RuleMapper ruleMapper;
    @Injection
    private ItemMapper itemMapper;
    @Injection
    private KlineManager klineManager;
    @Injection
    private NetManager netManager;

    /**
     * 加载
     * @return int
     */
    public int loadKline() {
        return ExecUtil.batch(itemMapper::listAll, i -> klineManager.load(i.getCode(), i.getType()))
                .stream().mapToInt(i -> i).sum();
    }

    /**
     * 重新加载
     * @return int
     */
    public int reloadKline() {
        return ExecUtil.batch(itemMapper::listAll, i -> klineManager.reload(i.getCode(), i.getType()))
                .stream().mapToInt(i -> i).sum();
    }

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

}
