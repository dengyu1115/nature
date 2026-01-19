package org.nature.biz.common.job;

import org.nature.biz.common.manager.KlineManager;
import org.nature.biz.common.model.Kline;
import org.nature.common.constant.Const;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.JobExec;
import org.nature.common.util.NotifyUtil;
import org.nature.func.job.protocol.Job;

import java.util.*;

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

    @Injection
    private org.nature.biz.etf.mapper.ItemMapper etfItemMapper;
    @Injection
    private org.nature.biz.bound.mapper.ItemMapper boundItemMapper;

    @Override
    public void exec(Date date) {
        List<org.nature.biz.etf.model.Item> etfItems = etfItemMapper.listAll();
        List<org.nature.biz.bound.model.Item> boundItems = boundItemMapper.listAll();
        List<Kline> list = new ArrayList<>();
        Set<String> set = new HashSet<>();
        etfItems.forEach(i -> {
            String key = String.join(Const.DELIMITER, i.getCode(), i.getType());
            if (!set.contains(key)) {
                Kline kline = new Kline();
                kline.setCode(i.getCode());
                kline.setType(i.getType());
                list.add(kline);
                set.add(key);
            }
        });
        boundItems.forEach(i -> {
            String key = String.join(Const.DELIMITER, i.getCode(), i.getType());
            if (!set.contains(key)) {
                Kline kline = new Kline();
                kline.setCode(i.getCode());
                kline.setType(i.getType());
                list.add(kline);
                set.add(key);
            }
        });
        int load = klineManager.load(list);
        NotifyUtil.notifyOne("K线加载", "K线加载完成，共加载" + load + "条数据");
    }

}
