package org.nature.biz.common.manager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.nature.biz.common.http.KlineHttp;
import org.nature.biz.common.mapper.KlineMapper;
import org.nature.biz.common.model.Kline;
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

    public int load(List<Kline> list) {
        return ExecUtil.batch(() -> list, this::load).stream().mapToInt(i -> i).sum();
    }

    public int reload(List<Kline> list) {
        return ExecUtil.batch(() -> list, this::reload).stream().mapToInt(i -> i).sum();
    }

    /**
     * 按项目加载
     * @param info 项目
     * @return int
     */
    public int load(Kline info) {
        String code = info.getCode();
        String type = info.getType();
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
     * @param info 项目
     * @return int
     */
    public int reload(Kline info) {
        klineMapper.deleteByItem(info.getCode(), info.getType());
        return this.load(info);
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
