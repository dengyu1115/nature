package org.nature.biz.etf.page;

import android.graphics.Color;
import org.nature.biz.etf.model.Profit;
import org.nature.common.chart.*;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.Page;
import org.nature.common.util.TextUtil;

import java.util.Comparator;
import java.util.List;

/**
 * 收益折线图
 * @author Nature
 * @version 1.0.0
 * @since 2025/10/09
 */
@PageView(name = "收益折线图", group = "", col = 0, row = 0)
public class ProfitChartPage extends Page {

    /**
     * 折线配置
     */
    public static final List<Content<Profit>> FUNC_DW = List.of(
            new Content<>(Color.RED, i -> i.getProfitSold().doubleValue()),
            new Content<>(Color.GREEN, i -> i.getProfitHold().doubleValue()),
            new Content<>(Color.BLUE, i -> i.getProfitTotal().doubleValue())
    );

    /**
     * 折线配置
     */
    public static final List<Content<Profit>> FUNC_LJ = List.of(
            new Content<>(Color.BLUE, i -> i.getPaidLeft().doubleValue()),
            new Content<>(Color.GREEN, i -> i.getPaidMax().doubleValue())
    );
    /**
     * 指标配置
     */
    private static final List<List<Quota<Profit>>> QS = List.of(
            List.of(
                    new Quota<>("项目:", d -> TextUtil.text(d.getName()), Color.BLACK),
                    new Quota<>("收益-已卖出:", d -> TextUtil.amount(d.getProfitSold()), Color.RED),
                    new Quota<>("收益-持有:", d -> TextUtil.amount(d.getProfitHold()), Color.GREEN),
                    new Quota<>("收益-总计:", d -> TextUtil.amount(d.getProfitTotal()), Color.BLUE)
            ),
            List.of(
                    new Quota<>("日期:", d -> TextUtil.text(d.getDate()), Color.BLACK),
                    new Quota<>("持仓-最大:", d -> TextUtil.amount(d.getPaidMax()), Color.BLUE),
                    new Quota<>("持仓-剩余:", d -> TextUtil.amount(d.getPaidLeft()), Color.GREEN)
            )
    );
    /**
     * 图形框配置
     */
    private static final List<BaseRect<Profit>> RS = List.of(
            new LineRect<>(1000, 1, FUNC_DW, TextUtil::amount),
            new LineRect<>(1000, 1, FUNC_LJ, TextUtil::amount)
    );

    /**
     * 图
     */
    private LineChart<Profit> chart;

    @Override
    protected void makeStructure() {
        page.addView(chart = new LineChart<>(context));
        chart.sizeDefault(30, 15, 1800);
        chart.init(QS, RS, Profit::getDate);
    }

    @Override
    protected void onShow() {
        List<Profit> list = this.getParam();
        // 按时间正序排序
        list.sort(Comparator.comparing(Profit::getDate));
        // 设置K线图加载数据
        chart.data(list);
    }

}
