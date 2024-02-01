package org.nature.func.job.protocol;

import java.util.Date;

/**
 * 任务
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/10
 */
public interface Job {

    /**
     * 执行
     * @param date 日期
     */
    void exec(Date date);

}
