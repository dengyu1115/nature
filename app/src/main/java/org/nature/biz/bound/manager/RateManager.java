package org.nature.biz.bound.manager;

import org.apache.commons.lang3.StringUtils;
import org.nature.biz.bound.mapper.ItemMapper;
import org.nature.biz.bound.mapper.RuleMapper;
import org.nature.biz.bound.model.Item;
import org.nature.biz.bound.model.Rate;
import org.nature.biz.bound.model.Rule;
import org.nature.biz.common.http.KlineHttp;
import org.nature.biz.common.mapper.KlineMapper;
import org.nature.biz.common.mapper.NetMapper;
import org.nature.biz.common.model.Kline;
import org.nature.biz.common.model.Net;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.DateUtil;
import org.nature.func.workday.manager.WorkdayManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 债券
 * @author Nature
 * @version 1.0.0
 * @since 2024/3/18
 */
@Component
public class RateManager {

    public static final int SCALE = 8;
    public static final String NULL = null;

    @Injection
    private KlineHttp klineHttp;
    @Injection
    private WorkdayManager workdayManager;
    @Injection
    private RuleMapper ruleMapper;
    @Injection
    private ItemMapper itemMapper;
    @Injection
    private KlineMapper klineMapper;
    @Injection
    private NetMapper netMapper;

    /**
     * 查询涨幅数据
     * @param rule 规则
     * @param date 日期
     * @return list
     */
    public List<Rate> listRatio(String rule, String date) {
        return this.list(rule, date, (l, r, d, n, p) -> l.stream().map(i -> this.buildRate(i, r, d, n, p))
                .sorted(Comparator.comparing(Rate::getRatio)).collect(Collectors.toList()));
    }

    /**
     * 查询涨幅对比数据
     * @param rule 规则
     * @param date 日期
     * @return list
     */
    public List<Rate> listCompare(String rule, String date) {
        return this.list(rule, date, (l, r, d, n, p) -> l.stream().collect(Collectors.groupingBy(Item::getRule))
                .values().stream().map(i -> i.stream().map(x -> {
                    // 与其他项目对比
                    List<Item> list = new ArrayList<>(i);
                    list.remove(x);
                    return list.stream().map(y -> this.buildRate(x, y, r, d, n, p)).collect(Collectors.toList());
                }).collect(Collectors.toList())).flatMap(List::stream).flatMap(List::stream)
                .sorted(Comparator.comparing(Rate::getRatio)).collect(Collectors.toList()));
    }

    public List<Rate> listTrigger() {
        return this.list(NULL, NULL, (l, r, d, n, p) -> l.stream().collect(Collectors.groupingBy(Item::getRule))
                .values().stream().map(i -> i.stream().map(x -> {
                    // 与其他项目对比
                    List<Item> list = new ArrayList<>(i);
                    list.remove(x);
                    return list.stream().map(y -> this.buildRate(x, y, r, d, n, p))
                            .filter(e -> {
                                BigDecimal ratio = e.getRatio();
                                Rule rule = r.get(e.getRuleCode());
                                e.setRuleName(rule.getName());
                                return ratio.compareTo(rule.getDiff().negate()) < 0;
                            })
                            .collect(Collectors.toList());
                }).collect(Collectors.toList())).flatMap(List::stream).flatMap(List::stream)
                .sorted(Comparator.comparing(Rate::getRatio)).collect(Collectors.toList()));
    }

    /**
     * 净值map
     * @param items   项目数据集合
     * @param dateMap 日期map
     * @return map
     */
    public Map<String, BigDecimal> netMap(List<Item> items, Map<String, String> dateMap) {
        List<Net> nets = netMapper.listByIds(items.stream().map(i -> {
            Net net = new Net();
            net.setCode(i.getFund());
            net.setDate(dateMap.get(i.getRule()));
            return net;
        }).collect(Collectors.toList()));
        return nets.stream().collect(Collectors.toMap(i -> i.getCode() + ":" + i.getDate(), Net::getNet, (o, n) -> o));
    }

