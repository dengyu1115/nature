package org.nature.common.util;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.nature.common.constant.Const;

import java.text.ParseException;
import java.util.Date;

public class DateUtil {

    public static Date parseDate(String date) {
        return parse(date, Const.FORMAT_DATE);
    }

    public static Date parse(String date, String format) {
        try {
            return DateUtils.parseDate(date, format);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String today() {
        return DateFormatUtils.format(new Date(), Const.FORMAT_DAY);
    }

    public static String now() {
        return DateFormatUtils.format(new Date(), Const.FORMAT_DATETIME);
    }

    public static String addDays(String date, int days) {
        Date parseDate = parse(date, Const.FORMAT_DAY);
        Date resultDate = DateUtils.addDays(parseDate, days);
        return DateFormatUtils.format(resultDate, Const.FORMAT_DAY);
    }

    public static String addWeeks(String date, int weeks) {
        Date parseDate = parse(date, Const.FORMAT_DATE);
        Date resultDate = DateUtils.addWeeks(parseDate, weeks);
        return DateFormatUtils.format(resultDate, Const.FORMAT_DATE);
    }

    public static String addMonths(String date, int months) {
        Date parseDate = parse(date, Const.FORMAT_DATE);
        Date resultDate = DateUtils.addMonths(parseDate, months);
        return DateFormatUtils.format(resultDate, Const.FORMAT_DATE);

    }

    public static String addYears(String date, int years) {
        Date parseDate = parse(date, Const.FORMAT_DATE);
        Date resultDate = DateUtils.addYears(parseDate, years);
        return DateFormatUtils.format(resultDate, Const.FORMAT_DATE);
    }

    public static String formatDay(Date date) {
        return DateFormatUtils.format(date, Const.FORMAT_DAY);
    }

}