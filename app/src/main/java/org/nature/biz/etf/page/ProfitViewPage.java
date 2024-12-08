package org.nature.biz.etf.page;

import android.widget.Button;
import android.widget.LinearLayout;
import org.nature.biz.etf.manager.ProfitManager;
import org.nature.biz.etf.model.ProfitView;
import org.nature.biz.etf.model.Rule;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.DateUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.Table;

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

    private final List<Table.Header<ProfitView>> headers = Arrays.asList(
            Table.header("项目", d -> TextUtil.text(d.getTitle()), C, C),
            Table.header("子项1", C, Arrays.asList(
                    Table.header("项目", d -> TextUtil.text(d.getTitle1()), C, C),
                    Table.header("值", d -> TextUtil.text(d.getValue1()), C, E)
            )),
            Table.header("子项2", C, Arrays.asList(
                    Table.header("项目", d -> TextUtil.text(d.getTitle2()), C, C),
                    Table.header("值", d -> TextUtil.text(d.getValue2()), C, E)
            )),
            Table.header("子项3", C, Arrays.asList(
                    Table.header("项目", d -> TextUtil.text(d.getTitle3()), C, C),
                    Table.header("值", d -> TextUtil.text(d.getValue3()), C, E)
            )));
    @Injection
    private ProfitManager profitManager;
    private Button dateBtn;

    @Override
    protected List<Table.Header<ProfitView>> headers() {
        return headers;
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
    protected void initHeaderViews(LinearLayout condition) {
        condition.addView(dateBtn = template.datePiker(10, 7));
    }

    @Override
    protected void initHeaderBehaviours() {

    }

    @Override
    protected int getTotalRows() {
        return 8;
    }

    @Override
    protected int getTotalColumns() {
        return 7;
    }
}
