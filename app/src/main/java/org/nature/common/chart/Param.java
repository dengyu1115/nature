package org.nature.common.chart;

import java.util.List;
import java.util.function.Function;

/**
 * 参数
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/23
 */
public class Param<T> {

    /**
     * 数据量：默认、最小、最大
     */
    public int sizeDefault = 90, sizeMin = 30, sizeMax = 1800;
    /**
     * 指标数据集合
     */
    public List<List<Quota<T>>> qs;
    /**
     * 内容数据集合
     */
    public List<BaseRect<T>> rs;
    /**
     * X轴文案获取函数
     */
    public Function<T, String> xText;
    /**
     * 数据：全部、展示
     */
    public List<T> data, list;
    /**
     * 当前数据
     */
    public T curr;
    /**
     * X轴文案集合
     */
    public List<String> xTexts;
    /**
     * X单位长度、下标、集合大小、展示开始下标、展示结束下标
     */
    public int intervalX, index, listSize = sizeDefault, listStart, listEnd;
    /**
     * X单位长度
     */
    public float unitX, dx, lx, ly;
    /**
     * 状态：长按、移动
     */
    public boolean longPressed, moving;

}
