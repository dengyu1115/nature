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

    public static String text(Object o) {
        return TextUtil.text(o, Object::toString);
    }

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

    public static String amount(BigDecimal o) {
        return TextUtil.text(o, i -> TextUtil.amount(i.doubleValue()));
    }

    public static String hundred(Double o) {
        if (o == null) {
            return "";
        }
        return TextUtil.text(o, i -> String.format("%.2f%%", i * 100d));
    }

    public static String hundred(BigDecimal o) {
        return TextUtil.text(o, i -> TextUtil.hundred(i.doubleValue()));
    }

    public static String percent(Double o) {
        return TextUtil.text(o, i -> String.format("%.2f%%", i));
    }

    public static String percent(BigDecimal o) {
        return TextUtil.text(o, i -> TextUtil.percent(i.doubleValue()));
    }

    public static String price(Double o) {
        return TextUtil.text(o, i -> String.format("%.3f", i));
    }

    public static String price(BigDecimal o) {
        return TextUtil.text(o, i -> TextUtil.price(i.doubleValue()));
    }

    public static String net(Double o) {
        return TextUtil.text(o, i -> String.format("%.4f", i));
    }

    public static Double getDouble(String s) {
        if (s == null || s.isEmpty() || s.equals("-") || s.equals("---")) {
            return null;
        }
        if (s.endsWith("%")) {
            s = s.replace("%", "");
        }
        return Double.valueOf(s);
    }

    public static BigDecimal getDecimal(String s) {
        if (s == null || s.isEmpty() || s.equals("-") || s.equals("---")) {
            return null;
        }
        if (s.endsWith("%")) {
            s = s.replace("%", "");
        }
        return new BigDecimal(s);
    }

    public static String getString(EditText et) {
        return et.getText().toString().trim();
    }

    public static BigDecimal getDecimal(EditText et) {
        String val = et.getText().toString().trim();
        if (val.isEmpty()) {
            return null;
        }
        if (!NumberUtils.isCreatable(val)) {
            throw new Warn("decimal format error:" + val);
        }
        return new BigDecimal(val);
    }

    public static Integer getInteger(EditText et) {
        String val = et.getText().toString().trim();
        if (val.isEmpty()) {
            return null;
        }
        if (!NumberUtils.isDigits(val)) {
            throw new Warn("number format error:" + val);
        }
        return Integer.valueOf(val);
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
