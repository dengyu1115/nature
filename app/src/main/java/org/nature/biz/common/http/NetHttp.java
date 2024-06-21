package org.nature.biz.common.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nature.biz.common.model.Net;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.util.HttpUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 净值
 * @author Nature
 * @version 1.0.0
 * @since 2024/3/15
 */
@Component
public class NetHttp {

    private static final String URL_NET_LIST = "http://api.fund.eastmoney.com/f10/lsjz?fundCode=%s&pageIndex=1&pageSize=100000" +
            "&startDate=%s&endDate=%s";
    public static final Map<String, String> HEADER = Map.of("Referer", "http://fundf10.eastmoney.com/");


    /**
     * 查询单位净值
     * @param code      基金code
     * @param dateStart 开始日期
     * @param dateEnd   结束日期
     * @return BigDecimal
     */
    public List<Net> list(String code, String dateStart, String dateEnd) {
        dateStart = this.format(dateStart);
        dateEnd = this.format(dateEnd);
        String url = String.format(URL_NET_LIST, code, dateStart, dateEnd);
        String response = HttpUtil.doGet(url, HEADER, lines -> lines.collect(Collectors.toList()).get(0));
        JSONObject json = JSON.parseObject(response);
        // 获取所需字段
        JSONObject data = json.getJSONObject("Data");
        if (data == null) {
            throw new Warn("历史净值数据缺失：" + code);
        }
        JSONArray list = data.getJSONArray("LSJZList");
        if (list == null) {
            throw new Warn("历史净值数据缺失：" + code + ":" + dateStart + ":" + dateEnd);
        }
        List<Net> results = new ArrayList<>();
        BigDecimal ldw = this.latestNet(list, "DWJZ");
        BigDecimal llj = this.latestNet(list, "LJJZ");
        for (Object i : list) {
            JSONObject o = (JSONObject) i;
            Net net = new Net();
            net.setCode(code);
            net.setDate(o.getString("FSRQ").replace("-", ""));
            net.setDw(o.getBigDecimal("DWJZ"));
            BigDecimal lj = o.getBigDecimal("LJJZ");
            net.setLj(lj);
            net.setNet(this.calcNet(ldw, llj, lj));
            results.add(net);
        }
        // 解析响应，获取净值
        return results;
    }

    /**
     * 日期格式化
     * @param date 日期
     * @return String
     */
    private String format(String date) {
        if (date == null || date.isEmpty()) {
            return "";
        }
        return new StringBuilder(date).insert(4, "-").insert(7, "-").toString();
    }

    /**
     * 获取最新单位净值
     * @param list 数据集
     * @param key  key
     * @return BigDecimal
     */
    private BigDecimal latestNet(JSONArray list, String key) {
        if (list.isEmpty()) {
            return null;
        }
        return list.getJSONObject(0).getBigDecimal(key);
    }

    /**
     * 复权计算净值
     * @param ldw 最新单位净值
     * @param llj 最新累计净值
     * @param lj  当前累计净值
     * @return BigDecimal
     */
    private BigDecimal calcNet(BigDecimal ldw, BigDecimal llj, BigDecimal lj) {
        if (ldw == null) {
            throw new Warn("最新单位净值为null");
        }
        if (llj == null || llj.compareTo(BigDecimal.ZERO) == 0) {
            throw new Warn("最新累计净值为null或者0");
        }
        if (lj == null) {
            throw new Warn("累计净值为null");
        }
        return ldw.divide(llj, 8, RoundingMode.HALF_UP).multiply(lj).setScale(4, RoundingMode.HALF_UP);
    }

}
