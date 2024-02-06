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
import org.nature.common.util.DateUtil;
import org.nature.common.util.ExecUtil;

import java.util.Date;
import java.util.List;

/**
 * K线
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
@Component
public class KlineManager {

    @Injection
    private KlineMapper klineMapper;
    @Injection
    private KlineHttp klineHttp;
    @Injection
    private ItemMapper itemMapper;

    /**
     * 加载
     * @return int
     */
    public int load() {
        return ExecUtil.batch(itemMapper::listAll, this::loadByItem).stream().mapToInt(i -> i).sum();
    }

    /**
     * 重新加载
     * @return int
     */
    public int reload() {
        return ExecUtil.batch(itemMapper::listAll, this::reloadByItem).stream().mapToInt(i -> i).sum();
    }

    /**
     * 按项目加载
     * @param item 项目
     * @return int
     */
    public int loadByItem(Item item) {
        String code = item.getCode();
        String type = item.getType();
        // 查询项目最新K线数据
        Kline kline = klineMapper.findLatest(code, type);
        // 加载起始日期参数计算
        String start = this.getLastDate(kline), end = DateFormatUtils.format(new Date(), "yyyyMMdd");
        // 通过http调用获取K线数据
        List<Kline> list = klineHttp.list(code, type, start, end);
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }
        // 保存入库
        return klineMapper.batchMerge(list);
    }

    /**
     * 按项目重新加载
     * @param item 项目
     * @return int
     */
    public int reloadByItem(Item item) {
        this.deleteByItem(item);
        return this.loadByItem(item);
    }

    /**
     * 按项目查询
     * @param item 项目
     * @return list
     */
    public List<Kline> listByItem(Item item) {
        return klineMapper.listByItem(item.getCode(), item.getType());
    }

    /**
     * 按项目删除
     * @param item 项目
     * @return int
     */
    public int deleteByItem(Item item) {
        return klineMapper.deleteByItem(item.getCode(), item.getType());
    }

    /**
     * 获取最新日期
     * @param kline K线对象
     * @return String
     */
    private String getLastDate(Kline kline) {
        return kline == null ? "" : DateUtil.addDays(kline.getDate(), 1).replace("-", "");
    }

}
