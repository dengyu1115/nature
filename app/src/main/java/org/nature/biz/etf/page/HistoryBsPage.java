package org.nature.biz.etf.page;

import org.nature.biz.etf.mapper.HoldMapper;
import org.nature.biz.etf.model.Hold;
import org.nature.biz.etf.model.Rule;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;

import java.util.List;

/**
 * 持有数据
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@PageView(name = "持有数据", group = "ETF", col = 0, row = 0)
public class HistoryBsPage extends BaseBsPage {

    @Injection
    private HoldMapper holdMapper;

    @Override
    protected List<Hold> data() {
        Rule rule = this.getParam();
        return holdMapper.listByRule(rule.getCode(), rule.getType(), rule.getName());
    }

}
