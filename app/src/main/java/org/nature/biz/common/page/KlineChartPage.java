package org.nature.biz.common.page;

import android.graphics.Color;
import android.widget.LinearLayout;
import org.nature.biz.common.mapper.KlineMapper;
import org.nature.biz.common.model.KInfo;
import org.nature.biz.common.model.KView;
import org.nature.biz.common.model.Kline;
import org.nature.biz.common.util.KlineUtil;
import org.nature.common.chart.*;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.Page;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.Button;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static org.nature.common.constant.Const.L_H;

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
    public static final List<Content<KView>> FUNC_AMOUNT = List.of(
            new Content<>(0xFFFF0000, KView::getAmount)
    );
    /**
     * 均线集合配置
     */
    public static final List<Content<KView>> MA_LIST = List.of(
            new Content<>(0xFFB22222, KView::getMa5),
            new Content<>(0xFFA020F0, KView::getMa10),
            new Content<>(0xFF2E8B57, KView::getMa20),
            new Content<>(0xFF0000CD, KView::getMa60)
    );
    /**
     * 指标配置
     */
    private static final List<List<Quota<KView>>> QS = List.of(
            List.of(
                    new Quota<>("项目:", d -> TextUtil.text(d.getName()), Color.BLACK),
                    new Quota<>("日期:", d -> TextUtil.text(d.getDate()), Color.BLACK),
                    new Quota<>("交易量:", d -> TextUtil.amount(d.getShare()), Color.BLUE),
                    new Quota<>("交易额:", d -> TextUtil.amount(d.getAmount()), Color.BLUE),
                    new Quota<>("涨幅:", d -> TextUtil.hundred(d.getRatioInc()), Color.BLUE)
            ),
            List.of(
                    new Quota<>("开盘:", d -> TextUtil.price(d.getOpen()), Color.GREEN),
                    new Quota<>("收盘:", d -> TextUtil.price(d.getLatest()), Color.GREEN),
                    new Quota<>("最高:", d -> TextUtil.price(d.getHigh()), Color.GREEN),
                    new Quota<>("最低:", d -> TextUtil.price(d.getLow()), Color.GREEN),
                    new Quota<>("累计涨幅:", d -> TextUtil.hundred(d.getRatioTotal()), Color.GREEN)
            ),
            List.of(
                    new Quota<>("MA5:", d -> TextUtil.price(d.getMa5()), 0xFFB22222),
                    new Quota<>("MA10:", d -> TextUtil.price(d.getMa10()), 0xFFA020F0),
                    new Quota<>("MA20:", d -> TextUtil.price(d.getMa20()), 0xFF2E8B57),
                    new Quota<>("MA60:", d -> TextUtil.price(d.getMa60()), 0xFF0000CD),
                    new Quota<>("振幅:", d -> TextUtil.hundred(d.getRatioDiff()), Color.BLUE)
            )
    );
    /**
     * 图形框配置
     */
    private static final List<BaseRect<KView>> RS = List.of(
            new KlineRect<>(1000, 3,
                    KView::getOpen,
                    KView::getLatest,
                    KView::getHigh,
                    KView::getLow,
                    MA_LIST,
                    TextUtil::price),
            new LineRect<>(1000, 1, FUNC_AMOUNT, TextUtil::amount)
    );

    /**
     * 项目名称
     */
    private String name;
    /**
     * K线数据原集合
     */
    private List<Kline> list;

    @Injection
    private KlineMapper klineMapper;

    /**
     * 图
     */
    private LineChart<KView> chart;

    @Override
    protected void makeStructure() {
        // 创建两个容器左右布局，左边放图，右边放K线切换按钮
        LinearLayout left = template.line(94, 100);
        LinearLayout right = template.line(6, 100);
        right.setOrientation(LinearLayout.VERTICAL);
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
        chart.init(QS, RS, KView::getDate);
    }

    @Override
    protected void onShow() {
        KInfo info = this.getParam();
        name = info.getName();
        // 查询K线数据
        list = klineMapper.listByItem(info.getCode(), info.getType());
        // 按时间正序排序
        list.sort(Comparator.comparing(Kline::getDate));
        // 设置K线图加载数据
        chart.data(KlineUtil.convert(list, name));
    }

    /**
     * 查看K线按钮
     * @param title 标题
     * @param func  K线数据转换函数
     * @return Button
     */
    private Button klineView(String title, Function<List<Kline>, List<Kline>> func) {
        Button button = template.button(title, 5, L_H);
        ClickUtil.onClick(button, () -> chart.data(KlineUtil.convert(func.apply(list), name)));
        return button;
    }

}
