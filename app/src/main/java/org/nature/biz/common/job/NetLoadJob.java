package org.nature.biz.common.job;

import org.nature.biz.bound.mapper.ItemMapper;
import org.nature.biz.bound.model.Item;
import org.nature.biz.common.manager.NetManager;
import org.nature.biz.common.model.Net;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.util.NotifyUtil;
import org.nature.func.job.protocol.Job;

import java.util.*;

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
    @Injection
    private ItemMapper itemMapper;

    @Override
    public void exec(Date date) {
        List<Item> items = itemMapper.listAll();
        List<Net> list = new ArrayList<>();
        Set<String> set = new HashSet<>();
        items.forEach(i -> {
            String key = i.getFund();
            if (!set.contains(key)) {
                Net net = new Net();
                net.setCode(i.getFund());
                list.add(net);
                set.add(key);
            }
        });
        int load = netManager.load(list);
        NotifyUtil.notifyOne("净值加载", "净值加载完成，共加载" + load + "条数据");
    }

}
