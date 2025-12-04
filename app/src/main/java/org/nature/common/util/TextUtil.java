package org.nature.common.util;

import android.annotation.SuppressLint;
import android.widget.EditText;
import org.apache.commons.lang3.math.NumberUtils;
import org.nature.common.exception.Warn;

import java.math.BigDecimal;
import java.util.function.Function;

/**
 * 文本工具类
 * @author Nature
 * @version 1.0.0
 * @since 2024/2/3
 */
@SuppressLint("DefaultLocale")
public class TextUtil {

    /**
     * 将对象转换为字符串表示
     * @param o 需要转换的对象
     * @return String 转换后的字符串
     */
    public static String text(Object o) {
        return TextUtil.text(o, Object::toString);
    }

    /**
     * 格式化金额数值为带单位的字符串（万/亿/万亿）
     * @param o 金额数值
     * @return String 格式化后的金额字符串
     */
    public static String amount(Double o) {
        return TextUtil.text(o, i -> {
            if (Math.abs(i) < 10000) {
                return String.format("%.2f", i);
            }
            if (Math.abs(i) < 100000000) {
                return String.format("%.2f万", i / 10000d);
            }
            if (Math.abs(i) < 1000000000000d) {
                return String.format("%.4f亿", i / 10000d / 10000d);
            }
            return String.format("%.4f万亿", i / 10000d / 10000d / 10000d);
        });
    }

    /**
     * 将BigDecimal金额格式化为带单位的字符串
     * @param o BigDecimal类型的金额
     * @return String 格式化后的金额字符串
     */
    public static String amount(BigDecimal o) {
        return TextUtil.text(o, i -> TextUtil.amount(i.doubleValue()));
    }

    /**
     * 将小数转换为百分比格式的字符串
     * @param o 小数值
     * @return String 百分比格式的字符串
     */
    public static String hundred(Double o) {
        if (o == null) {
            return "";
        }
        return TextUtil.text(o, i -> String.format("%.2f%%", i * 100d));
    }

    /**
     * 将BigDecimal小数转换为百分比格式的字符串
     * @param o BigDecimal类型的小数
     * @return String 百分比格式的字符串
     */
    public static String hundred(BigDecimal o) {
        return TextUtil.text(o, i -> TextUtil.hundred(i.doubleValue()));
    }

    /**
     * 将数值转换为百分比格式的字符串
     * @param o 数值
     * @return String 百分比格式的字符串
     */
    public static String percent(Double o) {
        return TextUtil.text(o, i -> String.format("%.2f%%", i));
    }

    /**
     * 将BigDecimal数值转换为百分比格式的字符串
     * @param o BigDecimal类型的数值
     * @return String 百分比格式的字符串
     */
    public static String percent(BigDecimal o) {
        return TextUtil.text(o, i -> TextUtil.percent(i.doubleValue()));
    }

    /**
     * 格式化价格数值为三位小数字符串
     * @param o 价格数值
     * @return String 格式化后的价格字符串
     */
    public static String price(Double o) {
        return TextUtil.text(o, i -> String.format("%.3f", i));
    }

    /**
     * 将BigDecimal价格格式化为三位小数字符串
     * @param o BigDecimal类型的价格
     * @return String 格式化后的价格字符串
     */
    public static String price(BigDecimal o) {
        return TextUtil.text(o, i -> TextUtil.price(i.doubleValue()));
    }

    /**
     * 格式化网络数值为四位小数字符串
     * @param o 网络数值
     * @return String 格式化后的网络字符串
     */
    public static String net(Double o) {
        return TextUtil.text(o, i -> String.format("%.4f", i));
    }

    public static BigDecimal decimal(String s) {
        if (s == null || s.isEmpty() || s.equals("-") || s.equals("---")) {
            return null;
        }
        if (s.endsWith("%")) {
            s = s.replace("%", "");
        }
        return new BigDecimal(s);
    }


    public static String join(String... arr) {
        return String.join(":", arr);
    }

    /**
     * 转化为文本
     * @param t    对象
     * @param func 函数
     * @return String
     */
    private static <T> String text(T t, Function<T, String> func) {
        if (t == null) {
            return "";
        }
        return func.apply(t);
    }
}
