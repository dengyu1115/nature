package org.nature.biz.page;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import org.nature.biz.manager.KlineManager;
import org.nature.biz.model.Item;
import org.nature.biz.model.Kline;
import org.nature.biz.model.KlineView;
import org.nature.common.chart.*;
import org.nature.common.constant.Const;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.Page;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.ViewTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                    new Q<>("MA10:", d -> TextUtil.price(d.getMa10()), 0xFFA020F0),
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

    private String name;
    private List<Kline> list;
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
        page.setOrientation(LinearLayout.VERTICAL);
        int height = metrics.heightPixels;
        float density = metrics.density;
        LinearLayout header = new LinearLayout(context);
        header.setLayoutParams(new LayoutParams(MATCH_PARENT, (int) (30 * density)));
        LinearLayout body = new LinearLayout(context);
        body.setLayoutParams(new LayoutParams(MATCH_PARENT, height - (int) (30 * density)));
        page.addView(header);
        page.addView(body);
        body.addView(chart = new LineChart<>(context));
        header.addView(this.klineView("日", i -> i));
        header.addView(this.klineView("周", this::convertWeek));
        header.addView(this.klineView("月", this::convertMonth));
        header.addView(this.klineView("季", this::convertSeason));
        header.addView(this.klineView("年", this::convertYear));
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
        List<KlineView> viewList = this.convert(list);
        // 设置K线图加载数据
        chart.data(viewList);
    }


    private Button klineView(String title, Function<List<Kline>, List<Kline>> func) {
        Button button = template.button(title, 60, 30);
        ClickUtil.onClick(button, () -> chart.data(this.convert(func.apply(list))));
        return button;
    }

    private List<Kline> convertWeek(List<Kline> list) {
        List<List<Kline>> ll = new ArrayList<>();
        List<Kline> l = null;
        Integer week = null;
        for (Kline i : list) {
            String date = i.getDate();
            LocalDate d = LocalDate.parse(date, DateTimeFormatter.ofPattern(Const.FORMAT_DAY));
            WeekFields wf = WeekFields.of(Locale.getDefault());
            int w = d.get(wf.weekOfWeekBasedYear());
            if (week == null || !week.equals(w)) {
                l = new ArrayList<>();
                ll.add(l);
                week = w;
            }
            l.add(i);
        }
        return ll.stream().map(this::toKline).collect(Collectors.toList());
    }

    private List<Kline> convertMonth(List<Kline> list) {
        List<List<Kline>> ll = new ArrayList<>();
        List<Kline> l = null;
        Month month = null;
        for (Kline i : list) {
            String date = i.getDate();
            LocalDate d = LocalDate.parse(date, DateTimeFormatter.ofPattern(Const.FORMAT_DAY));
            Month m = d.getMonth();
            if (month == null || !month.equals(m)) {
                l = new ArrayList<>();
                ll.add(l);
                month = m;
            }
            l.add(i);
        }
        return ll.stream().map(this::toKline).collect(Collectors.toList());
    }

    private List<Kline> convertSeason(List<Kline> list) {
        List<List<Kline>> ll = new ArrayList<>();
        List<Kline> l = null;
        Integer season = null;
        for (Kline i : list) {
            String date = i.getDate();
            LocalDate d = LocalDate.parse(date, DateTimeFormatter.ofPattern(Const.FORMAT_DAY));
            int m = d.getMonthValue();
            if (season == null || !season.equals((m - 1) / 3)) {
                l = new ArrayList<>();
                ll.add(l);
                season = (m - 1) / 3;
            }
            l.add(i);
        }
        return ll.stream().map(this::toKline).collect(Collectors.toList());
    }

    private List<Kline> convertYear(List<Kline> list) {
        List<List<Kline>> ll = new ArrayList<>();
        List<Kline> l = null;
        Integer year = null;
        for (Kline i : list) {
            String date = i.getDate();
            LocalDate d = LocalDate.parse(date, DateTimeFormatter.ofPattern(Const.FORMAT_DAY));
            int y = d.getYear();
            if (year == null || !year.equals(y)) {
                l = new ArrayList<>();
                ll.add(l);
                year = y;
            }
            l.add(i);
        }
        return ll.stream().map(this::toKline).collect(Collectors.toList());
    }

    private Kline toKline(List<Kline> list) {
        Kline kline = new Kline();
        for (Kline i : list) {
            kline.setCode(i.getCode());
            kline.setType(i.getType());
            kline.setDate(i.getDate());
            kline.setLatest(i.getLatest());
            if (kline.getOpen() == null) {
                kline.setOpen(i.getOpen());
            }
            if (kline.getHigh() == null || kline.getHigh().compareTo(i.getHigh()) < 0) {
                kline.setHigh(i.getHigh());
            }
            if (kline.getLow() == null || kline.getLow().compareTo(i.getLow()) > 0) {
                kline.setLow(i.getLow());
            }
            BigDecimal share = kline.getShare();
            if (share == null) {
                kline.setShare(i.getShare());
            } else {
                kline.setShare(share.add(i.getShare()));
            }
            BigDecimal amount = kline.getAmount();
            if (amount == null) {
                kline.setAmount(i.getAmount());
            } else {
                kline.setAmount(amount.add(i.getAmount()));
            }
        }
        return kline;
    }

    /**
     * 转换K线数据
     * @param list 数据集
     * @return list
     */
    private List<KlineView> convert(List<Kline> list) {
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
            view.setName(this.name);
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
