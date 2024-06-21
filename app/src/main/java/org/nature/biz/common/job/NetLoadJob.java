package org.nature.biz.common.job;

import org.nature.biz.common.manager.NetManager;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.util.NotifyUtil;
import org.nature.func.job.protocol.Job;

import java.util.Date;

/**
 * 净值数据加载
 * @author Nature
 * @version 1.0.0
 * @since 2024/6/21
 */
@JobExec(code = "net_load_job", name = "净值加载")
public class NetLoadJob implements Job {

    @Injection
    private NetManager netManager;

    @Override
    public void exec(Date date) {
        int load = netManager.loadAll();
        NotifyUtil.notifyOne("净值加载", "净值加载完成，共加载" + load + "条数据");
    }

}
