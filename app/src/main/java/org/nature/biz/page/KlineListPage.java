package org.nature.biz.page;

import android.widget.Button;
import org.nature.biz.manager.KlineManager;
import org.nature.biz.model.Item;
import org.nature.biz.model.Kline;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.TableView;

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

    private final List<TableView.D<Kline>> ds = Arrays.asList(
            TableView.row("CODE", d -> TextUtil.text(d.getCode()), C, S, Kline::getCode),
            TableView.row("日期", d -> TextUtil.text(d.getDate()), C, S, Kline::getDate),
            TableView.row("交易量", d -> TextUtil.amount(d.getShare()), C, E, Kline::getShare),
            TableView.row("交易额", d -> TextUtil.amount(d.getAmount()), C, E, Kline::getAmount),
            TableView.row("最新", d -> TextUtil.price(d.getLatest()), C, E, Kline::getLatest),
            TableView.row("今开", d -> TextUtil.price(d.getOpen()), C, E, Kline::getOpen),
            TableView.row("最高", d -> TextUtil.price(d.getHigh()), C, E, Kline::getHigh),
            TableView.row("最低", d -> TextUtil.price(d.getLow()), C, E, Kline::getLow)
    );

    @Override
    protected List<TableView.D<Kline>> define() {
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
    protected int getTotalColumns() {
        return ds.size();
    }

}
