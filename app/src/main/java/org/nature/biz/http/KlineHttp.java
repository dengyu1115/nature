package org.nature.biz.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nature.biz.model.Kline;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.util.HttpUtil;
import org.nature.common.util.TextUtil;

import java.util.List;
import java.util.stream.Collectors;

import static org.nature.common.constant.Const.EMPTY;
import static org.nature.common.constant.Const.HYPHEN;

/**
 * K线获取
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
     * 获取k线数据
     * @param code  code
     * @param type  type
     * @param start start
     * @param end   end
     * @return list
     */
    public List<Kline> list(String code, String type, String start, String end) {
        // 填充参数生成完整URL
        String uri = String.format(URL_KLINE, type, code, start, end);
        // 发起请求调用得到返回
        String response = HttpUtil.doGet(uri, lines -> lines.collect(Collectors.toList()).get(0));
        // 解析返回数据，转换为json对象
        JSONObject jo = JSON.parseObject(response);
        // 获取所需字段
        JSONObject data = jo.getJSONObject("data");
        if (data == null) {
            throw new RuntimeException("历史K线数据缺失：" + code + ":" + type);
        }
        JSONArray ks = data.getJSONArray("klines");
        if (ks == null) {
            throw new RuntimeException("历史K线数据缺失：" + code + ":" + type);
        }
        // 转换为Kline对象
        return ks.stream().map(i -> this.genKline(code, type, (String) i)).collect(Collectors.toList());
    }

    /**
     * 生成K线
     * @param code 项目编号
     * @param type 项目类型
     * @param line K线String数据
     * @return Kline
     */
    private Kline genKline(String code, String type, String line) {
        String[] s = line.split(",");
        Kline kline = new Kline();
        kline.setCode(code);
        kline.setType(type);
        kline.setDate(s[0].replace(HYPHEN, EMPTY));
        kline.setOpen(TextUtil.getDecimal(s[1]));
        kline.setLatest(TextUtil.getDecimal(s[2]));
        kline.setHigh(TextUtil.getDecimal(s[3]));
        kline.setLow(TextUtil.getDecimal(s[4]));
        kline.setShare(TextUtil.getDecimal(s[5]));
        kline.setAmount(TextUtil.getDecimal(s[6]));
        return kline;
    }

}
