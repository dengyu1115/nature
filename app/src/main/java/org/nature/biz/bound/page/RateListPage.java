package org.nature.biz.bound.page;

import android.widget.Button;
import org.nature.biz.bound.manager.RateManager;
import org.nature.biz.bound.mapper.RuleMapper;
import org.nature.biz.bound.model.Rate;
import org.nature.biz.bound.model.Rule;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.common.view.Table;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 债券列表页面
 * @author Nature
 * @version 1.0.0
 * @since 2024/3/19
 */
@PageView(name = "涨幅列表", group = "债券", col = 2, row = 1)
public class RateListPage extends ListPage<Rate> {

    @Injection
    private RateManager boundManager;
    @Injection
    private RuleMapper ruleMapper;

    private Selector<String> type, rule;

    private Button date;


    private final List<Table.Header<Rate>> headers = Arrays.asList(
            Table.header("规则编号", d -> TextUtil.text(d.getRuleCode()), C, S, Rate::getRuleCode),
            Table.header("规则名称", d -> TextUtil.text(d.getRuleName()), C, S, Rate::getRuleName),
            Table.header("CODE1", d -> TextUtil.text(d.getCode1()), C, S, Rate::getCode1),
            Table.header("名称1", d -> TextUtil.text(d.getName1()), C, S, Rate::getName1),
            Table.header("CODE2", d -> TextUtil.text(d.getCode2()), C, E, Rate::getCode2),
            Table.header("名称2", d -> TextUtil.text(d.getName2()), C, E, Rate::getName2),
            Table.header("价格1", d -> TextUtil.price(d.getPrice1()), C, E, Rate::getPrice1),
            Table.header("价格2", d -> TextUtil.price(d.getPrice2()), C, E, Rate::getPrice2),
            Table.header("幅度", d -> TextUtil.hundred(d.getRatio()), C, E, Rate::getRatio)
    );

    @Override
    protected List<Table.Header<Rate>> define() {
        return headers;
    }

    @Override
    protected List<Rate> listData() {
        String type = this.type.getValue();
        String rule = this.rule.getValue();
        String date = this.date.getText().toString();
        if ("0".equals(type)) {
            return boundManager.listRatio(rule, date);
        }
        return boundManager.listCompare(rule, date);
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(type = template.selector(80, 30));
        searchBar.addConditionView(rule = template.selector(80, 30));
        searchBar.addConditionView(date = template.datePiker(80, 30));
    }

    @Override
    protected void initHeaderBehaviours() {
        List<Rule> rules = ruleMapper.listAll();
        Map<String, String> map = rules.stream().collect(Collectors.toMap(Rule::getCode, Rule::getName));
        List<String> ruleCodes = rules.stream().map(Rule::getCode).collect(Collectors.toList());
        rule.mapper(map::get);
        rule.refreshData(ruleCodes);
        type.mapper(i -> "0".equals(i) ? "涨幅" : "对比");
        type.refreshData(List.of("0", "1"));
    }

    @Override
    protected int getTotalColumns() {
        return headers.size();
    }

}
