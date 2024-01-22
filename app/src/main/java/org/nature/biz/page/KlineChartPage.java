package org.nature.biz.page;

import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;
import org.nature.biz.manager.KlineManager;
import org.nature.biz.model.Item;
import org.nature.biz.model.Kline;
import org.nature.common.chart.C;
import org.nature.common.chart.LineChart;
import org.nature.common.chart.Q;
import org.nature.common.chart.R;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.Page;
import org.nature.common.util.TextUtil;

import java.util.Comparator;
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
     * 第一个框中的线条配置
     */
    public static final List<C<Kline>> FUNC_NET = List.of(
            new C<>(0xFFFF0000, i -> i.getOpen().doubleValue()),
            new C<>(0xFF1E90FF, i -> i.getLatest().doubleValue()),
            new C<>(0xFF32CD32, i -> i.getHigh().doubleValue()),
            new C<>(0xFFEEEE00, i -> i.getLow().doubleValue())
    );
    /**
     * 第二个框中的线条配置
     */
    public static final List<C<Kline>> FUNC_SHARE = List.of(
            new C<>(0xFFFF0000, i -> i.getShare().doubleValue())
    );
    /**
     * 第三个框中的线条配置
     */
    public static final List<C<Kline>> FUNC_AMOUNT = List.of(
            new C<>(0xFFFF0000, i -> i.getAmount().doubleValue())
    );
    /**
     * 颜色配置
     */
    private static final int[] COLORS = new int[]{0xFFFF0000, 0xFF1E90FF, 0xFF32CD32, 0xFFEEEE00, 0xFF8E388E};
    /**
     * 指标配置
     */
    private static final List<List<Q<Kline>>> QS = List.of(
            List.of(
                    new Q<>("CODE:", d -> TextUtil.text(d.getCode()), Color.BLACK),
                    new Q<>("TYPE:", d -> TextUtil.text(d.getType()), Color.BLACK),
                    new Q<>("日期:", d -> TextUtil.text(d.getDate()), Color.BLACK)
            ),
            List.of(
                    new Q<>("开盘:", d -> TextUtil.price(d.getOpen()), COLORS[0]),
                    new Q<>("收盘:", d -> TextUtil.price(d.getLatest()), COLORS[1]),
                    new Q<>("最高:", d -> TextUtil.price(d.getHigh()), COLORS[2]),
                    new Q<>("最低:", d -> TextUtil.price(d.getLow()), COLORS[3])
            ),
            List.of(
                    new Q<>("交易量:", d -> TextUtil.amount(d.getShare()), COLORS[0]),
                    new Q<>("交易额:", d -> TextUtil.amount(d.getAmount()), COLORS[0])
            )
    );
    /**
     * 图形框配置
     */
    private static final List<R<Kline>> RS = List.of(
            new R<>(1000, 3, FUNC_NET, TextUtil::price),
            new R<>(1000, 1, FUNC_SHARE, TextUtil::amount),
            new R<>(1000, 1, FUNC_AMOUNT, TextUtil::amount)
    );

    @Injection
    private KlineManager klineManager;

    /**
     * 图
     */
    private LineChart<Kline> chart;

    @Override
    protected void makeStructure(LinearLayout page, Context context) {
        page.addView(chart = new LineChart<>(context));
        chart.init(QS, RS, Kline::getDate, new Kline());
    }

    @Override
    protected void onShow() {
        Item item = this.getParam();
        // 查询K线数据
        List<Kline> list = klineManager.listByItem(item);
        // 按时间正序排序
        list.sort(Comparator.comparing(Kline::getDate));
        // 设置K线图加载数据
        chart.data(list);
    }
}
