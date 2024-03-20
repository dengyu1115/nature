package org.nature.biz.page;

import org.nature.biz.manager.BoundManager;
import org.nature.biz.model.BoundRate;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.common.view.TableView;

import java.util.Arrays;
import java.util.List;

/**
 * 债券列表页面
 * @author Nature
 * @version 1.0.0
 * @since 2024/3/19
 */
@PageView(name = "债券列表", group = "ETF", col = 3, row = 1)
public class BoundListPage extends ListPage<BoundRate> {

    @Injection
    private BoundManager boundManager;

    private Selector<String> type;

    private final List<TableView.D<BoundRate>> ds = Arrays.asList(
            TableView.row("CODE", d -> TextUtil.text(d.getCode1()), C, S, BoundRate::getCode1),
            TableView.row("名称", d -> TextUtil.text(d.getName1()), C, S, BoundRate::getName1),
            TableView.row("对比方CODE", d -> TextUtil.text(d.getCode2()), C, E, BoundRate::getCode2),
            TableView.row("对比方名称", d -> TextUtil.text(d.getName2()), C, E, BoundRate::getName2),
            TableView.row("幅度", d -> TextUtil.hundred(d.getRatio()), C, E, BoundRate::getRatio)
    );

    @Override
    protected List<TableView.D<BoundRate>> define() {
        return ds;
    }

    @Override
    protected List<BoundRate> listData() {
        String type = this.type.getValue();
        if ("0".equals(type)) {
            return boundManager.listRatio();
        }
        return boundManager.listCompare();
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(type = template.selector(80, 30));
    }

    @Override
    protected void initHeaderBehaviours() {
        type.mapper(i -> "0".equals(i) ? "涨幅" : "对比").init().refreshData(List.of("0", "1"));
    }

    @Override
    protected int getTotalColumns() {
        return ds.size();
    }

}
