package org.nature.func.job.manager;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.holder.JobHolder;
import org.nature.common.util.NotifyUtil;
import org.nature.common.util.RemoteExeUtil;
import org.nature.func.job.enums.Status;
import org.nature.func.job.mapper.ConfigInfoMapper;
import org.nature.func.job.model.ConfigInfo;
import org.nature.func.job.protocol.Job;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务执行
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/15
 */
@Component
public class ExecManager {

    @Injection
    private ConfigInfoMapper configInfoMapper;

    /**
     * 执行
     */
    public void exec(Date date) {
        // 开启异步线程
        RemoteExeUtil.submit(() -> {
            try {
                this.doExec(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void doExec(Date date) {
        String year = DateFormatUtils.format(date, "yyyy");
        String month = DateFormatUtils.format(date, "MM");
        String day = DateFormatUtils.format(date, "dd");
        String hour = DateFormatUtils.format(date, "HH");
        String minute = DateFormatUtils.format(date, "mm");
        String second = DateFormatUtils.format(date, "ss");
        // 查询所有任务配置数据
        List<ConfigInfo> list = configInfoMapper.listAll().stream()
                .filter(i -> Status.RUNNING.getCode().equals(i.getStatus())).collect(Collectors.toList());
        for (ConfigInfo i : list) {
            // 判断是否满足执行条件
            if (!this.meet(i, year, month, day, hour, minute, second)) {
                continue;
            }
            try {
                // 执行任务
                Job job = JobHolder.get(i.getCode());
                if (job != null) {
                    job.exec();
                }
            } catch (Exception e) {
                // 执行异常，发送通知
                NotifyUtil.notifyOne("定时任务执行异常", e.getMessage());
            }
        }
    }

    /**
     * 判断是否满足执行条件
     * @param info   任务配置数据
     * @param year   年
     * @param month  月
     * @param day    日
     * @param hour   时
     * @param minute 分
     * @param second 秒
     * @return boolean
     */
    private boolean meet(ConfigInfo info, String year, String month, String day, String hour, String minute, String second) {
        if (!this.meet(info.getYear(), year)) {
            return false;
        }
        if (!this.meet(info.getMonth(), month)) {
            return false;
        }
        if (!this.meet(info.getDay(), day)) {
            return false;
        }
        if (!this.meet(info.getHour(), hour)) {
            return false;
        }
        if (!this.meet(info.getMinute(), minute)) {
            return false;
        }
        return this.meet(info.getSecond(), second);
    }

    /**
     * 判断是否满足执行条件
     * @param condition 条件
     * @param time      时间
     * @return boolean
     */
    private boolean meet(String condition, String time) {
        String[] split = condition.split(":");
        String type = split[0];
        if ("0".equals(type)) {
            return true;
        }
        if ("1".equals(type)) {
            String[] ss = split[1].split("-");
            return time.compareTo(ss[0]) >= 0 && time.compareTo(ss[1]) <= 0;
        }
        if ("2".equals(type)) {
            List<String> list = Arrays.asList(split[1].split(","));
            return list.contains(time);
        }
        return false;
    }
}
