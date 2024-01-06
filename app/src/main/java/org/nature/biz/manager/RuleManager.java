package org.nature.biz.manager;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.nature.biz.http.KlineHttp;
import org.nature.biz.mapper.RuleMapper;
import org.nature.biz.model.Hold;
import org.nature.biz.model.Item;
import org.nature.biz.model.Kline;
import org.nature.biz.model.Rule;
import org.nature.biz.simulator.Simulator;
import org.nature.biz.simulator.SimulatorBuilder;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.CommonUtil;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RuleManager {

    @Injection
    private RuleMapper ruleMapper;
    @Injection
    private KlineHttp klineHttp;

    public int save(Rule rule) {
        Rule exists = ruleMapper.findById(rule);
        if (exists != null) {
            throw new RuntimeException("datum exists");
        }
        return ruleMapper.save(rule);
    }

    public int edit(Rule rule) {
        Rule exists = ruleMapper.findById(rule);
        if (exists == null) {
            throw new RuntimeException("datum not exists");
        }
        return ruleMapper.merge(rule);
    }

    public List<Rule> listByItem(Item item) {
        return ruleMapper.listByItem(item.getCode(), item.getType());
    }

    public int delete(Rule rule) {
        return ruleMapper.deleteById(rule);
    }

    public List<Hold> latestHandle() {
        List<Rule> rules = this.listValid();
        if (rules.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, List<Rule>> map = rules.stream()
                .collect(Collectors.groupingBy(i -> String.join(":", i.getCode(), i.getType())));
        String date = DateFormatUtils.format(new Date(), "yyyyMMdd");
        List<Hold> holds = new ArrayList<>();
        for (Map.Entry<String, List<Rule>> i : map.entrySet()) {
            String[] split = i.getKey().split(":");
            String code = split[0];
            String type = split[1];
            List<Kline> list = klineHttp.list(code, type, "", date);
            for (Rule rule : i.getValue()) {
                holds.addAll(this.latestHandle(rule, list));
            }
        }
        return holds;
    }

    public List<Hold> nextHandle(int count) {
        List<Rule> rules = this.listValid();
        if (rules.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, List<Rule>> map = rules.stream()
                .collect(Collectors.groupingBy(i -> String.join(":", i.getCode(), i.getType())));
        String date = DateFormatUtils.format(new Date(), "yyyyMMdd");
        List<Hold> holds = new ArrayList<>();
        for (Map.Entry<String, List<Rule>> i : map.entrySet()) {
            String[] split = i.getKey().split(":");
            String code = split[0];
            String type = split[1];
            List<Kline> list = klineHttp.list(code, type, "", date);
            for (Rule rule : i.getValue()) {
                holds.addAll(this.nextHandle(rule, list, count));
            }
        }
        return holds;
    }

    public List<Hold> latestHandle(Rule rule, List<Kline> list) {
        list.sort(Comparator.comparing(Kline::getDate));
        Simulator simulator = SimulatorBuilder.instance(rule, list, Collections.singletonList(CommonUtil.today()));
        simulator.calc();
        List<Hold> holds = simulator.latestHandle();
        for (Hold i : holds) {
            i.setRule(rule.getName());
        }
        return holds;
    }

    public List<Hold> nextHandle(Rule rule, List<Kline> list, int count) {
        list.sort(Comparator.comparing(Kline::getDate));
        Simulator simulator = SimulatorBuilder.instance(rule, list, Collections.singletonList(CommonUtil.today()));
        simulator.calc();
        List<Hold> holds = simulator.nextHandle(count);
        for (Hold i : holds) {
            i.setRule(rule.getName());
        }
        return holds;
    }

    public List<Rule> listValid() {
        return ruleMapper.listAll().stream().filter(i -> "1".equals(i.getStatus())).collect(Collectors.toList());
    }

}
