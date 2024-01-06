package org.nature.biz.page;

import org.nature.biz.manager.RuleManager;
import org.nature.biz.mapper.HoldMapper;
import org.nature.biz.mapper.ItemMapper;
import org.nature.biz.model.Hold;
import org.nature.biz.model.Item;
import org.nature.biz.model.Rule;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.CommonUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.ExcelView;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

@PageView(name = "最新操作", group = "ETF", col = 1, row = 3)
public class HoldListPage extends ListPage<Hold> {

    @Injection
    private RuleManager ruleManager;
    @Injection
    private ItemMapper itemMapper;
    @Injection
    private HoldMapper holdMapper;

    private Selector<String> typeSel, handleSel;
    private Selector<Integer> countSel;

    private final Map<String, String> itemNameMap = new HashMap<>();

    private final List<ExcelView.D<Hold>> ds = Arrays.asList(
            ExcelView.row("", C, Arrays.asList(
                    ExcelView.row("项目", d -> TextUtil.text(this.getItem(d)), C, S, CommonUtil.nullsLast(this::getItem)),
                    ExcelView.row("规则", d -> TextUtil.text(d.getRule()), C, S, CommonUtil.nullsLast(Hold::getRule)),
                    ExcelView.row("操作", d -> TextUtil.text(this.getHandle(d)), C, C, CommonUtil.nullsLast(this::getHandle)))
            ),
            ExcelView.row("日期", C, Arrays.asList(
                    ExcelView.row("买入", d -> TextUtil.text(d.getDateBuy()), C, S, CommonUtil.nullsLast(Hold::getDateBuy)),
                    ExcelView.row("卖出", d -> TextUtil.text(d.getDateSell()), C, E, CommonUtil.nullsLast(Hold::getDateSell)))
            ),
            ExcelView.row("价格", C, Arrays.asList(
                    ExcelView.row("标记", d -> TextUtil.price(d.getMark()), C, E, CommonUtil.nullsLast(Hold::getMark)),
                    ExcelView.row("买入", d -> TextUtil.price(d.getPriceBuy()), C, E, CommonUtil.nullsLast(Hold::getPriceBuy)),
                    ExcelView.row("卖出", d -> TextUtil.price(d.getPriceSell()), C, E, CommonUtil.nullsLast(Hold::getPriceSell)))
            ),
            ExcelView.row("份额", d -> TextUtil.text(d.getShareBuy()), C, E, CommonUtil.nullsLast(Hold::getShareBuy)),
            ExcelView.row("金额", d -> TextUtil.amount(this.getAmount(d)), C, E, CommonUtil.nullsLast(this::getAmount)),
            ExcelView.row("盈利", d -> TextUtil.amount(d.getProfit()), C, E, CommonUtil.nullsLast(Hold::getProfit))
    );

    @Override
    protected List<ExcelView.D<Hold>> define() {
        return ds;
    }

    @Override
    protected List<Hold> listData() {
        Rule rule = this.getParam();
        List<Hold> list;
        if (rule != null) {
            list = holdMapper.listByRule(rule.getCode(), rule.getType(), rule.getName());
        } else {
            String type = this.typeSel.getValue();
            if ("0".equals(type)) {
                list = ruleManager.latestHandle();
            } else {
                list = ruleManager.nextHandle(this.countSel.getValue());
            }
        }
        String handle = this.handleSel.getValue();
        list = list.stream().filter(i -> {
            if ("0".equals(handle)) {
                return true;
            }
            if ("1".equals(handle)) {
                return i.getDateSell() == null;
            }
            return i.getDateSell() != null;
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(typeSel = template.selector(60, 30));
        searchBar.addConditionView(handleSel = template.selector(60, 30));
        searchBar.addConditionView(countSel = template.selector(60, 30));
    }

    @Override
    protected void initHeaderBehaviours() {
        List<Item> items = itemMapper.listAll();
        itemNameMap.clear();
        itemNameMap.putAll(items.stream()
                .collect(Collectors.toMap(i -> String.join(":", i.getCode(), i.getType()), Item::getName)));
        typeSel.setVisibility(this.getParam() == null ? VISIBLE : GONE);
        typeSel.mapper(i -> {
            if ("0".equals(i)) {
                return "最新";
            }
            return "预计";
        }).init().refreshData(Arrays.asList("0", "1"));
        handleSel.mapper(i -> {
            if ("1".equals(i)) {
                return "买";
            }
            if ("2".equals(i)) {
                return "卖";
            }
            return "请选择";
        }).init().refreshData(Arrays.asList("0", "1", "2"));
        countSel.mapper(Object::toString).init().refreshData(Arrays.asList(1, 2, 3, 4, 5));
        countSel.setVisibility("0".equals(typeSel.getValue()) ? GONE : VISIBLE);
        typeSel.onChangeRun(() -> countSel.setVisibility("0".equals(typeSel.getValue()) ? GONE : VISIBLE));
    }

    @Override
    protected int getExcelColumns() {
        return 11;
    }

    private String getItem(Hold d) {
        return itemNameMap.get(String.join(":", d.getCode(), d.getType()));
    }

    private String getHandle(Hold d) {
        return d.getDateSell() == null ? "买" : "卖";
    }

    private BigDecimal getAmount(Hold d) {
        return (d.getPriceSell() == null ? d.getPriceBuy() : d.getPriceSell()).multiply(d.getShareBuy());
    }

}
