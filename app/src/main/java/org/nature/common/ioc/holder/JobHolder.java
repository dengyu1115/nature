package org.nature.common.ioc.holder;

import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.func.job.protocol.Job;

import java.util.*;

/**
 * 任务持有器
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
public class JobHolder {

    private static final Map<String, Job> CTX = new HashMap<>();

    private static final Map<String, String> EXEC_MAP = new LinkedHashMap<>();

    /**
     * 获取任务实例
     * @param code code
     * @return Job
     */
    public static Job get(String code) {
        return CTX.get(code);
    }

    /**
     * 注册任务
     * @param anno 注解
     * @param job  任务实例
     */
    public synchronized static void register(JobExec anno, Object job) {
        if (anno == null) {
            throw new Warn("任务参数错误");
        }
        String code = anno.code();
        if (CTX.get(code) != null) {
            throw new Warn("对应code的任务已经存在:" + code);
        }
        if (!(job instanceof Job)) {
            throw new Warn("不是Job实例:" + job);
        }
        CTX.put(code, (Job) job);
        EXEC_MAP.put(code, anno.name());
    }

    /**
     * 全部任务
     * @return list
     */
    public static List<String> jobs() {
        return new ArrayList<>(EXEC_MAP.keySet());
    }

    /**
     * 获取任务名称
     * @param code code
     * @return String
     */
    public static String getName(String code) {
        return EXEC_MAP.get(code);
    }

}
