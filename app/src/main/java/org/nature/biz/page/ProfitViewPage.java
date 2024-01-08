package org.nature.biz.page;

import android.widget.Button;
import org.nature.biz.manager.ProfitManager;
import org.nature.biz.model.ProfitView;
import org.nature.biz.model.Rule;
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
 * 盈利总览
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@PageView(name = "盈利总览", group = "ETF", col = 2, row = 1)
public class ProfitViewPage extends ListPage<ProfitView> {

    @Injection
    private ProfitManager profitManager;

    private final List<ExcelView.D<ProfitView>> ds = Arrays.asList(
            ExcelView.row("项目", d -> TextUtil.text(d.getTitle()), C, C),
            ExcelView.row("子项目1", d -> TextUtil.text(d.getTitle1()), C, C),
            ExcelView.row("值", d -> TextUtil.text(d.getValue1()), C, E),
            ExcelView.row("子项目2", d -> TextUtil.text(d.getTitle2()), C, C),
            ExcelView.row("值", d -> TextUtil.text(d.getValue2()), C, E),
            ExcelView.row("子项目3", d -> TextUtil.text(d.getTitle3()), C, C),
            ExcelView.row("值", d -> TextUtil.text(d.getValue3()), C, E)
    );

    private Button dateBtn;

    @Override
    protected List<ExcelView.D<ProfitView>> define() {
        return ds;
    }

    @Override
    protected List<ProfitView> listData() {
        Rule rule = this.getParam();
        String date = this.dateBtn.getText().toString();
        if ("".equals(date)) {
            date = CommonUtil.today();
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
    protected int getExcelColumns() {
        return 7;
    }
}
