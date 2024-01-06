package org.nature.common.util;

import java.util.List;
import java.util.function.Function;

/**
 * MA值计算工具类
 * @author nature
 * @version 1.0.0
 * @since 2019/11/6 23:14
 */
public class MaUtil {

    private static final int DAY = 1, WEEK = 7, MONTH = 30, SEASON = 90, YEAR = 360;

    /**
     * 计算MA值回归比率
     * @param data     data
     * @param len      比对方
     * @param total    对比方
     * @param function function
     * @param <T>      T
     * @return double
     */
    public static <T> double backRate(List<T> data, int total, int len, Function<T, Double> function) {
        int size = data.size();
        if (total < len) throw new RuntimeException("total is little than " + len);
        if (size < total) throw new RuntimeException("size is little than " + total);
        double av = average(data.subList(size - len, size), function);
        double bv = average(data.subList(size - total, size), function);
        return (av - bv) / bv;
    }

    /**
     * 计算MA值回归比率
     * @param data     data
     * @param len      MA范围
     * @param function function
     * @param <T>      T
     * @return double
     */
    public static <T> double backRate(List<T> data, int len, Function<T, Double> function) {
        int size = data.size();
        if (size < len) throw new RuntimeException("size is little than " + len);
        double av = function.apply(data.get(size - 1));
        double bv = average(data.subList(size - len, size), function);
        return (av - bv) / bv;
    }

    /**
     * 计算MA增长比率
     * @param data     data
     * @param total    取数总长
     * @param len      ma长度
     * @param function function
     * @param <T>      T
     * @return double
     */
    public static <T> double riseRate(List<T> data, int total, int len, Function<T, Double> function) {
        int size = data.size();
        if (size < total) throw new RuntimeException("size is little than " + total);
        if (total < len) throw new RuntimeException("total is little than " + len);
        List<T> tempList = data.subList(size - total, size);
        double av = average(tempList.subList(total - len, total), function);
        double bv = average(tempList.subList(0, len), function);
        return (av - bv) / bv;
    }

    /**
     * 平均值计算
     * @param data     data
     * @param function function
     * @param <T>      T
     * @return double
     */
    public static <T> double average(List<T> data, Function<T, Double> function) {
        double total = 0;
        for (T datum : data) total += function.apply(datum);
        return total / data.size();
    }

}
