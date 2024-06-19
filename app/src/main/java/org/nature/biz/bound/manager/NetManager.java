package org.nature.biz.bound.manager;

import org.nature.biz.bound.http.NetHttp;
import org.nature.biz.bound.mapper.NetMapper;
import org.nature.biz.bound.model.Net;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;

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
        return net == null ? "" : net.getDate();
    }

}
