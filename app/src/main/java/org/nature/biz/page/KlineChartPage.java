package org.nature.biz.page;

import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;
import org.nature.biz.manager.KlineManager;
import org.nature.biz.model.Item;
import org.nature.biz.model.Kline;
import org.nature.biz.model.KlineView;
import org.nature.common.chart.*;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.Page;
import org.nature.common.util.TextUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * K线图
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@PageView(name = "K线图", group = "", col = 0, row = 0)
public class KlineChartPage extends Page {

    /**
     * 第三个框中的线条配置
     */
    public static final List<C<KlineView>> FUNC_AMOUNT = List.of(
            new C<>(0xFFFF0000, KlineView::getAmount)
    );
    /**
     * 均线集合
     */
    public static final List<C<KlineView>> MA_LIST = List.of(
            new C<>(0xFFB22222, KlineView::getMa5),
            new C<>(0xFFA020F0, KlineView::getMa10),
            new C<>(0xFF2E8B57, KlineView::getMa20),
            new C<>(0xFF0000CD, KlineView::getMa60)
    );
    /**
     * 指标配置
     */
    private static final List<List<Q<KlineView>>> QS = List.of(
            List.of(
                    new Q<>("项目:", d -> TextUtil.text(d.getName()), Color.BLACK),
                    new Q<>("日期:", d -> TextUtil.text(d.getDate()), Color.BLACK),
                    new Q<>("交易量:", d -> TextUtil.amount(d.getShare()), Color.BLUE),
                    new Q<>("交易额:", d -> TextUtil.amount(d.getAmount()), Color.BLUE)
            ),
            List.of(
                    new Q<>("开盘:", d -> TextUtil.price(d.getOpen()), Color.GREEN),
                    new Q<>("收盘:", d -> TextUtil.price(d.getLatest()), Color.GREEN),
                    new Q<>("最高:", d -> TextUtil.price(d.getHigh()), Color.GREEN),
                    new Q<>("最低:", d -> TextUtil.price(d.getLow()), Color.GREEN)
            ),
            List.of(
                    new Q<>("MA5:", d -> TextUtil.price(d.getMa5()), 0xFFB22222),
                    new Q<>("MA10:", d -> TextUtil.price(d.getMa10()),0xFFA020F0),
                    new Q<>("MA20:", d -> TextUtil.price(d.getMa20()), 0xFF2E8B57),
                    new Q<>("MA60:", d -> TextUtil.price(d.getMa60()), 0xFF0000CD)
            )
    );
    /**
     * 图形框配置
     */
    private static final List<BR<KlineView>> RS = List.of(
            new KR<>(1000, 3,
                    KlineView::getOpen,
                    KlineView::getLatest,
                    KlineView::getHigh,
                    KlineView::getLow,
                    MA_LIST,
                    TextUtil::price),
            new LR<>(1000, 1, FUNC_AMOUNT, TextUtil::amount)
    );

    @Injection
    private KlineManager klineManager;

    /**
     * 图
     */
    private LineChart<KlineView> chart;

    @Override
    protected void makeStructure(LinearLayout page, Context context) {
        page.addView(chart = new LineChart<>(context));
        chart.sizeDefault(30, 15, 1800);
        chart.init(QS, RS, KlineView::getDate, new KlineView());
    }

    @Override
    protected void onShow() {
        Item item = this.getParam();
        // 查询K线数据
        List<Kline> list = klineManager.listByItem(item);
        // 按时间正序排序
        list.sort(Comparator.comparing(Kline::getDate));
        List<KlineView> viewList = this.convert(list, item.getName());
        // 设置K线图加载数据
        chart.data(viewList);
    }

    /**
     * 转换K线数据
     * @param list 数据集
     * @param name 项目名称
     * @return list
     */
    private List<KlineView> convert(List<Kline> list, String name) {
        List<KlineView> viewList = new ArrayList<>();
        List<Double> l5 = new LinkedList<>();
        List<Double> l10 = new LinkedList<>();
        List<Double> l20 = new LinkedList<>();
        List<Double> l60 = new LinkedList<>();
        for (Kline k : list) {
            KlineView view = new KlineView();
            view.setDate(k.getDate());
            view.setOpen(k.getOpen().doubleValue());
            double latest = k.getLatest().doubleValue();
            view.setName(name);
            view.setLatest(latest);
            view.setHigh(k.getHigh().doubleValue());
            view.setLow(k.getLow().doubleValue());
            view.setAmount(k.getAmount().doubleValue());
            view.setShare(k.getShare().doubleValue());
            view.setMa5(this.addValue(l5, 5, latest));
            view.setMa10(this.addValue(l10, 10, latest));
            view.setMa20(this.addValue(l20, 20, latest));
            view.setMa60(this.addValue(l60, 60, latest));
            viewList.add(view);
        }
        return viewList;
    }

    /**
     * 添加数值
     * @param list  集合
     * @param size  控制大小
     * @param value 数值
     */
    private Double addValue(List<Double> list, int size, double value) {
        if (list.size() == size) {
            list.remove(0);
        }
        list.add(value);
        if (list.size() == size) {
            return list.stream().mapToDouble(a -> a).average().orElse(0.0);
        }
        return null;
    }
}
