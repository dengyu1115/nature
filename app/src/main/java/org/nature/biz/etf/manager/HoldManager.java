package org.nature.biz.etf.manager;

import org.nature.biz.etf.mapper.HoldMapper;
import org.nature.biz.common.mapper.KlineMapper;
import org.nature.biz.etf.mapper.RuleMapper;
import org.nature.biz.etf.model.Hold;
import org.nature.biz.common.model.Kline;
import org.nature.biz.etf.model.Rule;
import org.nature.biz.etf.simulator.Simulator;
import org.nature.biz.etf.simulator.SimulatorBuilder;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.DateUtil;
import org.nature.common.util.ExecUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 持有数据
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@Component
public class HoldManager {

    @Injection
    private HoldMapper holdMapper;
    @Injection
    private RuleMapper ruleMapper;
    @Injection
    private KlineMapper klineMapper;

    /**
     * 计算持有数据
     * @return int
     */
    public int calc() {
        return ExecUtil.batch(ruleMapper::listAll, this::calc).stream().mapToInt(i -> i).sum();
    }

    /**
     * 按规则计算
     * @param rule 规则数据
     * @return int
     */
    public int calc(Rule rule) {
        String code = rule.getCode();
        String type = rule.getType();
        // 获取K线数据
        List<Kline> list = klineMapper.listByItem(code, type);
        // 按时间正序排序
        list.sort(Comparator.comparing(Kline::getDate));
        // 创建模拟器
        Simulator simulator = SimulatorBuilder.instance(rule, list, Collections.singletonList(DateUtil.today()));
        // 计算
        simulator.calc();
        // 获取持有数据
        List<Hold> holds = simulator.getHoldList();
        String name = rule.getName();
        for (Hold i : holds) {
            i.setRule(name);
        }
        // 删除原数据
        holdMapper.deleteByRule(code, type, name);
        // 新结果保存
        return holdMapper.batchSave(holds);
    }

}
