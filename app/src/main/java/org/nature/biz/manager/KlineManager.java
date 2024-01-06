package org.nature.biz.manager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.nature.biz.http.KlineHttp;
import org.nature.biz.mapper.ItemMapper;
import org.nature.biz.mapper.KlineMapper;
import org.nature.biz.model.Item;
import org.nature.biz.model.Kline;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.CommonUtil;
import org.nature.common.util.RemoteExeUtil;

import java.util.Date;
import java.util.List;

@Component
public class KlineManager {

    @Injection
    private KlineMapper klineMapper;
    @Injection
    private KlineHttp klineHttp;
    @Injection
    private ItemMapper itemMapper;

    public int load() {
        return RemoteExeUtil.exec(itemMapper::listAll, this::loadByItem).stream().mapToInt(i -> i).sum();
    }

    public int reload() {
        return RemoteExeUtil.exec(itemMapper::listAll, this::reloadByItem).stream().mapToInt(i -> i).sum();
    }

    public int loadByItem(Item item) {
        String code = item.getCode();
        String type = item.getType();
        Kline kline = klineMapper.findLatest(code, type);
        String start = this.getLastDate(kline), end = DateFormatUtils.format(new Date(), "yyyyMMdd");
        List<Kline> list = klineHttp.list(code, type, start, end);
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }
        return klineMapper.batchMerge(list);
    }

    public int reloadByItem(Item item) {
        this.deleteByItem(item);
        return this.loadByItem(item);
    }

    public List<Kline> listByItem(Item item) {
        return klineMapper.listByItem(item.getCode(), item.getType());
    }

    public int deleteByItem(Item item) {
        return klineMapper.deleteByItem(item.getCode(), item.getType());
    }

    private String getLastDate(Kline kline) {
        return kline == null ? "" : CommonUtil.addDays(kline.getDate(), 1).replace("-", "");
    }

}
