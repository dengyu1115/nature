package org.nature.biz.bound.job;

import org.nature.biz.bound.manager.RateManager;
import org.nature.biz.bound.model.Rate;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.util.DateUtil;
import org.nature.common.util.Md5Util;
import org.nature.common.util.NotifyUtil;
import org.nature.func.job.protocol.Job;
import org.nature.func.workday.manager.WorkdayManager;

import java.math.BigDecimal;
import java.util.*;

/**
 * 债券差价提醒
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@JobExec(code = "bound_notice_job", name = "债券差价提醒")
public class BoundNoticeJob implements Job {

    /**
     * 已通知数据map
     */
    private static final Map<String, Set<String>> NOTICE_MAP = new HashMap<>();

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private static boolean running;

    @Injection
    private WorkdayManager workdayManager;
    @Injection
    private RateManager rateManager;


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
            this.boundHandle();
        });
    }

    /**
     * 债券处理
     */
    private void boundHandle() {
        String today = DateUtil.today();
        List<Rate> rates = rateManager.listTrigger();
        this.notice(today, rates);
        this.deleteExpired(today);
    }

    /**
     * 进行通知操作
     * @param date  日期
     * @param rates 涨幅数据
     */
    private void notice(String date, List<Rate> rates) {
        // 已通知数据set
        Set<String> set = NOTICE_MAP.computeIfAbsent(date, k -> new HashSet<>());
        // 记录需要通知的文案集合
        List<String> list = new ArrayList<>();
        // 遍历操作数据，进行通知操作
        for (Rate i : rates) {
            String code1 = i.getCode1();
            String code2 = i.getCode2();
            BigDecimal ratio = i.getRatio();
            String key = Md5Util.md5(code1, code2, ratio.toPlainString());
            // 如果已经通知过，则跳过
            if (set.contains(key)) {
                continue;
            }
            set.add(key);
            list.add(i.getName1() + "和" + i.getName2() + "相差" + ratio.multiply(HUNDRED) + "%");
        }
        if (list.isEmpty()) {
            return;
        }
        // 通知
        String text = "出现机会" + String.join("，", list) + "。";
        NotifyUtil.speak(text);
        NotifyUtil.notifyOne("债券差价", text);
    }

    /**
     * 删除过期数据
     * @param date 日期
     */
    private void deleteExpired(String date) {
        List<Map<String, ?>> list = List.of(NOTICE_MAP);
        for (Map<String, ?> map : list) {
            Set<String> set = new HashSet<>(map.keySet());
            for (String s : set) {
                if (!date.equals(s)) {
                    map.remove(s);
                }
            }
        }
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

}
