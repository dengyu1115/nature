package org.nature.biz.manager;

import org.nature.biz.mapper.KlineMapper;
import org.nature.biz.model.Kline;
import org.nature.biz.model.Profit;
import org.nature.biz.model.ProfitView;
import org.nature.biz.model.Rule;
import org.nature.biz.simulator.Simulator;
import org.nature.biz.simulator.SimulatorBuilder;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.TextUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProfitManager {

    private static final int SCALE = 4;
    @Injection
    private KlineMapper klineMapper;
    @Injection
    private RuleManager ruleManager;

    public List<ProfitView> view(String dateEnd) {
        List<Rule> rules = ruleManager.listValid();
        Profit profit = rules.stream().map(i -> this.calc(i, dateEnd)).filter(Objects::nonNull)
                .reduce(new Profit(), this::merge);
        return this.buildView(profit);
    }

    public List<ProfitView> view(Rule rule, String dateEnd) {
        Profit profit = this.calc(rule, dateEnd);
        if (profit == null) {
            return new ArrayList<>();
        }
        return this.buildView(profit);
    }

    public List<Profit> list(List<String> dates) {
        List<Rule> rules = ruleManager.listValid();
        if (dates.size() == 1) {
            String date = dates.get(0);
            return rules.stream().map(i -> this.copy(i, this.calc(i, date)))
                    .filter(Objects::nonNull).collect(Collectors.toList());
        }
        return rules.stream().map(rule -> this.list(rule, dates)).flatMap(List::stream).collect(Collectors.toList());
    }

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

    public Profit merge(List<Profit> list) {
        return list.stream().reduce(new Profit(), this::merge);
    }

    private Profit calc(Rule rule, String dateEnd) {
        List<Kline> list = klineMapper.listByItem(rule.getCode(), rule.getType());
        list.sort(Comparator.comparing(Kline::getDate));
        Simulator simulator = SimulatorBuilder.instance(rule, list, Collections.singletonList(dateEnd));
        simulator.calc();
        return simulator.profit();
    }

    private List<Profit> calc(Rule rule, List<String> dates) {
        List<Kline> list = klineMapper.listByItem(rule.getCode(), rule.getType());
        list.sort(Comparator.comparing(Kline::getDate));
        Simulator simulator = SimulatorBuilder.instance(rule, list, dates);
        simulator.calc();
        return simulator.profits();
    }

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
        BigDecimal paidMax = a.getPaidMax();
        if (paidMax.compareTo(BigDecimal.ZERO) == 0) {
            a.setProfitRatio(BigDecimal.ZERO);
        } else {
            a.setProfitRatio(a.getProfitSold().divide(paidMax, SCALE, RoundingMode.HALF_UP));
        }
        return a;
    }

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
        if (paidMax.compareTo(BigDecimal.ZERO) == 0) {
            profit.setProfitRatio(BigDecimal.ZERO);
        } else {
            profit.setProfitRatio(profit.getProfitSold().divide(paidMax, SCALE, RoundingMode.HALF_UP));
        }
        return profit;
    }

    private List<ProfitView> buildView(Profit profit) {
        List<ProfitView> results = new ArrayList<>();
        results.add(new ProfitView("日期", "开始", profit.getDateStart(), "结束", profit.getDateEnd(), "", ""));
        results.add(new ProfitView("操作次数", "买入", profit.getTimesBuy() + "", "卖出", profit.getTimesSell() + "", "", ""));
        results.add(new ProfitView("投入金额", "最大", TextUtil.amount(profit.getPaidMax()), "剩余", TextUtil.amount(profit.getPaidLeft()), "总额", TextUtil.amount(profit.getPaidTotal())));
        results.add(new ProfitView("回收金额", "", "", "", "", "总额", TextUtil.amount(profit.getReturned())));
        results.add(new ProfitView("份额", "", "", "", "", "总额", TextUtil.text(profit.getShareTotal())));
        results.add(new ProfitView("盈利金额", "卖出", TextUtil.amount(profit.getProfitSold()), "持有", TextUtil.amount(profit.getProfitHold()), "总额", TextUtil.amount(profit.getProfitTotal())));
        results.add(new ProfitView("收益率", "卖出/最大", TextUtil.hundred(profit.getProfitRatio()), "", "", "", ""));
        return results;
    }

    private Profit copy(Rule rule, Profit profit) {
        profit.setCode(rule.getCode());
        profit.setType(rule.getType());
        profit.setRule(rule.getName());
        return profit;
    }

}
