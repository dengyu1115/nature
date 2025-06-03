package org.nature.biz.bound.manager;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.nature.biz.bound.mapper.ItemMapper;
import org.nature.biz.bound.model.Item;
import org.nature.biz.bound.model.Result;
import org.nature.biz.bound.model.Rule;
import org.nature.biz.common.mapper.KlineMapper;
import org.nature.biz.common.mapper.NetMapper;
import org.nature.biz.common.model.Kline;
import org.nature.biz.common.model.Net;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.DateUtil;
import org.nature.common.util.TextUtil;
import org.nature.func.workday.manager.WorkdayManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 演算
 * @author Nature
 * @version 1.0.0
 * @since 2024/5/30
 */
@Component
public class CalcManager {

    /**
     * 策略集合
     */
    private static final Map<String, Strategy> STRATEGIES = new HashMap<>();

    static {
        STRATEGIES.put("MIN", (p, m, d, l) -> !CalcManager.same(p, m));
        STRATEGIES.put("DIFF", (p, m, d, l) -> {
            // 当前持有和最适合买入的是同一标的
            if (CalcManager.same(p, m)) {
                return false;
            }
            BigDecimal ratio = l.stream().filter(i -> CalcManager.same(i, p)).map(IR::getRatio).findFirst()
                    .orElseThrow(() -> new Warn("无数据"));
            // 判断最适合买入的标的和持有标的的涨幅差是否满足切换差值
            return ratio.subtract(m.ratio).compareTo(d) >= 0;
        });
        STRATEGIES.put("!DIFF", (p, m, d, l) -> {
            // 当前持有和最适合买入的是同一标的
            if (CalcManager.same(p, m)) {
                return false;
            }
            BigDecimal ratio = l.stream().filter(i -> CalcManager.same(i, p)).map(IR::getRatio).findFirst()
                    .orElseThrow(() -> new Warn("无数据"));
            // 判断最适合买入的标的和持有标的的涨幅差是否满足切换差值
            return ratio.subtract(m.ratio).compareTo(d) < 0;
        });
    }

    @Injection
    private NetMapper netMapper;
    @Injection
    private ItemMapper itemMapper;
    @Injection
    private KlineMapper klineMapper;
    @Injection
    private WorkdayManager workdayManager;

    /**
     * 判断是否同一标的
     * @param a a
     * @param b b
     * @return boolean
     */
    private static boolean same(IR a, IR b) {
        return a.code.equals(b.code) && a.type.equals(b.type);
    }

    /**
     * 处理
     * @param rule     规则
     * @param strategy 策略
     * @return list
     */
    public List<Result> process(Rule rule, String strategy) {
        String dateStart = rule.getDateStart();
        String dateEnd = rule.getDateEnd();
        String firstDate = workdayManager.latestWorkday(dateStart);
        if (StringUtils.isBlank(dateEnd)) {
            dateEnd = DateUtil.today();
        }
        List<Item> items = itemMapper.listByRule(rule.getCode());
        if (items.isEmpty()) {
            throw new Warn("没有配置项目");
        }
        int days = rule.getDays();
        String start = workdayManager.lastWorkday(firstDate, days);
        List<String> fundCodes = items.stream().map(Item::getFund).distinct().collect(Collectors.toList());
        String end = dateEnd;
        List<Net> list = netMapper.list(fundCodes, start, end);
        Map<String, BigDecimal> netMap = list.stream()
                .collect(Collectors.toMap(i -> this.nk(i.getCode(), i.getDate()), Net::getNet));
        Map<String, BigDecimal> priceMap = items.stream()
                .map(i -> klineMapper.list(i.getCode(), i.getType(), start, end)).flatMap(List::stream)
                .collect(Collectors.toMap(i -> this.pk(i.getCode(), i.getType(), i.getDate()), Kline::getLatest, (o, n) -> o));
        // 上一个处理工作日
        String preWd = null;
        // 上一个处理交易日
        String preTd = null;
        IR preItem = null;
        BigDecimal diff = rule.getDiff();
        List<Result> results = new ArrayList<>();
        BigDecimal total = new BigDecimal("0");
        while (dateStart.compareTo(dateEnd) <= 0) {
            String workday = workdayManager.latestWorkday(dateStart);
            // 下个日期
            dateStart = DateUtil.addDays(dateStart, 1);
            // 跳过节假日
            if (workday.equals(preWd)) {
                continue;
            }
            String netDate = workdayManager.lastWorkday(workday, days);
            // 上一交易日修改为本次处理的交易日
            preWd = workday;
            // 计算标的涨幅数据
            List<IR> irs = items.stream().map(i -> this.buildIR(i, workday, priceMap, netDate, netMap))
                    .collect(Collectors.toList());
            IR min = irs.stream().min(Comparator.comparing(IR::getRatio)).orElseThrow(() -> new Warn("无数据"));
            // 首次买入操作
            if (preItem == null) {
                preItem = min;
                preTd = workday;
            }
            Strategy st = STRATEGIES.get(strategy);
            if (st == null) {
                throw new Warn("策略不存在：" + strategy);
            }
            // 如果有交易则
            if (!st.check(preItem, min, diff, irs)) {
                continue;
            }
            // 收益率汇总
            total = total.add(this.calcRatio(preItem.code, preItem.type, priceMap, preWd, preTd));
            preItem = min;
            preTd = preWd;
        }
        if (preTd != null && preTd.compareTo(preWd) < 0) {
            // 如果最后一个交易日没有进行交易，计算最后一天的收益
            total = total.add(this.calcRatio(preItem.code, preItem.type, priceMap, preWd, preTd));
        }
        results.add(this.buildResult("total", "total", "汇总值", "策略结论", total));
        for (Item i : items) {
            String code = i.getCode();
            String type = i.getType();
            String name = i.getName();
            String ruleName = rule.getName();
            BigDecimal ratio = this.calcRatio(code, type, priceMap, preWd, firstDate);
            results.add(this.buildResult(code, type, name, ruleName, ratio));
        }
        return results;
    }

