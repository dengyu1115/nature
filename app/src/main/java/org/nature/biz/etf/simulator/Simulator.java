package org.nature.biz.etf.simulator;

import org.nature.biz.etf.model.Hold;
import org.nature.biz.etf.model.Profit;

import java.util.List;

/**
 * 收益模拟
 * @author Nature
 * @version 1.0.0
 * @since 2024/4/29
 */
public interface Simulator {

    /**
     * 计算
     */
    void calc();

    /**
     * 持仓数据获取
     * @return 持仓集合
     */
    List<Hold> getHoldList();

    /**
     * 收益获取
     * @return 收益
     */
    Profit profit();

    /**
     * 获取全部收益数据
     * @return 收益集合
     */
    List<Profit> profits();

    /**
     * 最新操作
     * @return 操作数据集合
     */
    List<Hold> latestHandle();

    /**
     * 下一步操作
     * @param count 条数
     * @return 操作数据集合
     */
    List<Hold> nextHandle(int count);

}
