package org.nature.biz.job;

import org.nature.biz.manager.RuleManager;
import org.nature.biz.mapper.ItemMapper;
import org.nature.biz.model.Hold;
import org.nature.biz.model.Item;
import org.nature.common.constant.Const;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.util.DateUtil;
import org.nature.common.util.Md5Util;
import org.nature.common.util.NotifyUtil;
import org.nature.func.job.protocol.Job;
import org.nature.func.workday.manager.WorkdayManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public static final BigDecimal HUNDRED = new BigDecimal("100");

    @Injection
    private RuleManager ruleManager;
    @Injection
    private ItemMapper itemMapper;
    @Injection
    private WorkdayManager workdayManager;

    /**
     * 已操作数据记录map
     */
    private static final Map<String, Set<String>> MAP = new HashMap<>();

    @Override
    public void exec(Date date) {
        // 执行延时1秒以上废弃任务
        if (System.currentTimeMillis() - date.getTime() > 1000) {
            return;
        }
        // 非工作日不处理
        if (!workdayManager.isWorkday()) {
            return;
        }
        // 查询最新操作数据
        List<Hold> holds = ruleManager.latestHandle();
        // 转换买入、卖出数据
        String today = DateUtil.today();
        String keyBuy = today + ":buy";
        String keySell = today + ":sell";
        Set<String> buyExists = MAP.computeIfAbsent(keyBuy, k -> new HashSet<>());
        Set<String> sellExists = MAP.computeIfAbsent(keySell, k -> new HashSet<>());
        // 项目-操作-价格-数量
        Map<String, Map<String, Map<BigDecimal, BigDecimal>>> map = new HashMap<>();
        // 遍历数据，解析需要新执行的各个项目的买入、卖出-价格-份额数据
        for (Hold i : holds) {
            String key = this.key(i);
            BigDecimal priceSell = i.getPriceSell();
            if (priceSell == null) {
                if (buyExists.contains(key)) {
                    continue;
                }
                // 买入操作数据添加
                this.fillHandleData(map, i, keyBuy, i.getPriceBuy(), i.getShareBuy());
                buyExists.add(key);
            } else {
                if (sellExists.contains(key)) {
                    continue;
                }
                // 卖出操作数据添加
                this.fillHandleData(map, i, keySell, i.getPriceSell(), i.getShareSell());
                sellExists.add(key);
            }
        }
        // 构建文本
        String text = this.buildText(map, keyBuy, keySell);
        // 转换语音进行提示
        if (text != null) {
            NotifyUtil.notifyOne("交易提醒", text);
            NotifyUtil.speak(text);
        }
        // 删除过期数据
        Set<String> set = new HashSet<>(MAP.keySet());
        for (String s : set) {
            if (!keyBuy.equals(s) && !keySell.equals(s)) {
                MAP.remove(s);
            }
        }
    }

    /**
     * 填充操作数据
     * @param map       数据map
     * @param i         持仓数据
     * @param handleKey 操作key
     * @param price     价格
     * @param share     份额
     */
    private void fillHandleData(Map<String, Map<String, Map<BigDecimal, BigDecimal>>> map, Hold i, String handleKey,
                                BigDecimal price, BigDecimal share) {
        String itemKey = String.join(Const.DELIMITER, i.getCode(), i.getType());
        // 操作数据map
        Map<BigDecimal, BigDecimal> priceShare = map.computeIfAbsent(itemKey, k -> new HashMap<>())
                .computeIfAbsent(handleKey, k -> new HashMap<>());
        // 相同价格的份额累加
        BigDecimal decimal = priceShare.get(price);
        if (decimal == null) {
            decimal = share;
        } else {
            decimal = decimal.add(share);
        }
        priceShare.put(price, decimal);
    }

    /**
     * 构建语音文本
     * @param map     数据map
     * @param keyBuy  买入操作key
     * @param keySell 卖出操作key
     * @return String
     */
    private String buildText(Map<String, Map<String, Map<BigDecimal, BigDecimal>>> map, String keyBuy, String keySell) {
        // 无数据
        if (map.isEmpty()) {
            return null;
        }
        // 取出所有项目信息用户获取项目名称
        Map<String, String> nameMap = itemMapper.listAll().stream()
                .collect(Collectors.toMap(i -> String.join(Const.DELIMITER, i.getCode(), i.getType()), Item::getName));
        StringBuilder builder = new StringBuilder();
        // 遍历map，拼接文本
        map.forEach((k, v) -> {
            builder.append(nameMap.get(k));
            builder.append(this.handleText(v, keyBuy, "买入"));
            builder.append(this.handleText(v, keySell, "卖出"));
        });
        return builder.toString();
    }

    /**
     * 构建操作文本
     * @param handleMap 操作数据map
     * @param handleKey 操作key
     * @param handle    操作
     * @return String
     */
    private String handleText(Map<String, Map<BigDecimal, BigDecimal>> handleMap, String handleKey, String handle) {
        StringBuilder builder = new StringBuilder();
        Map<BigDecimal, BigDecimal> mb = handleMap.get(handleKey);
        if (mb != null) {
            mb.forEach((price, share) -> {
                builder.append("以价格").append(price).append(handle).append(this.formatShare(share)).append("手。");
            });
        }
        return builder.toString();
    }

    /**
     * 格式化份额转换 股->手
     * @param share 份额
     * @return BigDecimal
     */
    private BigDecimal formatShare(BigDecimal share) {
        return share.divide(HUNDRED, 0, RoundingMode.DOWN);
    }

    /**
     * 转换key
     * @param i 持仓数据
     * @return String
     */
    private String key(Hold i) {
        return Md5Util.md5(i.getCode(), i.getType(), i.getRule(), i.getDateBuy(), i.getLevel() + "");
    }

}
