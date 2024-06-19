package org.nature.biz.etf.job;

import org.nature.biz.etf.manager.ItemManager;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.util.NotifyUtil;
import org.nature.func.job.protocol.Job;

import java.util.Date;

/**
 * K线数据加载
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@JobExec(code = "kline_load_job", name = "K线加载")
public class KlineLoadJob implements Job {

    @Injection
    private ItemManager itemManager;

    @Override
    public void exec(Date date) {
        int load = itemManager.loadKline();
        NotifyUtil.notifyOne("K线加载", "K线加载完成，共加载" + load + "条数据");
    }

}
