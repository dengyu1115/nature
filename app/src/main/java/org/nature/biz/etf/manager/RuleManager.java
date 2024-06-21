package org.nature.biz.etf.manager;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.nature.biz.common.http.KlineHttp;
import org.nature.biz.common.mapper.KlineMapper;
import org.nature.biz.common.model.KInfo;
import org.nature.biz.common.model.Kline;
import org.nature.biz.common.protocol.KlineItems;
import org.nature.biz.etf.mapper.ItemMapper;
import org.nature.biz.etf.mapper.RuleMapper;
import org.nature.biz.etf.model.Hold;
import org.nature.biz.etf.model.Rule;
import org.nature.biz.etf.simulator.Simulator;
import org.nature.biz.etf.simulator.SimulatorBuilder;
import org.nature.common.constant.Const;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.DateUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 规则
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@Component
public class RuleManager implements KlineItems {

    @Injection
    private RuleMapper ruleMapper;
    @Injection
    private ItemMapper itemMapper;
    @Injection
    private KlineMapper klineMapper;
    @Injection
    private KlineHttp klineHttp;

    /**
     * 删除
     * @param rule 规则
     * @return int
     */
    public int delete(Rule rule) {
        return ruleMapper.deleteById(rule);
    }

    /**
     * 最新操作
     * @return list
     */
    public List<Hold> latestHandle() {
        List<Rule> rules = this.listValid();
        // 无规则数据直接返回
        if (rules.isEmpty()) {
            return new ArrayList<>();
        }
        // 按项目分组
        Map<String, List<Rule>> map = rules.stream()
                .collect(Collectors.groupingBy(i -> String.join(Const.DELIMITER, i.getCode(), i.getType())));
        // 当前日期
        String date = DateFormatUtils.format(new Date(), Const.FORMAT_DAY);
        List<Hold> holds = new ArrayList<>();
        for (Map.Entry<String, List<Rule>> i : map.entrySet()) {
            String[] split = i.getKey().split(":");
            String code = split[0];
            String type = split[1];
            List<Kline> list = this.listKline(code, type, date);
            for (Rule rule : i.getValue()) {
                // 按规则计算最新操作数据
                holds.addAll(this.latestHandle(rule, list));
            }
        }
        return holds;
    }

    /**
     * 预计操作
     * @param count 数量
     * @return list
     */
    public List<Hold> nextHandle(int count) {
        List<Rule> rules = this.listValid();
        // 无规则数据直接返回
        if (rules.isEmpty()) {
            return new ArrayList<>();
        }
        // 按项目分组
        Map<String, List<Rule>> map = rules.stream()
                .collect(Collectors.groupingBy(i -> String.join(":", i.getCode(), i.getType())));
        // 当前日期
        String date = DateFormatUtils.format(new Date(), "yyyyMMdd");
        List<Hold> holds = new ArrayList<>();
        for (Map.Entry<String, List<Rule>> i : map.entrySet()) {
            String[] split = i.getKey().split(":");
            String code = split[0];
            String type = split[1];
            // 查询K线数据
            List<Kline> list = this.listKline(code, type, date);
            for (Rule rule : i.getValue()) {
                // 按规则计算预计操作数据
                holds.addAll(this.nextHandle(rule, list, count));
            }
        }
        return holds;
    }

    /**
     * 按规则计算最新操作数据
     * @param rule 规则
     * @param list K线数据
     * @return list
     */
    public List<Hold> latestHandle(Rule rule, List<Kline> list) {
        list.sort(Comparator.comparing(Kline::getDate));
        Simulator simulator = SimulatorBuilder.instance(rule, list, Collections.singletonList(DateUtil.today()));
        simulator.calc();
        List<Hold> holds = simulator.latestHandle();
        for (Hold i : holds) {
            i.setRule(rule.getName());
        }
        return holds;
    }

    /**
     * 按规则计算预计操作数据
     * @param rule  规则
     * @param list  K线数据
     * @param count 条数
     * @return list
     */
    public List<Hold> nextHandle(Rule rule, List<Kline> list, int count) {
        list.sort(Comparator.comparing(Kline::getDate));
        Simulator simulator = SimulatorBuilder.instance(rule, list, Collections.singletonList(DateUtil.today()));
        simulator.calc();
        List<Hold> holds = simulator.nextHandle(count);
        for (Hold i : holds) {
            i.setRule(rule.getName());
        }
        return holds;
    }

    /**
     * 查询有效规则
     * @return list
     */
    public List<Rule> listValid() {
        return ruleMapper.listAll().stream().filter(i -> "1".equals(i.getStatus())).collect(Collectors.toList());
    }

    /**
     * 查询K线数据
     * @param code code
     * @param type 类型
     * @param date 日期
     * @return list
     */
    private List<Kline> listKline(String code, String type, String date) {
        // 查询K线数据
        List<Kline> kList = klineMapper.listByItem(code, type);
        // 移除比当前日期大的数据
        kList.removeIf(i -> i.getDate().compareTo(date) > 0);
        // 按日期排序
        kList.sort(Comparator.comparing(Kline::getDate));
        // 计算开始日期
        String start = "";
        if (!kList.isEmpty()) {
            int index = kList.size() - 1;
            Kline kline = kList.get(index);
            String lastDate = kline.getDate();
            if (lastDate.compareTo(date) == 0) {
                // 如果有当天的数据移除掉，开始日期取当天
                kList.remove(index);
                start = date;
            } else {
                start = DateUtil.addDays(lastDate, 1);
            }
        }
        // 网络查询最新K线数据
        List<Kline> list = klineHttp.list(code, type, start, date);
        // 库中数据和网络数据合并
        kList.addAll(list);
        return kList;
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
