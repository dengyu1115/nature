package org.nature.biz.manager;

import org.nature.biz.mapper.HoldMapper;
import org.nature.biz.mapper.KlineMapper;
import org.nature.biz.mapper.RuleMapper;
import org.nature.biz.model.Hold;
import org.nature.biz.model.Kline;
import org.nature.biz.model.Rule;
import org.nature.biz.simulator.Simulator;
import org.nature.biz.simulator.SimulatorBuilder;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.CommonUtil;
import org.nature.common.util.RemoteExeUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
public class HoldManager {

    @Injection
    private HoldMapper holdMapper;
    @Injection
    private RuleMapper ruleMapper;
    @Injection
    private KlineMapper klineMapper;

    public int calc() {
        return RemoteExeUtil.exec(ruleMapper::listAll, this::calc).stream().mapToInt(i -> i).sum();
    }

    public int calc(Rule rule) {
        String code = rule.getCode();
        String type = rule.getType();
        List<Kline> list = klineMapper.listByItem(code, type);
        list.sort(Comparator.comparing(Kline::getDate));
        Simulator simulator = SimulatorBuilder.instance(rule, list, Collections.singletonList(CommonUtil.today()));
        simulator.calc();
        List<Hold> holds = simulator.getHoldList();
        String name = rule.getName();
        for (Hold i : holds) {
            i.setRule(name);
        }
        holdMapper.deleteByRule(code, type, name);
        return holdMapper.batchSave(holds);
    }

}
