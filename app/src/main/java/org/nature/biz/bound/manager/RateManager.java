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
    public List<Rate> list(String rule, String date) {
        // 查询规则数据
        List<Rule> rules = ruleMapper.listAll().stream()
                .filter(i -> "1".equals(i.getStatus()) && (rule == null || rule.equals(i.getCode())))
                .collect(Collectors.toList());
        if (rules.isEmpty()) {
            return new ArrayList<>();
        }
        // 规则转换为 map
        Map<String, Rule> ruleMap = rules.stream().collect(Collectors.toMap(Rule::getCode, i -> i));
        // 查询规则包含的项目集合
        List<Item> items = itemMapper.listByRules(new ArrayList<>(ruleMap.keySet()));
        if (items.isEmpty()) {
            return new ArrayList<>();
        }
        // 计算价格取数日期
        String priceDate = this.priceDate(date);
        // 计算净值取数日期
        Map<String, String> dateMap = this.netDateMap(rules, priceDate);
        // 净值数据map
        Map<String, BigDecimal> netMap = this.netMap(items, dateMap);
        // 价格数据map
        Map<String, BigDecimal> priceMap = this.priceMap(items, priceDate);
        // 结果数据转换输出
        return items.stream().map(i -> this.buildRate(i, priceDate, ruleMap, dateMap, netMap, priceMap))
                .sorted(Comparator.comparing(Rate::getRatio)).collect(Collectors.toList());
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
        return nets.stream().collect(Collectors.toMap(this::netKey, Net::getNet, (o, n) -> o));
    }

    private String netKey(Net i) {
        return i.getCode() + ":" + i.getDate();
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
     * @param item      项目
     * @param datePrice 价格日期
     * @param ruleMap   规则map
     * @param dateMap   日期map
     * @param netMap    净值map
     * @param priceMap  价格map
     * @return Rate
     */
    private Rate buildRate(Item item, String datePrice, Map<String, Rule> ruleMap, Map<String, String> dateMap,
                           Map<String, BigDecimal> netMap, Map<String, BigDecimal> priceMap) {
        Rate rate = new Rate();
        Rule rule = ruleMap.get(item.getRule());
        rate.setRuleCode(rule.getCode());
        rate.setRuleName(rule.getName());
        String code = item.getCode();
        String name = item.getName();
        String type = item.getType();
        String fund = item.getFund();
        String ruleCode = item.getRule();
        String priceKey = code + ":" + type;
        BigDecimal price = priceMap.get(priceKey);
        Warn.check(() -> price == null, "价格获取失败:" + priceKey);
        String dateNet = dateMap.get(ruleCode);
        String netKey = fund + ":" + dateNet;
        BigDecimal net = netMap.get(netKey);
        Warn.check(() -> net == null, "净值获取失败:" + netKey);
        Warn.check(() -> BigDecimal.ZERO.compareTo(net) == 0, "净值为0:" + netKey);
        rate.setEtfCode(code);
        rate.setEtfName(name);
        rate.setFundCode(fund);
        rate.setDateNet(dateNet);
        rate.setDatePrice(datePrice);
        rate.setPrice(price);
        rate.setNet(net);
        // 涨幅=（价格-净值）x系数÷净值
        rate.setRatio(rate.getPrice().subtract(net).multiply(item.getRatio()).divide(net, SCALE, RoundingMode.HALF_UP));
        return rate;
    }

}
