package org.nature.biz.job;

import org.jetbrains.annotations.NotNull;
import org.nature.biz.manager.RuleManager;
import org.nature.biz.model.Hold;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.util.CommonUtil;
import org.nature.common.util.Md5Util;
import org.nature.common.util.NotifyUtil;
import org.nature.func.job.protocol.Job;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 操作提醒
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@JobExec(code = "handle_notice_job", name = "操作提醒")
public class HandleNoticeJob implements Job {

    @Injection
    private RuleManager ruleManager;

    private static final Map<String, Set<String>> MAP = new HashMap<>();

    @Override
    public void exec(String param) {
        // 查询最新操作数据
        List<Hold> holds = ruleManager.latestHandle();
        // 转换买入、卖出数据
        Set<String> buySet = holds.stream().filter(i -> i.getPriceSell() == null).map(this::key).collect(Collectors.toSet());
        Set<String> sellSet = holds.stream().filter(i -> i.getPriceSell() != null).map(this::key).collect(Collectors.toSet());
        String today = CommonUtil.today();
        String keyBuy = today + ":buy";
        String keySell = today + ":sell";
        Set<String> buyExists = MAP.computeIfAbsent(keyBuy, k -> new HashSet<>());
        Set<String> sellExists = MAP.computeIfAbsent(keySell, k -> new HashSet<>());
        buySet.removeAll(buyExists);
        sellSet.removeAll(sellExists);
        // 发送提醒
        if (!buySet.isEmpty()) {
            NotifyUtil.speak("您有" + buySet.size() + "个买入操作请处理！");
        }
        if (!sellSet.isEmpty()) {
            NotifyUtil.speak("您有" + sellExists.size() + "个卖出操作请处理！");
        }
        // 更新数据
        buyExists.addAll(buySet);
        sellExists.addAll(sellSet);
        // 删除过期数据
        Set<String> set = new HashSet<>(MAP.keySet());
        for (String s : set) {
            if (!keyBuy.equals(s) && !keySell.equals(s)) {
                MAP.remove(s);
            }
        }
    }

    private String key(Hold i) {
        return Md5Util.md5(i.getCode(), i.getType(), i.getRule(), i.getLevel() + "");
    }

}
