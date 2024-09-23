package org.nature.biz.etf.page;

import org.nature.biz.etf.manager.RuleManager;
import org.nature.biz.etf.model.Hold;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;

import java.util.List;

/**
 * 持有数据
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@PageView(name = "最新操作", group = "ETF", col = 1, row = 2)
public class LatestBsPage extends BaseBsPage {

    @Injection
    private RuleManager ruleManager;

    @Override
    protected List<Hold> data() {
        return ruleManager.latestHandle();
    }

}
