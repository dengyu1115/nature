package org.nature.biz.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.util.HttpUtil;

import java.math.BigDecimal;
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


    private static final String URL_NET = "http://api.fund.eastmoney.com/f10/lsjz?fundCode=%s&pageIndex=1&pageSize=1" +
            "&startDate=%s&endDate=%s";
    public static final Map<String, String> HEADER = Map.of("Referer", "http://fundf10.eastmoney.com/");

    /**
     * 查询单位净值
     * @param code 基金code
     * @param date 日期
     * @return BigDecimal
     */
    public BigDecimal getNetValue(String code, String date) {
        date = new StringBuilder(date).insert(4, "-").insert(7, "-").toString();
        String url = String.format(URL_NET, code, date, date);
        String response = HttpUtil.doGet(url, HEADER, lines -> lines.collect(Collectors.toList()).get(0));
        JSONObject json = JSON.parseObject(response);
        // 获取所需字段
        JSONObject data = json.getJSONObject("Data");
        if (data == null) {
            throw new RuntimeException("历史净值数据缺失：" + code);
        }
        JSONArray list = data.getJSONArray("LSJZList");
        if (list == null || list.isEmpty()) {
            throw new RuntimeException("历史净值数据缺失：" + code);
        }
        // 解析响应，获取净值
        return list.getJSONObject(0).getBigDecimal("DWJZ");
    }

}
