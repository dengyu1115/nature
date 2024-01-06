package org.nature.common.util;

import android.annotation.SuppressLint;

import java.math.BigDecimal;

@SuppressLint("DefaultLocale")
public class TextUtil {

    public static String text(Object o) {
        if (o == null) return "";
        return o.toString();
    }

    public static String amount(Double o) {
        if (o == null) return "";
        else if (Math.abs(o) < 10000) return String.format("%.2f", o);
        else if (Math.abs(o) < 100000000) return String.format("%.2f万", o / 10000d);
        else if (Math.abs(o) < 1000000000000d) return String.format("%.4f亿", o / 10000d / 10000d);
        else return String.format("%.4f万亿", o / 10000d / 10000d / 10000d);
    }

    public static String amount(BigDecimal o) {
        if (o == null) return "";
        return amount(o.doubleValue());
    }

    public static String hundred(Double o) {
        if (o == null) return "";
        return String.format("%.2f%%", o * 100d);
    }

    public static String hundred(BigDecimal o) {
        if (o == null) return "";
        return hundred(o.doubleValue());
    }

    public static String percent(Double o) {
        if (o == null) return "";
        return String.format("%.2f%%", o);
    }

    public static String percent(BigDecimal o) {
        if (o == null) return "";
        return percent(o);
    }

    public static String price(Double o) {
        if (o == null) return "";
        return String.format("%.3f", o);
    }

    public static String price(BigDecimal o) {
        if (o == null) return "";
        return price(o.doubleValue());
    }

    public static String net(Double o) {
        if (o == null) return "";
        return String.format("%.4f", o);
    }

    public static Double getDouble(String s) {
        if (s == null || s.isEmpty() || s.equals("-") || s.equals("---")) return null;
        if (s.endsWith("%")) s = s.replace("%", "");
        return Double.valueOf(s);
    }

    public static BigDecimal getDecimal(String s) {
        if (s == null || s.isEmpty() || s.equals("-") || s.equals("---")) return null;
        if (s.endsWith("%")) s = s.replace("%", "");
        return new BigDecimal(s);
    }
}