    /**
     * 价格map
     * @param items 项目数据集合
     * @param date  K线数据取数日期
     * @return map
     */
    public Map<String, BigDecimal> priceMap(List<Item> items, String date) {
        if (DateUtil.today().equals(date)) {
            Map<String, BigDecimal> map = new ConcurrentHashMap<>();
            items.parallelStream().forEach(i -> {
                String code = i.getCode();
                String type = i.getType();
                List<Kline> list = klineHttp.list(code, type, date, date);
                String key = code + ":" + type;
                Warn.check(list::isEmpty, "获取K线数据失败:" + key);
                BigDecimal latest = list.get(0).getLatest();
                map.put(key, latest);
            });
            return map;
        }
        List<Kline> list = klineMapper.listByIds(items.stream().map(i -> {
            Kline kline = new Kline();
            kline.setCode(i.getCode());
            kline.setType(i.getType());
            kline.setDate(date);
            return kline;
        }).collect(Collectors.toList()));
        return list.stream().collect(Collectors.toMap(i -> i.getCode() + ":" + i.getType(), Kline::getLatest));
    }

    /**
     * 查询涨幅集合
     * @param rule 规则
     * @param date 日期
     * @param c    处理逻辑
     * @return list
     */
    private List<Rate> list(String rule, String date, C2 c) {
        // 查询规则数据
        List<Rule> rules = ruleMapper.listAll().stream()
                .filter(i -> "1".equals(i.getStatus()) && (rule == null || rule.equals(i.getCode())))
                .collect(Collectors.toList());
        if (rules.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, Rule> ruleMap = rules.stream().collect(Collectors.toMap(Rule::getCode, i -> i));
        // 查询规则包含的项目集合
        List<Item> items = itemMapper.listByRules(new ArrayList<>(ruleMap.keySet()));
        if (items.isEmpty()) {
            return new ArrayList<>();
        }
        // 计算价格取数日期
        String priceDate = this.priceDate(date);
        // 计算净值取数日期
        Map<String, String> netDateMap = this.netDateMap(rules, priceDate);
        // 净值数据map
        Map<String, BigDecimal> netMap = this.netMap(items, netDateMap);
        // 价格数据map
        Map<String, BigDecimal> priceMap = this.priceMap(items, priceDate);

        // 结果数据转换输出
        return c.use(items, ruleMap, netDateMap, netMap, priceMap);
    }

    /**
     * 计算价格取数日期
     * @return String
     */
    private String priceDate(String date) {
        String now = DateUtil.nowTime();
        String today = DateUtil.today();
        if (StringUtils.isBlank(date) || date.compareTo(today) >= 0) {
            // 9:25前看昨天数据
            String day = "09:25:00".compareTo(now) < 0 ? today : DateUtil.addDays(today, -1);
            return workdayManager.latestWorkday(day);
        }
        return workdayManager.latestWorkday(date);
    }

    /**
     * 净值日期map
     * @param rules   规则集合
     * @param endDate 截至日期
     * @return map
     */
    private Map<String, String> netDateMap(List<Rule> rules, String endDate) {
        // 取截至日期相距取数天数的最近工作日作为取净值数据的日期
        return rules.stream().collect(Collectors.toMap(Rule::getCode,
                i -> workdayManager.lastWorkday(endDate, i.getDays())));
    }

    /**
     * 构建涨幅对象
     * @param item     项目
     * @param ruleMap  规则map
     * @param dateMap  日期map
     * @param netMap   净值map
     * @param priceMap 价格map
     * @return Rate
     */
    private Rate buildRate(Item item, Map<String, Rule> ruleMap, Map<String, String> dateMap,
                           Map<String, BigDecimal> netMap, Map<String, BigDecimal> priceMap) {
        Rate rate = new Rate();
        Rule rule = ruleMap.get(item.getRule());
        rate.setRuleCode(rule.getCode());
        rate.setRuleName(rule.getName());
        BigDecimal net = this.fillItemProp(rate, item, dateMap, netMap, priceMap, (i, c, n, p) -> {
            i.setCode1(c);
            i.setName1(n);
            i.setPrice1(p);
        });
        rate.setCode2("NET");
        rate.setName2("净值");
        rate.setPrice2(net);
        // 涨幅=（价格-净值）x系数÷净值
        rate.setRatio(rate.getPrice1().subtract(net).multiply(item.getRatio()).divide(net, SCALE, RoundingMode.HALF_UP));
        return rate;
    }

    /**
     * 构建涨幅对象
     * @param i1       项目1
     * @param i2       项目2
     * @param ruleMap  规则map
     * @param dateMap  日期map
     * @param netMap   净值map
     * @param priceMap 价格map
     * @return Rate
     */
    private Rate buildRate(Item i1, Item i2, Map<String, Rule> ruleMap, Map<String, String> dateMap,
                           Map<String, BigDecimal> netMap, Map<String, BigDecimal> priceMap) {
        Rate rate = new Rate();
        Rule rule = ruleMap.get(i1.getRule());
        rate.setRuleCode(rule.getCode());
        rate.setRuleName(rule.getName());
        // 填充项目1属性，计算净值
        BigDecimal net1 = this.fillItemProp(rate, i1, dateMap, netMap, priceMap, (i, c, n, p) -> {
            i.setCode1(c);
            i.setName1(n);
            i.setPrice1(p);
        });
        // 填充项目2属性，计算净值
        BigDecimal net2 = this.fillItemProp(rate, i2, dateMap, netMap, priceMap, (i, c, n, p) -> {
            i.setCode2(c);
            i.setName2(n);
            i.setPrice2(p);
        });
        // 项目1涨幅计算
        BigDecimal v1 = rate.getPrice1().subtract(net1).multiply(i1.getRatio()).divide(net1, SCALE, RoundingMode.HALF_UP);
        // 项目2涨幅计算
        BigDecimal v2 = rate.getPrice2().subtract(net2).multiply(i2.getRatio()).divide(net2, SCALE, RoundingMode.HALF_UP);
        rate.setRatio(v1.subtract(v2));
        return rate;
    }

    /**
     * 填充涨幅对象属性
     * @param rate     涨幅对象
     * @param item     项目
     * @param dateMap  日期map
     * @param netMap   净值map
     * @param priceMap 价格map
     * @param c        处理逻辑
     * @return BigDecimal
     */
    private BigDecimal fillItemProp(Rate rate, Item item, Map<String, String> dateMap, Map<String, BigDecimal> netMap,
                                    Map<String, BigDecimal> priceMap, C1 c) {
        String code = item.getCode();
        String name = item.getName();
        String type = item.getType();
        String fund = item.getFund();
        String rule = item.getRule();
        String priceKey = code + ":" + type;
        BigDecimal price = priceMap.get(priceKey);
        Warn.check(() -> price == null, "价格获取失败:" + priceKey);
        String netKey = fund + ":" + dateMap.get(rule);
        BigDecimal net = netMap.get(netKey);
        Warn.check(() -> net == null, "净值获取失败:" + netKey);
        Warn.check(() -> BigDecimal.ZERO.compareTo(net) == 0, "净值为0:" + netKey);
        c.use(rate, code, name, price);
        return net;
    }

    @FunctionalInterface
    private interface C1 {
        void use(Rate rate, String code, String name, BigDecimal price);
    }

    @FunctionalInterface
    private interface C2 {
        List<Rate> use(List<Item> items, Map<String, Rule> ruleMap, Map<String, String> dateMap,
                       Map<String, BigDecimal> netMap, Map<String, BigDecimal> priceMap);
    }

}
