package org.nature.biz.job;

import org.nature.biz.manager.KlineManager;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.util.NotifyUtil;
import org.nature.func.job.protocol.Job;

/**
 * K线数据加载
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@JobExec(code = "kline_load_job", name = "K线加载")
public class KlineLoadJob implements Job {

    @Injection
    private KlineManager klineManager;

    @Override
    public void exec(String param) {
        int load = klineManager.load();
        NotifyUtil.notifyOne("K线加载", "K线加载完成，共加载" + load + "条数据");
    }

}
