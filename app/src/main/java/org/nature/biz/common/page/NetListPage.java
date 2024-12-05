package org.nature.biz.common.page;

import android.widget.Button;
import org.nature.biz.common.mapper.NetMapper;
import org.nature.biz.common.model.NInfo;
import org.nature.biz.common.model.Net;
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
@PageView(name = "净值列表", group = "", col = 0, row = 0)
public class NetListPage extends ListPage<Net> {

    @Injection
    private NetMapper netMapper;

    private Button chart;

    private final List<Table.Header<Net>> headers = Arrays.asList(
            Table.header("CODE", d -> TextUtil.text(d.getCode()), C, S, Net::getCode),
            Table.header("日期", d -> TextUtil.text(d.getDate()), C, S, Net::getDate),
            Table.header("单位净值", d -> TextUtil.amount(d.getDw()), C, E, Net::getDw),
            Table.header("累计净值", d -> TextUtil.amount(d.getLj()), C, E, Net::getLj),
            Table.header("复权净值", d -> TextUtil.price(d.getNet()), C, E, Net::getNet)
    );

    @Override
    protected List<Table.Header<Net>> define() {
        return headers;
    }

    @Override
    protected List<Net> listData() {
        NInfo info = this.getParam();
        return netMapper.listByCode(info.getCode());
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(chart = template.button("图", 3, 7));
    }

    @Override
    protected void initHeaderBehaviours() {
        chart.setOnClickListener(l -> this.show(NetChartPage.class, this.getParam()));
    }

    @Override
    protected int getTotalColumns() {
        return headers.size();
    }

}
