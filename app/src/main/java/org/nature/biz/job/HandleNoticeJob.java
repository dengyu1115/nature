package org.nature.biz.job;

import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.util.NotifyUtil;
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
        NotifyUtil.notify(2, "操作提醒", "这是一条测试提醒");
        NotifyUtil.speak("123',V456,ABC,def,12d3,木头人，测试消息！");
    }

}
