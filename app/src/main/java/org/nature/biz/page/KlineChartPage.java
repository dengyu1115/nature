package org.nature.biz.page;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import org.nature.biz.manager.KlineManager;
import org.nature.biz.model.Item;
import org.nature.biz.model.Kline;
import org.nature.biz.model.KlineView;
import org.nature.biz.util.KlineUtil;
import org.nature.common.chart.*;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.Page;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.ViewTemplate;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * K线图
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@PageView(name = "K线图", group = "", col = 0, row = 0)
public class KlineChartPage extends Page {

    /**
     * 交易额折线配置
     */
    public static final List<C<KlineView>> FUNC_AMOUNT = List.of(
            new C<>(0xFFFF0000, KlineView::getAmount)
    );
    /**
     * 均线集合配置
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
                    new Q<>("交易额:", d -> TextUtil.amount(d.getAmount()), Color.BLUE),
                    new Q<>("涨幅:", d -> TextUtil.hundred(d.getRatioInc()), Color.BLUE)
            ),
            List.of(
                    new Q<>("开盘:", d -> TextUtil.price(d.getOpen()), Color.GREEN),
                    new Q<>("收盘:", d -> TextUtil.price(d.getLatest()), Color.GREEN),
                    new Q<>("最高:", d -> TextUtil.price(d.getHigh()), Color.GREEN),
                    new Q<>("最低:", d -> TextUtil.price(d.getLow()), Color.GREEN),
                    new Q<>("累计涨幅:", d -> TextUtil.hundred(d.getRatioTotal()), Color.GREEN)
            ),
            List.of(
                    new Q<>("MA5:", d -> TextUtil.price(d.getMa5()), 0xFFB22222),
                    new Q<>("MA10:", d -> TextUtil.price(d.getMa10()), 0xFFA020F0),
                    new Q<>("MA20:", d -> TextUtil.price(d.getMa20()), 0xFF2E8B57),
                    new Q<>("MA60:", d -> TextUtil.price(d.getMa60()), 0xFF0000CD),
                    new Q<>("振幅:", d -> TextUtil.hundred(d.getRatioDiff()), Color.BLUE)
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

    /**
     * 项目名称
     */
    private String name;
    /**
     * K线数据原集合
     */
    private List<Kline> list;
    /**
     * view模板
     */
    private ViewTemplate template;

    @Injection
    private KlineManager klineManager;

    /**
     * 图
     */
    private LineChart<KlineView> chart;

    @Override
    protected void makeStructure(LinearLayout page, Context context) {
        template = ViewTemplate.build(context);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        float density = metrics.density;
        // 创建两个容器左右布局，左边放图，右边放K线切换按钮
        LinearLayout left = new LinearLayout(context);
        LinearLayout right = new LinearLayout(context);
        left.setLayoutParams(new LayoutParams(width - (int) (100 * density), MATCH_PARENT));
        left.setOrientation(LinearLayout.VERTICAL);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setLayoutParams(new LayoutParams((int) (100 * density), MATCH_PARENT));
        right.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(left);
        page.addView(right);
        left.addView(chart = new LineChart<>(context));
        WeekFields wf = WeekFields.of(Locale.getDefault());
        right.addView(this.klineView("日", i -> i));
        right.addView(this.klineView("周", i -> KlineUtil.convert(i, d -> d.get(wf.weekOfWeekBasedYear()))));
        right.addView(this.klineView("月", i -> KlineUtil.convert(i, LocalDate::getMonthValue)));
        right.addView(this.klineView("季", i -> KlineUtil.convert(i, d -> (d.getMonthValue() - 1) / 3)));
        right.addView(this.klineView("年", i -> KlineUtil.convert(i, LocalDate::getYear)));
        chart.sizeDefault(30, 15, 1800);
        chart.init(QS, RS, KlineView::getDate);
    }

    @Override
    protected void onShow() {
        Item item = this.getParam();
        name = item.getName();
        // 查询K线数据
        list = klineManager.listByItem(item);
        // 按时间正序排序
        list.sort(Comparator.comparing(Kline::getDate));
        List<KlineView> viewList = KlineUtil.convert(list, name);
        // 设置K线图加载数据
        chart.data(viewList);
    }

    /**
     * 查看K线按钮
     * @param title 标题
     * @param func  K线数据转换函数
     * @return Button
     */
    private Button klineView(String title, Function<List<Kline>, List<Kline>> func) {
        Button button = template.button(title, 60, 30);
        ClickUtil.onClick(button, () -> chart.data(KlineUtil.convert(func.apply(list), name)));
        return button;
    }

}
