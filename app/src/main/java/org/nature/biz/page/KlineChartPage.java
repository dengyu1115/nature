package org.nature.biz.page;

import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;
import org.nature.biz.manager.KlineManager;
import org.nature.biz.model.Item;
import org.nature.biz.model.Kline;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.Page;
import org.nature.common.util.TextUtil;
import org.nature.common.view.LineChart;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static org.nature.common.view.LineChart.C;
import static org.nature.common.view.LineChart.Q;

@PageView(name = "K线图", group = "", col = 0, row = 0)
public class KlineChartPage extends Page {

    public static final List<Function<Kline, Double>> FUNC_NET = List.of(
            i -> i.getOpen().doubleValue(),
            i -> i.getLatest().doubleValue(),
            i -> i.getHigh().doubleValue(),
            i -> i.getLow().doubleValue());
    public static final List<Function<Kline, Double>> FUNC_SHARE = List.of(
            i -> i.getShare().doubleValue()
    );
    public static final List<Function<Kline, Double>> FUNC_AMOUNT = List.of(
            i -> i.getAmount().doubleValue()
    );
    private static final int[] COLORS = new int[]{0xFFFF0000, 0xFF1E90FF, 0xFF32CD32, 0xFFEEEE00, 0xFF8E388E};
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
    private static final List<C<Kline>> RS = List.of(
            new C<>(1000, 3, FUNC_NET, TextUtil::price),
            new C<>(1000, 1, FUNC_SHARE, TextUtil::amount),
            new C<>(1000, 1, FUNC_AMOUNT, TextUtil::amount)
    );

    @Injection
    private KlineManager klineManager;

    private LineChart<Kline> chart;

    @Override
    protected void makeStructure(LinearLayout page, Context context) {
        page.addView(chart = new LineChart<>(context));
        chart.init(COLORS, QS, RS, Kline::getDate, new Kline());
    }

    @Override
    protected void onShow() {
        Item item = this.getParam();
        List<Kline> list = klineManager.listByItem(item);
        list.sort(Comparator.comparing(Kline::getDate));
        chart.data(list);
    }
}
