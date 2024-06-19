package org.nature.biz.common.manager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.nature.biz.common.http.KlineHttp;
import org.nature.biz.common.mapper.KlineMapper;
import org.nature.biz.common.model.Kline;
import org.nature.biz.etf.mapper.ItemMapper;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.DateUtil;

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
     * 按项目加载
     * @param code 项目编号
     * @param type 项目类型
     * @return int
     */
    public int load(String code, String type) {
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
     * @param code 项目编号
     * @param type 项目类型
     * @return int
     */
    public int reload(String code, String type) {
        klineMapper.deleteByItem(code, type);
        return this.load(code, type);
    }

    /**
     * 获取最新日期
     * @param kline K线对象
     * @return String
     */
    private String getLastDate(Kline kline) {
        return kline == null ? "" : DateUtil.addDays(kline.getDate(), 1);
    }

}
