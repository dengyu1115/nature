package org.nature.common.util;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.nature.common.constant.Const;

import java.text.ParseException;
import java.util.Date;
import java.util.function.BiFunction;

/**
 * 日期工具类
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/20
 */
public class DateUtil {

    /**
     * 转化日期
     * @param date   日期字符串
     * @param format 日期格式
     * @return Date
     */
    public static Date parse(String date, String format) {
        try {
            return DateUtils.parseDate(date, format);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 格式化日期
     * @param date 日期
     * @return String
     */
    public static String formatDay(Date date) {
        return DateFormatUtils.format(date, Const.FORMAT_DAY);
    }

    /**
     * 格式化日期
     * @param date   日期
     * @param format 日期格式
     * @return String
     */
    public static String format(Date date, String format) {
        return DateFormatUtils.format(date, format);
    }

    /**
     * 获取当前日期
     * @return String
     */
    public static String today() {
        return DateFormatUtils.format(new Date(), Const.FORMAT_DAY);
    }

    /**
     * 获取当前时间
     * @return String
     */
    public static String now() {
        return DateFormatUtils.format(new Date(), Const.FORMAT_DATETIME);
    }

    /**
     * 获取当前时间
     * @return String
     */
    public static String nowTime() {
        return DateFormatUtils.format(new Date(), Const.FORMAT_TIME);
    }

    /**
     * 增加日
     * @param date 日期
     * @param num  数量
     * @return String
     */
    public static String addDays(String date, int num) {
        return DateUtil.addDate(date, num, DateUtils::addDays);
    }

    /**
     * 增加日
     * @param date 日期
     * @param num  数量
     * @return String
     */
    public static String addWeeks(String date, int num) {
        return DateUtil.addDate(date, num, DateUtils::addWeeks);
    }

    /**
     * 增加日
     * @param date 日期
     * @param num  数量
     * @return String
     */
    public static String addMonths(String date, int num) {
        return DateUtil.addDate(date, num, DateUtils::addMonths);
    }

    /**
     * 增加日
     * @param date 日期
     * @param num  数量
     * @return String
     */
    public static String addYears(String date, int num) {
        return DateUtil.addDate(date, num, DateUtils::addYears);
    }

    /**
     * 增加日期
     * @param date 日期
     * @param num  数量
     * @param func 增加函数
     * @return String
     */
    private static String addDate(String date, int num, BiFunction<Date, Integer, Date> func) {
        Date parseDate = parse(date, Const.FORMAT_DAY);
        Date resultDate = func.apply(parseDate, num);
        return DateFormatUtils.format(resultDate, Const.FORMAT_DAY);
    }

}