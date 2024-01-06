package org.nature.biz.job;

import org.nature.biz.manager.HoldManager;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.func.job.protocol.Job;

@JobExec(code = "hold_calc_job", name = "持仓计算")
public class HoldCalcJob implements Job {

    @Injection
    private HoldManager holdManager;

    @Override
    public void exec(String param) {
        holdManager.calc();
    }

}
