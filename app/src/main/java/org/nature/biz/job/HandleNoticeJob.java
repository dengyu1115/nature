package org.nature.biz.job;

import org.nature.common.ioc.annotation.JobExec;
import org.nature.func.job.protocol.Job;

@JobExec(code = "handle_notice_job", name = "操作提醒")
public class HandleNoticeJob implements Job {

    @Override
    public void exec(String param) {

    }

}
