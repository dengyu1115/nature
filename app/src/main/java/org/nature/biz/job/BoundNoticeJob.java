package org.nature.biz.job;

import org.nature.biz.http.KlineHttp;
import org.nature.biz.http.NetHttp;
import org.nature.biz.model.Kline;
import org.nature.common.constant.Const;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.util.DateUtil;
import org.nature.common.util.Md5Util;
import org.nature.common.util.NotifyUtil;
import org.nature.func.job.protocol.Job;
import org.nature.func.workday.manager.WorkdayManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 债券差价提醒
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@JobExec(code = "bound_notice_job", name = "债券差价提醒")
public class BoundNoticeJob implements Job {

    /**
     * code-名称map
     */
    public static final Map<String, String> NAME_MAP = Map.of(
            "159649", "国开债ETF",
            "159650", "国开ETF",
            "159651", "国开债券ETF"
    );
    /**
     * 品种集合
     */
    public static final List<String> LIST = NAME_MAP.keySet().stream().sorted().collect(Collectors.toList());
    /**
     * 最新净值记录map
     */
    private static final Map<String, Map<String, BigDecimal>> NET_MAP = new HashMap<>();
    /**
     * 最新操作数据map
     */
    private static final Map<String, Map<String, BigDecimal>> HANDLE_MAP = new HashMap<>();
    /**
     * 已通知数据map
     */
    private static final Map<String, Set<String>> NOTICE_MAP = new HashMap<>();
    /**
     * 差值比例标准
     */
    private static final BigDecimal DIFF = new BigDecimal("0.0005");

    public static final BigDecimal HUNDRED = new BigDecimal("100");

    private static boolean running;

    @Injection
    private WorkdayManager workdayManager;
    @Injection
    private NetHttp netHttp;
    @Injection
    private KlineHttp klineHttp;


    @Override
    public void exec(Date date) {
        this.doExec(() -> {
            // 执行延时1秒以上废弃任务
            if (System.currentTimeMillis() - date.getTime() > 1000) {
                return;
            }
            // 非工作日不处理
            if (!workdayManager.isWorkday()) {
                return;
            }
            this.boundHandle();
        });
    }

    /**
     * 债券处理
     */
    private void boundHandle() {
        String today = DateUtil.today();
        Map<String, BigDecimal> netMap = this.getNetMap(today);
        // 查询最新K线值，计算涨跌幅
        Map<String, BigDecimal> ratioMap = this.getRatioMap(today, netMap);
        // 已A3_2的排列计算价格差，满足价差的提醒
        this.calcHandle(today, ratioMap);
        // 通知
        this.notice(today);
        // 删除过期数据
        this.deleteExpired(today);
    }

    /**
     * 获取净值数据map
     * @param date 日期
     * @return map
     */
    private Map<String, BigDecimal> getNetMap(String date) {
        // 取出当天数据
        Map<String, BigDecimal> netMap = NET_MAP.get(date);
        // 当天没数据则查询初始化
        if (netMap == null) {
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
     * 获取涨跌幅map
     * @param date   日期
     * @param netMap 净值map
     * @return map
     */
    private Map<String, BigDecimal> getRatioMap(String date, Map<String, BigDecimal> netMap) {
        return LIST.parallelStream().map(i -> {
            List<Kline> ks = klineHttp.list(i, "0", date, date);
            if (ks.isEmpty()) {
                throw new Warn("K线获取失败:" + i);
            }
            return ks.get(0);
        }).collect(Collectors.toMap(Kline::getCode, i -> this.calcRatio(netMap, i)));
    }

    /**
     * 计算操作数据
     * @param date     日期
     * @param ratioMap 涨跌幅map
     */
    private void calcHandle(String date, Map<String, BigDecimal> ratioMap) {
        Map<String, BigDecimal> handleMap = HANDLE_MAP.computeIfAbsent(date, k -> new HashMap<>());
        // 排列组合数据计算差价是否满足通知条件
        LIST.forEach(m -> {
            List<String> list = new ArrayList<>(LIST);
            list.remove(m);
            list.forEach(n -> {
                String key = String.join(Const.DELIMITER, m, n);
                BigDecimal v = handleMap.get(key);
                if (v == null) {
                    v = DIFF;
                }
                BigDecimal ratio = ratioMap.get(m);
                if (ratio != null && ratio.compareTo(v) >= 0) {
                    handleMap.put(key, v);
                }
            });
        });
    }

    /**
     * 进行通知操作
     * @param date 日期
     */
    private void notice(String date) {
        // 已通知数据set
        Set<String> set = NOTICE_MAP.computeIfAbsent(date, k -> new HashSet<>());
        // 操作数据
        Map<String, BigDecimal> handleMap = HANDLE_MAP.get(date);
        if (handleMap == null) {
            return;
        }
        // 记录需要通知的文案集合
        List<String> list = new ArrayList<>();
        // 遍历操作数据，进行通知操作
        Comparator<P> comparator = Comparator.comparing(i -> i.v);
        Comparator<P> reversed = comparator.reversed();
        handleMap.entrySet().stream().map(i -> {
            String[] keys = i.getKey().split(Const.DELIMITER);
            String m = keys[0];
            String n = keys[1];
            return new P(m, n, i.getValue());
        }).sorted(reversed).forEach(i -> {
            String key = Md5Util.md5(i.m, i.n, i.v.toPlainString());
            // 如果已经通知过，则跳过
            if (set.contains(key)) {
                return;
            }
            set.add(key);
            list.add(NAME_MAP.get(i.m) + "和" + NAME_MAP.get(i.n) + "相差" + i.v.multiply(HUNDRED) + "%");
        });
        if (list.isEmpty()) {
            return;
        }
        // 通知
        String text = "出现机会" + String.join("，", list) + "。";
        NotifyUtil.speak(text);
        NotifyUtil.notifyOne("债券差价", text);
    }

    /**
     * 计算涨幅
     * @param netMap 净值map
     * @param i      K线数据
     * @return BigDecimal
     */
    private BigDecimal calcRatio(Map<String, BigDecimal> netMap, Kline i) {
        BigDecimal base = netMap.get(i.getCode());
        return i.getLatest().subtract(base).divide(base, 4, RoundingMode.HALF_UP);
    }

    /**
     * 删除过期数据
     * @param date 日期
     */
    private void deleteExpired(String date) {
        List<Map<String, ?>> list = List.of(NET_MAP, NOTICE_MAP, HANDLE_MAP);
        for (Map<String, ?> map : list) {
            Set<String> set = new HashSet<>(map.keySet());
            for (String s : set) {
                if (!date.equals(s)) {
                    map.remove(s);
                }
            }
        }
    }

    /**
     * 串行执行
     * @param runnable 执行逻辑
     */
    private void doExec(Runnable runnable) {
        synchronized (this) {
            // 已经有执行中任务，不再执行
            if (running) {
                return;
            }
            // 标记正在执行
            running = true;
        }
        // 执行任务
        try {
            runnable.run();
        } finally {
            // 执行完毕，标记为未执行
            running = false;
        }
    }

    private static class P {
        private final String m;
        private final String n;
        private final BigDecimal v;

        public P(String m, String n, BigDecimal v) {
            this.m = m;
            this.n = n;
            this.v = v;
        }
    }

}
