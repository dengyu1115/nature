package org.nature.biz.bound.page;

import android.widget.LinearLayout;
import org.nature.biz.bound.manager.CalcManager;
import org.nature.biz.bound.model.Result;
import org.nature.biz.bound.model.Rule;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.TextUtil;
import org.nature.common.view.Selector;
import org.nature.common.view.Table;

import java.util.Arrays;
import java.util.List;

/**
 * 计算结果
 * @author Nature
 * @version 1.0.0
 * @since 2024/6/4
 */
@PageView(name = "计算结果", group = "债券", col = 0, row = 0)
public class ResultListPage extends ListPage<Result> {

    /**
     * 表头
     */
    private final List<Table.Header<Result>> headers = Arrays.asList(
            Table.header("规则", d -> TextUtil.text(d.getRule()), C, S, Result::getRule),
            Table.header("名称", d -> TextUtil.text(d.getName()), C, S, Result::getName),
            Table.header("编号", d -> TextUtil.text(d.getCode()), C, C, Result::getCode),
            Table.header("类型", d -> TextUtil.text(d.getType()), C, C, Result::getType),
            Table.header("涨幅", d -> TextUtil.hundred(d.getRatio()), C, E, Result::getRatio)
    );
    @Injection
    private CalcManager calcManager;
    private Selector<String> strategy;

    @Override
    protected List<Table.Header<Result>> headers() {
        return headers;
    }

    @Override
    protected List<Result> listData() {
        Rule rule = this.getParam();
        return calcManager.process(rule, strategy.getValue());
    }

    @Override
    protected void initHeaderViews(LinearLayout condition) {
        condition.addView(strategy = template.selector(8, 7));
    }

    @Override
    protected void initHeaderBehaviours() {
        strategy.mapper(i -> i);
        strategy.refreshData(Arrays.asList("MIN", "DIFF", "!DIFF"));
    }

    @Override
    protected int getTotalColumns() {
        return headers.size();
    }

    @Override
    protected int getFixedColumns() {
        return headers.size();
    }

}