    /**
     * 构建IR对象
     * @param i        项目
     * @param workday  工作日
     * @param priceMap 价格数据
     * @param netDate  净值日期
     * @param netMap   净值数据
     * @return IR
     */
    private IR buildIR(Item i, String workday, Map<String, BigDecimal> priceMap,
                       String netDate, Map<String, BigDecimal> netMap) {
        IR r = new IR();
        String code = i.getCode();
        String type = i.getType();
        String fund = i.getFund();
        r.setCode(code);
        r.setType(type);
        String pk = this.pk(code, type, workday);
        BigDecimal price = priceMap.get(pk);
        if (price == null) {
            throw new Warn("价格数据不存在：" + pk);
        }
        String nk = this.nk(fund, netDate);
        BigDecimal net = netMap.get(nk);
        if (net == null) {
            throw new Warn("净值数据不存在：" + nk);
        }
        BigDecimal ratio = price.subtract(net).multiply(i.getRatio()).divide(net, 8, RoundingMode.HALF_UP);
        r.setRatio(ratio);
        return r;
    }

    /**
     * 构建结果对象
     * @param code  编号
     * @param type  类型
     * @param name  名称
     * @param rule  规则名称
     * @param ratio 涨幅
     * @return Result
     */
    private Result buildResult(String code, String type, String name, String rule, BigDecimal ratio) {
        Result r = new Result();
        r.setRule(rule);
        r.setCode(code);
        r.setType(type);
        r.setName(name);
        r.setRatio(ratio);
        return r;
    }

    /**
     * 计算涨幅
     * @param code     编号
     * @param type     类型
     * @param priceMap 价格map
     * @param d1       日期1
     * @param d2       日期2
     * @return BigDecimal
     */
    private BigDecimal calcRatio(String code, String type, Map<String, BigDecimal> priceMap, String d1, String d2) {
        String pk1 = this.pk(code, type, d1);
        BigDecimal p1 = priceMap.get(pk1);
        if (p1 == null) {
            throw new Warn("价格数据不存在：" + pk1);
        }
        String pk2 = this.pk(code, type, d2);
        BigDecimal p2 = priceMap.get(pk2);
        if (p2 == null) {
            throw new Warn("价格数据不存在：" + pk2);
        }
        return p1.subtract(p2).divide(p2, 8, RoundingMode.HALF_DOWN);
    }

    /**
     * 价格key
     * @param code 编号
     * @param type 类型
     * @param date 日期
     * @return String
     */
    private String pk(String code, String type, String date) {
        return TextUtil.join(code, type, date);
    }

    /**
     * 净值key
     * @param code 编号
     * @param date 日期
     * @return String
     */
    private String nk(String code, String date) {
        return TextUtil.join(code, date);
    }

    /**
     * 策略
     */
    private interface Strategy {
        boolean check(IR pre, IR min, BigDecimal diff, List<IR> irs);
    }

    @Getter
    @Setter
    private static class IR {
        private String code;
        private String type;
        private BigDecimal ratio;
    }

}
