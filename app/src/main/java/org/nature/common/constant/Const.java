package org.nature.common.constant;

import android.graphics.Color;

import java.math.BigDecimal;

/**
 * 常量
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
public interface Const {
    /**
     * 分隔符
     */
    String DELIMITER = ":";
    /**
     * 空字符串
     */
    String EMPTY = "";
    /**
     * 符号：-
     */
    String HYPHEN = "-";
    /**
     * CHARSET
     */
    String UTF_8 = "utf8";
    /**
     * 总计
     */
    String TOTAL = "总计";
    /**
     * 小数点保留位数
     */
    int SCALE = 4;
    /**
     * 格式化：年
     */
    String FORMAT_YEAR = "yyyy";
    /**
     * 格式化：年月日
     */
    String FORMAT_DAY = "yyyyMMdd";
    /**
     * 格式化：年-月-日
     */
    String FORMAT_DATE = "yyyy-MM-dd";
    /**
     * 格式化：月-日
     */
    String FORMAT_MONTH_DAY = "MM-dd";
    /**
     * 格式化：年月日时分秒
     */
    String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";
    /**
     * 格式化：时分秒
     */
    String FORMAT_TIME = "HH:mm:ss";
    /**
     * 背景颜色
     */
    int BG_COLOR = Color.parseColor("#ff99cc00");
    /**
     * 间隔、行宽、行宽-标题、行宽-内容、行高
     */
    int PAGE_WIDTH = 2188, PAGE_HEIGHT = 1080, PAD = 5, L_W = 25, L_W_T = 10, L_W_C = 14, L_H = 7;
    /**
     * 小数位数
     */
    int SCALE_PRICE = 3;
    /**
     * 小数位数
     */
    int SCALE_PROFIT = SCALE_PRICE + 1;
    /**
     * 百
     */
    BigDecimal HUNDRED = new BigDecimal("100");

}
