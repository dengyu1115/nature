package org.nature.biz.manager;

import org.nature.biz.http.KlineHttp;
import org.nature.biz.http.NetHttp;
import org.nature.biz.mapper.BoundMapper;
import org.nature.biz.model.BoundRate;
import org.nature.biz.model.Kline;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.DateUtil;
import org.nature.func.workday.manager.WorkdayManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static org.nature.biz.memory.BoundMemory.LIST;
import static org.nature.biz.memory.BoundMemory.NAME_MAP;

/**
 * 债券
 * @author Nature
 * @version 1.0.0
 * @since 2024/3/18
 */
@Component
public class BoundManager {

    /**
     * 最新净值记录map
     */
    private static final Map<String, Map<String, BigDecimal>> NET_MAP = new HashMap<>();

    public static final int SCALE = 8;
    public static final String EMPTY = "";
    @Injection
    private NetHttp netHttp;
    @Injection
    private KlineHttp klineHttp;
    @Injection
    private BoundMapper boundMapper;
    @Injection
    private WorkdayManager workdayManager;

    /**
     * 查询涨幅数据
     * @return list
     */
    public List<BoundRate> listRatio() {
        String today = DateUtil.today();
        Map<String, BigDecimal> netMap = this.getNetMap(today);
        return LIST.parallelStream().map(i -> new BoundRate(i, NAME_MAP.get(i), EMPTY, EMPTY, this.calcRatio(i, today, netMap)))
                .sorted(Comparator.comparing(BoundRate::getRatio)).collect(Collectors.toList());
    }

    /**
     * 查询涨幅对比数据
     * @return list
     */
    public List<BoundRate> listCompare() {
        String today = DateUtil.today();
        Map<String, BigDecimal> netMap = this.getNetMap(today);
        Map<String, BigDecimal> ratioMap = LIST.parallelStream()
                .collect(Collectors.toMap(i -> i, i -> this.calcRatio(i, today, netMap)));
        return LIST.stream().map(i -> {
                    BigDecimal mv = ratioMap.get(i);
                    if (mv == null) {
                        return null;
                    }
                    return LIST.stream().filter(j -> !i.equals(j)).map(j -> {
                        BigDecimal nv = ratioMap.get(j);
                        if (nv == null) {
                            return null;
                        }
                        return new BoundRate(i, NAME_MAP.get(i), j, NAME_MAP.get(j), mv.subtract(nv));
                    }).filter(Objects::nonNull).collect(Collectors.toList());
                }).filter(Objects::nonNull).flatMap(List::stream).sorted(Comparator.comparing(BoundRate::getRatio))
                .collect(Collectors.toList());
    }

    /**
     * 获取净值map
     * @param date 日期
     * @return map
     */
    public Map<String, BigDecimal> getNetMap(String date) {
        Map<String, BigDecimal> netMap = NET_MAP.get(date);
        // 当天没数据则查询初始化
        if (netMap == null) {
            // 清空
            NET_MAP.clear();
            // 查询最新净值
            String lastWorkday = workdayManager.lastWorkday(date);
            netMap = LIST.parallelStream().collect(Collectors.toMap(i -> i, i -> {
                BigDecimal value = netHttp.getNetValue(i, lastWorkday);
                if (value == null) {
                    throw new Warn("净值获取失败:" + i);
                }
                return value;
            }));
            NET_MAP.put(date, netMap);
        }
        return netMap;
    }

    /**
     * 计算涨幅
     * @param code   代码
     * @param date   日期
     * @param netMap 净值map
     * @return BigDecimal
     */
    private BigDecimal calcRatio(String code, String date, Map<String, BigDecimal> netMap) {
        BigDecimal net = netMap.get(code);
        if (net == null) {
            throw new Warn("获取净值数据失败：" + code);
        }
        List<Kline> list = klineHttp.list(code, "0", date, date);
        if (list.isEmpty()) {
            throw new Warn("获取K线数据失败：" + code);
        }
        BigDecimal latest = list.get(0).getLatest();
        if (latest == null) {
            throw new Warn("获取K线数据失败：" + code + " 最新价为空");
        }
        return latest.subtract(net).divide(net, SCALE, RoundingMode.HALF_UP);
    }

}
