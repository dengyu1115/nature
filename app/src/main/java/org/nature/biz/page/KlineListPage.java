package org.nature.biz.page;

import android.widget.Button;
import org.nature.biz.manager.KlineManager;
import org.nature.biz.model.Item;
import org.nature.biz.model.Kline;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.CommonUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.ExcelView;
import org.nature.common.view.SearchBar;

import java.util.Arrays;
import java.util.List;

/**
 * K线列表
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@PageView(name = "K线列表", group = "", col = 0, row = 0)
public class KlineListPage extends ListPage<Kline> {

    @Injection
    private KlineManager klineManager;

    private Button chart;

    private final List<ExcelView.D<Kline>> ds = Arrays.asList(
            ExcelView.row("CODE", d -> TextUtil.text(d.getCode()), C, S, CommonUtil.nullsLast(Kline::getCode)),
            ExcelView.row("日期", d -> TextUtil.text(d.getDate()), C, S, CommonUtil.nullsLast(Kline::getDate)),
            ExcelView.row("交易量", d -> TextUtil.amount(d.getShare()), C, E, CommonUtil.nullsLast(Kline::getShare)),
            ExcelView.row("交易额", d -> TextUtil.amount(d.getAmount()), C, E, CommonUtil.nullsLast(Kline::getAmount)),
            ExcelView.row("最新", d -> TextUtil.price(d.getLatest()), C, E, CommonUtil.nullsLast(Kline::getLatest)),
            ExcelView.row("今开", d -> TextUtil.price(d.getOpen()), C, E, CommonUtil.nullsLast(Kline::getOpen)),
            ExcelView.row("最高", d -> TextUtil.price(d.getHigh()), C, E, CommonUtil.nullsLast(Kline::getHigh)),
            ExcelView.row("最低", d -> TextUtil.price(d.getLow()), C, E, CommonUtil.nullsLast(Kline::getLow))
    );

    @Override
    protected List<ExcelView.D<Kline>> define() {
        return ds;
    }

    @Override
    protected List<Kline> listData() {
        Item item = this.getParam();
        return klineManager.listByItem(item);
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(chart = template.button("图", 30, 30));
    }

    @Override
    protected void initHeaderBehaviours() {
        chart.setOnClickListener(l -> {
            this.show(KlineChartPage.class, this.getParam());
        });
    }

    @Override
    protected int getExcelColumns() {
        return 8;
    }

}
