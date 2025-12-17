package org.nature.biz.etf.manager;

import org.nature.biz.common.mapper.KlineMapper;
import org.nature.biz.common.model.Kline;
import org.nature.biz.etf.model.Profit;
import org.nature.biz.etf.model.ProfitView;
import org.nature.biz.etf.model.Rule;
import org.nature.biz.etf.simulator.Simulator;
import org.nature.biz.etf.simulator.SimulatorBuilder;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.TextUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static org.nature.common.constant.Const.EMPTY;
import static org.nature.common.constant.Const.SCALE;

/**
 * 收益
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@Component
public class ProfitManager {

    @Injection
    private KlineMapper klineMapper;
    @Injection
    private RuleManager ruleManager;

    /**
     * 总览
     * @param dateEnd 截至日期
     * @return list
     */
    public Profit overview(String dateEnd) {
        // 查询全部有效规则数据，生成总览数据
        return this.buildView(ruleManager.listValid(), dateEnd);
    }

    /**
     * 按规则总览
     * @param rule    规则
     * @param dateEnd 截止日期
     * @return list
     */
    public Profit overview(Rule rule, String dateEnd) {
        return this.buildView(Collections.singletonList(rule), dateEnd);
    }

    /**
     * 按日期查询
     * @param dates 日期集合
     * @return list
     */
    public List<Profit> list(List<String> dates) {
        // 查询全部有效规则数据
        List<Rule> list = ruleManager.listValid();
        if (dates.size() == 1) {
            String date = dates.get(0);
            return list.stream().map(i -> this.copy(i, this.calc(i, date)))
                    .filter(Objects::nonNull).collect(Collectors.toList());
        }
        return list.stream().map(i -> this.list(i, dates)).flatMap(List::stream).collect(Collectors.toList());
    }

    /**
     * 按日期查询单个规则收益
     * @param rule  规则
     * @param dates 日期集合
     * @return list
     */
    public List<Profit> list(Rule rule, List<String> dates) {
        List<Profit> profits = this.calc(rule, dates);
        for (Profit p : profits) {
            this.copy(rule, p);
        }
        if (profits.size() < 2) {
            return profits;
        }
        List<Profit> results = new ArrayList<>();
        for (int i = 1; i < profits.size(); i++) {
            results.add(this.copy(rule, this.diff(profits.get(i), profits.get(i - 1))));
        }
        return results;
    }

    /**
     * 合并收益数据
     * @param list 数据集合
     * @return Profit
     */
    public Profit merge(List<Profit> list) {
        return list.stream().reduce(new Profit(), this::merge);
    }

    /**
     * 生成总览数据
     * @param rules   规则集合
     * @param dateEnd 结束日期
     * @return list
     */
    private Profit buildView(List<Rule> rules, String dateEnd) {
        // 计算收益
        return rules.stream().map(i -> this.calc(i, dateEnd)).filter(Objects::nonNull)
                .reduce(new Profit(), this::merge);
    }

    /**
     * 按规则计算
     * @param rule    规则
     * @param dateEnd 结束日期
     * @return Profit
     */
    private Profit calc(Rule rule, String dateEnd) {
        // 查询K线数据
        List<Kline> list = klineMapper.listByItem(rule.getCode(), rule.getType());
        // 时间正序
        list.sort(Comparator.comparing(Kline::getDate));
        // 构建模拟器实例
        Simulator simulator = SimulatorBuilder.instance(rule, list, Collections.singletonList(dateEnd));
        // 计算收益
        simulator.calc();
        // 返回收益对象
        return simulator.profit();
    }

    /**
     * 按规则计算收益
     * @param rule  规则
     * @param dates 日期集合
     * @return list
     */
    private List<Profit> calc(Rule rule, List<String> dates) {
        List<Kline> list = klineMapper.listByItem(rule.getCode(), rule.getType());
        list.sort(Comparator.comparing(Kline::getDate));
        Simulator simulator = SimulatorBuilder.instance(rule, list, dates);
        simulator.calc();
        return simulator.profits();
    }

    /**
     * 合并收益
     * @param a a
     * @param b b
     * @return Profit
     */
    private Profit merge(Profit a, Profit b) {
        String dateStart = a.getDateStart();
        String start = b.getDateStart();
        a.setDateStart(dateStart == null || start == null || dateStart.compareTo(start) > 0 ? start : dateStart);
        String dateEnd = a.getDateEnd();
        String end = b.getDateEnd();
        a.setDateEnd(dateEnd == null || end == null || dateEnd.compareTo(end) < 0 ? end : dateEnd);
        a.setTimesBuy(a.getTimesBuy() + b.getTimesBuy());
        a.setTimesSell(a.getTimesSell() + b.getTimesSell());
        a.setPaidMax(a.getPaidMax().add(b.getPaidMax()));
        a.setPaidLeft(a.getPaidLeft().add(b.getPaidLeft()));
        a.setShareTotal(a.getShareTotal().add(b.getShareTotal()));
        a.setPaidTotal(a.getPaidTotal().add(b.getPaidTotal()));
        a.setProfitSold(a.getProfitSold().add(b.getProfitSold()));
        a.setProfitHold(a.getProfitHold().add(b.getProfitHold()));
        a.setProfitTotal(a.getProfitTotal().add(b.getProfitTotal()));
        a.setReturned(a.getReturned().add(b.getReturned()));
        a.setAmountCurr(a.getPaidLeft().add(a.getProfitHold()));
        BigDecimal paidMax = a.getPaidMax();
        BigDecimal profitRatio = paidMax.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                a.getProfitSold().divide(paidMax, SCALE, RoundingMode.HALF_UP);
        a.setProfitRatio(profitRatio);
        return a;
    }

    /**
     * 求差值
     * @param a a
     * @param b b
     * @return Profit
     */
    private Profit diff(Profit a, Profit b) {
        Profit profit = new Profit();
        profit.setCode(a.getCode());
        profit.setType(a.getType());
        profit.setRule(a.getRule());
        profit.setDate(a.getDate());
        profit.setDateStart(a.getDateStart());
        profit.setDateEnd(a.getDateEnd());
        profit.setTimesBuy(a.getTimesBuy() - b.getTimesBuy());
        profit.setTimesSell(a.getTimesSell() - b.getTimesSell());
        profit.setPaidMax(a.getPaidMax());
        profit.setPaidLeft(a.getPaidLeft().subtract(b.getPaidLeft()));
        profit.setShareTotal(a.getShareTotal().subtract(b.getShareTotal()));
        profit.setPaidTotal(a.getPaidTotal().subtract(b.getPaidTotal()));
        profit.setProfitSold(a.getProfitSold().subtract(b.getProfitSold()));
        profit.setProfitHold(a.getProfitHold().subtract(b.getProfitHold()));
        profit.setProfitTotal(a.getProfitTotal().subtract(b.getProfitTotal()));
        profit.setReturned(a.getReturned().subtract(b.getReturned()));
        BigDecimal paidMax = profit.getPaidMax();
        BigDecimal profitRatio = paidMax.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO :
                profit.getProfitSold().divide(paidMax, SCALE, RoundingMode.HALF_UP);
        profit.setProfitRatio(profitRatio);
        return profit;
    }

    /**
     * 规则属性拷贝至收益对象
     * @param rule   规则
     * @param profit 收益
     * @return Profit
     */
    private Profit copy(Rule rule, Profit profit) {
        profit.setCode(rule.getCode());
        profit.setType(rule.getType());
        profit.setRule(rule.getName());
        return profit;
    }

}
