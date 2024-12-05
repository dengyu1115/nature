package org.nature.biz.common.page;

import android.widget.Button;
import org.nature.biz.common.mapper.KlineMapper;
import org.nature.biz.common.model.KInfo;
import org.nature.biz.common.model.Kline;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Table;

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
    private KlineMapper klineMapper;

    private Button chart;

    private final List<Table.Header<Kline>> headers = Arrays.asList(
            Table.header("CODE", d -> TextUtil.text(d.getCode()), C, S, Kline::getCode),
            Table.header("日期", d -> TextUtil.text(d.getDate()), C, S, Kline::getDate),
            Table.header("交易量", d -> TextUtil.amount(d.getShare()), C, E, Kline::getShare),
            Table.header("交易额", d -> TextUtil.amount(d.getAmount()), C, E, Kline::getAmount),
            Table.header("最新", d -> TextUtil.price(d.getLatest()), C, E, Kline::getLatest),
            Table.header("今开", d -> TextUtil.price(d.getOpen()), C, E, Kline::getOpen),
            Table.header("最高", d -> TextUtil.price(d.getHigh()), C, E, Kline::getHigh),
            Table.header("最低", d -> TextUtil.price(d.getLow()), C, E, Kline::getLow)
    );

    @Override
    protected List<Table.Header<Kline>> define() {
        return headers;
    }

    @Override
    protected List<Kline> listData() {
        KInfo info = this.getParam();
        return klineMapper.listByItem(info.getCode(), info.getType());
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(chart = template.button("图", 3, 7));
    }

    @Override
    protected void initHeaderBehaviours() {
        chart.setOnClickListener(l -> this.show(KlineChartPage.class, this.getParam()));
    }

    @Override
    protected int getTotalColumns() {
        return headers.size();
    }

}
