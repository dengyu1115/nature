package org.nature.biz.etf.page;

import org.nature.biz.etf.manager.RuleManager;
import org.nature.biz.etf.model.Hold;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;

import java.util.Arrays;
import java.util.List;

/**
 * 持有数据
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@PageView(name = "预测操作", group = "ETF", col = 1, row = 4)
public class PredictBsPage extends BaseBsPage {

    @Injection
    private RuleManager ruleManager;

    private Selector<Integer> countSel;

    @Override
    protected List<Hold> data() {
        return ruleManager.nextHandle(this.countSel.getValue());
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        super.initHeaderViews(searchBar);
        searchBar.addConditionView(countSel = template.selector(60, 30));
    }

    @Override
    protected void initHeaderBehaviours() {
        super.initHeaderBehaviours();
        countSel.mapper(Object::toString).init().refreshData(Arrays.asList(1, 2, 3, 4, 5));
        // 默认展示3条
        countSel.setValue(3);
    }

}
