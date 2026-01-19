package org.nature.biz.common.manager;

import org.nature.biz.common.http.NetHttp;
import org.nature.biz.common.mapper.NetMapper;
import org.nature.biz.common.model.Net;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.DateUtil;
import org.nature.common.util.ExecUtil;

import java.util.List;

/**
 * 净值
 * @author Nature
 * @version 1.0.0
 * @since 2024/5/30
 */
@Component
public class NetManager {

    @Injection
    private NetMapper netMapper;
    @Injection
    private NetHttp netHttp;


    public int load(List<Net> list) {
        return ExecUtil.batch(() -> list, i -> this.load(i.getCode())).stream().mapToInt(i -> i).sum();
    }

    public int reload(List<Net> list) {
        return ExecUtil.batch(() -> list, i -> this.reload(i.getCode())).stream().mapToInt(i -> i).sum();
    }

    public int load(String code) {
        Net net = netMapper.findLatest(code);
        String start = this.getLastDate(net);
        List<Net> list = netHttp.list(code, start, "");
        return netMapper.batchMerge(list);
    }

    public int reload(String code) {
        netMapper.deleteByCode(code);
        return this.load(code);
    }

    /**
     * 获取最新日期
     * @param net 净值对象
     * @return String
     */
    private String getLastDate(Net net) {
        return net == null ? "" : DateUtil.addDays(net.getDate(), 1);
    }

}
