package org.nature.html.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializeConfig;
import org.nature.biz.common.manager.KlineManager;
import org.nature.biz.common.manager.NetManager;
import org.nature.biz.common.model.Kline;
import org.nature.biz.common.model.Net;
import org.nature.biz.etf.manager.ProfitManager;
import org.nature.biz.etf.manager.RuleManager;
import org.nature.biz.etf.model.Rule;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.holder.JobHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CommManager {

    private static final TypeReference<List<String>> TYPE_LIST = new TypeReference<>() {
    };
    private static final TypeReference<List<Net>> TYPE_LIST_NET = new TypeReference<>() {
    };
    private static final TypeReference<List<Kline>> TYPE_LIST_KLINE = new TypeReference<>() {
    };
    @Injection
    private RuleManager ruleManager;
    @Injection
    private ProfitManager profitManager;
    @Injection
    private NetManager netManager;
    @Injection
    private KlineManager klineManager;

    public Object handle(String name, String param) {
        switch (name) {
            case "jobs":
                return this.jobs();
            case "kline_load":
                return this.klineLoad(param);
            case "kline_reload":
                return this.klineReload(param);
            case "net_load":
                return this.netLoad(param);
            case "net_reload":
                return this.netReload(param);
            case "etf_latest_handle":
                return this.etfLatestHandle();
            case "etf_next_handle":
                return this.etfNextHandle(param);
            case "etf_left_handle":
                return this.etfLeftHandle();
            case "etf_profit_list":
                return this.etfProfitList(param);
            case "etf_rule_profit_list":
                return this.etfRuleProfitList(param);
            case "etf_profit_overview":
                return this.etfProfitOverview(param);
            case "etf_rule_profit_overview":
                return this.etfRuleProfitOverview(param);
        }
        throw new Warn("调用方法不支持：" + name);
    }

    private Object netReload(String param) {
        JSONObject json = JSON.parseObject(param);
        List<Net> list = json.getObject("list", TYPE_LIST_NET);
        return netManager.reload(list);
    }

    private Object netLoad(String param) {
        JSONObject json = JSON.parseObject(param);
        List<Net> list = json.getObject("list", TYPE_LIST_NET);
        return netManager.load(list);
    }

    private Object klineReload(String param) {
        JSONObject json = JSON.parseObject(param);
        List<Kline> list = json.getObject("list", TYPE_LIST_KLINE);
        return klineManager.reload(list);
    }

    private Object klineLoad(String param) {
        JSONObject json = JSON.parseObject(param);
        List<Kline> list = json.getObject("list", TYPE_LIST_KLINE);
        return klineManager.load(list);
    }

    private Object etfProfitOverview(String param) {
        JSONObject json = JSON.parseObject(param);
        String date = json.getString("date");
        return this.convert(profitManager.overview(date));
    }

    private Object etfRuleProfitOverview(String param) {
        JSONObject json = JSON.parseObject(param);
        String date = json.getString("date");
        Rule rule = json.getObject("rule", Rule.class);
        return this.convert(profitManager.overview(rule, date));
    }

    private Object etfProfitList(String param) {
        JSONObject json = JSON.parseObject(param);
        List<String> dates = json.getObject("dates", TYPE_LIST);
        return this.convert(profitManager.list(dates));
    }

    private Object etfRuleProfitList(String param) {
        JSONObject json = JSON.parseObject(param);
        List<String> dates = json.getObject("dates", TYPE_LIST);
        Rule rule = json.getObject("rule", Rule.class);
        return this.convert(profitManager.list(rule, dates));
    }

    private Object etfLeftHandle() {
        return this.convert(ruleManager.leftHold());
    }

    private Object etfNextHandle(String param) {
        JSONObject json = JSON.parseObject(param);
        int count = json.getIntValue("count");
        return this.convert(ruleManager.nextHandle(count));
    }

    private Object etfLatestHandle() {
        return this.convert(ruleManager.latestHandle());
    }

    private Object convert(List<?> holds) {
        SerializeConfig config = new SerializeConfig();
        config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        return holds.stream().map(i -> JSON.parseObject(JSON.toJSONString(i, config)))
                .collect(Collectors.toList());
    }

    private Object convert(Object obj) {
        SerializeConfig config = new SerializeConfig();
        config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        return JSON.parseObject(JSON.toJSONString(obj, config));
    }

    private Object jobs() {
        return JobHolder.jobs().stream().map(i -> {
            Map<String, String> map = new HashMap<>();
            map.put("code", i);
            map.put("name", JobHolder.getName(i));
            return map;
        }).collect(Collectors.toList());
    }

}
