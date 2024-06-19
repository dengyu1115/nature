package org.nature.biz.etf.manager;

import org.nature.biz.common.manager.KlineManager;
import org.nature.biz.etf.mapper.ItemMapper;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.util.ExecUtil;

/**
 * 项目
 * @author Nature
 * @version 1.0.0
 * @since 2024/5/30
 */
@Component
public class ItemManager {

    @Injection
    private ItemMapper itemMapper;
    @Injection
    private KlineManager klineManager;

    /**
     * 加载
     * @return int
     */
    public int loadKline() {
        return ExecUtil.batch(itemMapper::listAll, i -> klineManager.load(i.getCode(), i.getType()))
                .stream().mapToInt(i -> i).sum();
    }

    /**
     * 重新加载
     * @return int
     */
    public int reloadKline() {
        return ExecUtil.batch(itemMapper::listAll, i -> klineManager.reload(i.getCode(), i.getType()))
                .stream().mapToInt(i -> i).sum();
    }
}
