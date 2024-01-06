package org.nature.biz.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nature.biz.model.Kline;
import org.nature.common.constant.Const;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.util.HttpUtil;
import org.nature.common.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * etf day detail net
 * @author nature
 * @version 1.0.0
 * @since 2020/4/4 18:20
 */
@Component
public class KlineHttp {

    /**
     * 链接地址：K线列表
     */
    private static final String URL_KLINE = "http://push2his.eastmoney.com/api/qt/stock/kline/get?secid=%s.%s" +
            "&fields1=f1,f2,f3,f4,f5&fields2=f51,f52,f53,f54,f55,f56,f57&klt=101&fqt=1&beg=%s&end=%s";
    /**
     * CHARSET
     */
    private static final String CHARSET = "utf8";

    /**
     * 获取k线数据
     * @param code  code
     * @param type  type
     * @param start start
     * @param end   end
     * @return list
     */
    public List<Kline> list(String code, String type, String start, String end) {
        String uri = String.format(URL_KLINE, type, code, start, end);
        String response = HttpUtil.doGet(uri, CHARSET, lines -> lines.collect(Collectors.toList()).get(0));
        JSONObject jo = JSON.parseObject(response);
        JSONObject data = jo.getJSONObject("data");
        if (data == null) throw new RuntimeException("历史K线数据缺失：" + code + ":" + type);
        JSONArray ks = data.getJSONArray("klines");
        if (ks == null) throw new RuntimeException("历史K线数据缺失：" + code + ":" + type);
        List<Kline> list = new ArrayList<>();
        for (Object datum : ks) list.add(this.genKline(code, type, (String) datum));
        return list;
    }

    private Kline genKline(String code, String type, String line) {
        String[] s = line.split(",");
        Kline kline = new Kline();
        kline.setCode(code);
        kline.setType(type);
        kline.setDate(s[0].replace("-", Const.EMPTY));
        kline.setOpen(TextUtil.getDecimal(s[1]));
        kline.setLatest(TextUtil.getDecimal(s[2]));
        kline.setHigh(TextUtil.getDecimal(s[3]));
        kline.setLow(TextUtil.getDecimal(s[4]));
        kline.setShare(TextUtil.getDecimal(s[5]));
        kline.setAmount(TextUtil.getDecimal(s[6]));
        return kline;
    }

}
