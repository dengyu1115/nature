package org.nature.biz.common.page;

import android.graphics.Color;
import org.nature.biz.common.mapper.NetMapper;
import org.nature.biz.common.model.NInfo;
import org.nature.biz.common.model.NView;
import org.nature.biz.common.model.Net;
import org.nature.common.chart.*;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.Page;
import org.nature.common.util.TextUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 净值折线图
 * @author Nature
 * @version 1.0.0
 * @since 2024/6/21
 */
@PageView(name = "净值折线图", group = "", col = 0, row = 0)
public class NetChartPage extends Page {

    /**
     * 折线配置
     */
    public static final List<Content<NView>> FUNC_DW = List.of(
            new Content<>(Color.RED, NView::getDw),
            new Content<>(Color.GREEN, NView::getNet)
    );

    /**
     * 折线配置
     */
    public static final List<Content<NView>> FUNC_LJ = List.of(
            new Content<>(Color.BLUE, NView::getLj)
    );
    /**
     * 指标配置
     */
    private static final List<List<Quota<NView>>> QS = List.of(
            List.of(
                    new Quota<>("项目:", d -> TextUtil.text(d.getName()), Color.BLACK),
                    new Quota<>("日期:", d -> TextUtil.text(d.getDate()), Color.BLACK),
                    new Quota<>("单位净值:", d -> TextUtil.amount(d.getDw()), Color.RED),
                    new Quota<>("复权净值:", d -> TextUtil.amount(d.getNet()), Color.GREEN),
                    new Quota<>("累计净值:", d -> TextUtil.amount(d.getLj()), Color.BLUE)
            )
    );
    /**
     * 图形框配置
     */
    private static final List<BaseRect<NView>> RS = List.of(
            new LineRect<>(1000, 1, FUNC_DW, TextUtil::amount),
            new LineRect<>(1000, 1, FUNC_LJ, TextUtil::amount)
    );

    @Injection
    private NetMapper netMapper;

    /**
     * 图
     */
    private LineChart<NView> chart;

    @Override
    protected void makeStructure() {
        page.addView(chart = new LineChart<>(context));
        chart.sizeDefault(30, 15, 1800);
        chart.init(QS, RS, NView::getDate);
    }

    @Override
    protected void onShow() {
        NInfo info = this.getParam();
        String name = info.getName();
        // 查询K线数据
        List<Net> list = netMapper.listByCode(info.getCode());
        // 按时间正序排序
        list.sort(Comparator.comparing(Net::getDate));
        List<NView> viewList = this.convert(list, name);
        // 设置K线图加载数据
        chart.data(viewList);
    }

    /**
     * 转换K线数据
     * @param list 数据集
     * @param name 名称
     * @return list
     */
    public List<NView> convert(List<Net> list, String name) {
        List<NView> viewList = new ArrayList<>();
        for (Net i : list) {
            NView view = new NView();
            view.setName(name);
            view.setDate(i.getDate());
            double dw = i.getDw().doubleValue();
            double lj = i.getLj().doubleValue();
            double net = i.getNet().doubleValue();
            view.setDw(dw);
            view.setLj(lj);
            view.setNet(net);
            viewList.add(view);
        }
        return viewList;
    }

}
