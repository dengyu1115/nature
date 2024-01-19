package org.nature.func.workday.job;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.nature.common.constant.Const;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.util.NotifyUtil;
import org.nature.func.job.protocol.Job;
import org.nature.func.workday.manager.WorkdayManager;

import java.util.Date;

/**
 * 工作日数据加载
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@JobExec(code = "workday_load_job", name = "K线加载")
public class WorkdayLoadJob implements Job {

    @Injection
    private WorkdayManager workdayManager;

    @Override
    public void exec(String param) {
        String year = DateFormatUtils.format(DateUtils.addYears(new Date(), 1), Const.FORMAT_YEAR);
        int load = workdayManager.load(year);
        NotifyUtil.notifyOne("工作日数据加载", "加载完成，共加载" + load + "条数据");
    }

}
