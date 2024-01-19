package org.nature.biz.job;

import org.nature.biz.manager.HoldManager;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.util.NotifyUtil;
import org.nature.func.job.protocol.Job;

/**
 * 持仓计算
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@JobExec(code = "hold_calc_job", name = "持仓计算")
public class HoldCalcJob implements Job {

    @Injection
    private HoldManager holdManager;

    @Override
    public void exec(String param) {
        int calc = holdManager.calc();
        NotifyUtil.notifyOne("持仓计算", "计算完成，共计算" + calc + "条持仓数据");
    }

}
