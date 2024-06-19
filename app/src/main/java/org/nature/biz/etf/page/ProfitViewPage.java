package org.nature.biz.etf.page;

import android.widget.Button;
import org.nature.biz.etf.manager.ProfitManager;
import org.nature.biz.etf.model.ProfitView;
import org.nature.biz.etf.model.Rule;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.DateUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.TableView;
import org.nature.common.view.SearchBar;

import java.util.Arrays;
import java.util.List;

/**
 * 盈利总览
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@PageView(name = "盈利总览", group = "ETF", col = 2, row = 1)
public class ProfitViewPage extends ListPage<ProfitView> {

    @Injection
    private ProfitManager profitManager;

    private final List<TableView.D<ProfitView>> ds = Arrays.asList(
            TableView.row("项目", d -> TextUtil.text(d.getTitle()), C, C),
            TableView.row("子项目1", d -> TextUtil.text(d.getTitle1()), C, C),
            TableView.row("值", d -> TextUtil.text(d.getValue1()), C, E),
            TableView.row("子项目2", d -> TextUtil.text(d.getTitle2()), C, C),
            TableView.row("值", d -> TextUtil.text(d.getValue2()), C, E),
            TableView.row("子项目3", d -> TextUtil.text(d.getTitle3()), C, C),
            TableView.row("值", d -> TextUtil.text(d.getValue3()), C, E)
    );

    private Button dateBtn;

    @Override
    protected List<TableView.D<ProfitView>> define() {
        return ds;
    }

    @Override
    protected List<ProfitView> listData() {
        Rule rule = this.getParam();
        String date = this.dateBtn.getText().toString();
        if ("".equals(date)) {
            date = DateUtil.today();
        }
        if (rule == null) {
            return profitManager.view(date);
        }
        return profitManager.view(rule, date);
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(dateBtn = template.datePiker(80, 30));
    }

    @Override
    protected void initHeaderBehaviours() {

    }

    @Override
    protected int getTotalColumns() {
        return 7;
    }
}
