package org.nature.biz.common.manager;

import org.nature.biz.common.http.NetHttp;
import org.nature.biz.common.mapper.NetMapper;
import org.nature.biz.common.model.NInfo;
import org.nature.biz.common.model.Net;
import org.nature.biz.common.protocol.NetItems;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.holder.InstanceHolder;
import org.nature.common.util.DateUtil;
import org.nature.common.util.ExecUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<NInfo> nItems() {
        List<NetItems> list = InstanceHolder.list(NetItems.class);
        return new ArrayList<>(list.stream().map(NetItems::nItems).flatMap(List::stream)
                .collect(Collectors.toMap(NInfo::getCode, i -> i, (o, n) -> o)).values());
    }


    public int loadAll() {
        return ExecUtil.batch(this::nItems, i -> this.load(i.getCode())).stream().mapToInt(i -> i).sum();
    }

    public int reloadAll() {
        return ExecUtil.batch(this::nItems, i -> this.reload(i.getCode())).stream().mapToInt(i -> i).sum();
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
