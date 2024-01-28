package org.nature.biz.util;

import org.nature.biz.model.Kline;
import org.nature.biz.model.KlineView;
import org.nature.common.constant.Const;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * K线处理工具类
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/28
 */
public class KlineUtil {


    /**
     * 转换K线数据
     * @param list 数据集
     * @param name 名称
     * @return list
     */
    public static List<KlineView> convert(List<Kline> list, String name) {
        List<KlineView> viewList = new ArrayList<>();
        List<Double> l5 = new LinkedList<>();
        List<Double> l10 = new LinkedList<>();
        List<Double> l20 = new LinkedList<>();
        List<Double> l60 = new LinkedList<>();
        Double last = null, first = null;
        for (Kline k : list) {
            KlineView view = new KlineView();
            view.setName(name);
            view.setDate(k.getDate());
            double open = k.getOpen().doubleValue();
            double latest = k.getLatest().doubleValue();
            double high = k.getHigh().doubleValue();
            double low = k.getLow().doubleValue();
            if (last == null) {
                last = open;
                first = open;
            }
            view.setOpen(open);
            view.setLatest(latest);
            view.setHigh(high);
            view.setLow(low);
            // 涨幅设置
            view.setRatioDiff((high - low) / low);
            view.setRatioInc((latest - last) / last);
            view.setRatioTotal((latest - first) / first);
            last = latest;
            view.setAmount(k.getAmount().doubleValue());
            view.setShare(k.getShare().doubleValue());
            // 均线值设置
            view.setMa5(KlineUtil.addValue(l5, 5, latest));
            view.setMa10(KlineUtil.addValue(l10, 10, latest));
            view.setMa20(KlineUtil.addValue(l20, 20, latest));
            view.setMa60(KlineUtil.addValue(l60, 60, latest));
            viewList.add(view);
        }
        return viewList;
    }

    /**
     * 按规则分组K线数据并聚合转换
     * @param list K线数据集合
     * @param func 转换逻辑
     * @return list
     */
    public static List<Kline> convert(List<Kline> list, Function<LocalDate, Integer> func) {
        List<List<Kline>> ll = new ArrayList<>();
        List<Kline> l = null;
        Integer val = null;
        for (Kline i : list) {
            String date = i.getDate();
            LocalDate d = LocalDate.parse(date, DateTimeFormatter.ofPattern(Const.FORMAT_DAY));
            int currVal = func.apply(d);
            // 比较新旧值不等说明需要切换分组了
            if (val == null || !val.equals(currVal)) {
                l = new ArrayList<>();
                ll.add(l);
                val = currVal;
            }
            l.add(i);
        }
        return ll.stream().map(KlineUtil::toKline).collect(Collectors.toList());
    }

    /**
     * 分组数据转换为K线对象
     * @param list 分组K线数据集合
     * @return Kline
     */
    private static Kline toKline(List<Kline> list) {
        Kline kline = new Kline();
        for (Kline i : list) {
            kline.setCode(i.getCode());
            kline.setType(i.getType());
            kline.setDate(i.getDate());
            kline.setLatest(i.getLatest());
            // 设置开盘价
            if (kline.getOpen() == null) {
                kline.setOpen(i.getOpen());
            }
            //设置最高价
            if (kline.getHigh() == null || kline.getHigh().compareTo(i.getHigh()) < 0) {
                kline.setHigh(i.getHigh());
            }
            //设置最低价
            if (kline.getLow() == null || kline.getLow().compareTo(i.getLow()) > 0) {
                kline.setLow(i.getLow());
            }
            //设置交易份额
            BigDecimal share = kline.getShare();
            if (share == null) {
                kline.setShare(i.getShare());
            } else {
                kline.setShare(share.add(i.getShare()));
            }
            //设置交易金额
            BigDecimal amount = kline.getAmount();
            if (amount == null) {
                kline.setAmount(i.getAmount());
            } else {
                kline.setAmount(amount.add(i.getAmount()));
            }
        }
        return kline;
    }

    /**
     * 添加数值
     * @param list  集合
     * @param size  控制大小
     * @param value 数值
     */
    private static Double addValue(List<Double> list, int size, double value) {
        if (list.size() == size) {
            list.remove(0);
        }
        list.add(value);
        if (list.size() == size) {
            return list.stream().mapToDouble(a -> a).average().orElse(0.0);
        }
        return null;
    }

}
