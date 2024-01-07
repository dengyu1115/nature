package org.nature.biz.job;

import org.nature.common.ioc.annotation.JobExec;
import org.nature.func.job.protocol.Job;

/**
 * 操作提醒
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@JobExec(code = "handle_notice_job", name = "操作提醒")
public class HandleNoticeJob implements Job {

    @Override
    public void exec(String param) {

    }

}
