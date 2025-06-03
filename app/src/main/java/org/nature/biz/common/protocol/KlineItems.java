package org.nature.biz.common.protocol;

import org.nature.biz.common.model.KInfo;

import java.util.List;

/**
 * K线数据接口
 * 用于定义获取K线信息的方法。
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public interface KlineItems {

    /**
     * 获取K线数据列表
     * @return 返回包含KInfo对象的列表，每个对象代表一个K线数据点
     */
    List<KInfo> kItems();
}
