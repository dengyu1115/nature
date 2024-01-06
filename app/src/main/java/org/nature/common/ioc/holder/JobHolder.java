package org.nature.common.ioc.holder;

import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.func.job.protocol.Job;

import java.util.*;

public class JobHolder {

    private static final Map<String, Job> CTX = new HashMap<>();

    private static final Map<String, String> EXEC_MAP = new LinkedHashMap<>();

    public static Job get(String code) {
        return CTX.get(code);
    }

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

    public static List<String> jobs() {
        return new ArrayList<>(EXEC_MAP.keySet());
    }

    public static String getName(String code) {
        return EXEC_MAP.get(code);
    }

}
