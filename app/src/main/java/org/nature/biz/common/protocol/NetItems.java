package org.nature.biz.common.protocol;

import org.nature.biz.common.model.NInfo;

import java.util.List;

/**
 * 网络数据接口
 * 用于定义获取网络信息的方法。
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/7
 */
public interface NetItems {

    /**
     * 获取网络数据列表
     * @return 返回包含NInfo对象的列表，每个对象代表一个网络数据点
     */
    List<NInfo> nItems();
}
