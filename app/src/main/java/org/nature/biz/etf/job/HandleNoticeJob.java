package org.nature.biz.etf.job;

import org.nature.biz.common.manager.RecordManager;
import org.nature.biz.etf.manager.RuleManager;
import org.nature.biz.etf.mapper.ItemMapper;
import org.nature.biz.etf.model.Hold;
import org.nature.biz.etf.model.Item;
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
    public static final String RECORD_TYPE = "ETF_NOTICE", KEY_BUY = "buy", KEY_SELL = "sell";

    private static boolean running;

    @Injection
    private RuleManager ruleManager;
    @Injection
    private ItemMapper itemMapper;
    @Injection
    private WorkdayManager workdayManager;
    @Injection
    private RecordManager recordManager;

    @Override
    public void exec(Date date) {
        this.doExec(() -> {
            // 执行延时1秒以上废弃任务
            if (System.currentTimeMillis() - date.getTime() > 1000) {
                return;
            }
            // 非工作日不处理
            if (!workdayManager.isWorkday()) {
                return;
            }
            this.exec();
        });
    }

    /**
     * etf最新操作数据处理
     */
    private void exec() {
        // 查询最新操作数据
        List<Hold> holds = ruleManager.latestHandle();
        // 转换买入、卖出数据
        String today = DateUtil.today();
        Map<String, Set<String>> handleMap = recordManager.get(RECORD_TYPE, today, new HashMap<>());
        Set<String> buyExists = handleMap.computeIfAbsent(KEY_BUY, k -> new HashSet<>());
        Set<String> sellExists = handleMap.computeIfAbsent(KEY_SELL, k -> new HashSet<>());
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
                this.fillHandleData(map, i, KEY_BUY, i.getPriceBuy(), i.getShareBuy());
                buyExists.add(key);
            } else {
                if (sellExists.contains(key)) {
                    continue;
                }
                // 卖出操作数据添加
                this.fillHandleData(map, i, KEY_SELL, i.getPriceSell(), i.getShareSell());
                sellExists.add(key);
            }
        }
        // 构建文本
        String text = this.buildText(map);
        // 转换语音进行提示
        if (text != null) {
            NotifyUtil.notifyOne("交易提醒", text);
            NotifyUtil.speak(text);
        }
        recordManager.set(RECORD_TYPE, today, handleMap);
    }

    /**
     * 串行执行
     * @param runnable 执行逻辑
     */
    private void doExec(Runnable runnable) {
        synchronized (this) {
            // 已经有执行中任务，不再执行
            if (running) {
                return;
            }
            // 标记正在执行
            running = true;
        }
        // 执行任务
        try {
            runnable.run();
        } finally {
            // 执行完毕，标记为未执行
            running = false;
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
     * @param map 数据map
     * @return String
     */
    private String buildText(Map<String, Map<String, Map<BigDecimal, BigDecimal>>> map) {
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
            builder.append(this.handleText(v, KEY_BUY, "买入"));
            builder.append(this.handleText(v, KEY_SELL, "卖出"));
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
