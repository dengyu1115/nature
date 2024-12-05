package org.nature.biz.etf.page;

import org.nature.biz.etf.mapper.ItemMapper;
import org.nature.biz.etf.model.Hold;
import org.nature.biz.etf.model.Item;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.page.ListPage;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.common.view.Table;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 持有数据
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
public abstract class BaseBsPage extends ListPage<Hold> {

    @Injection
    private ItemMapper itemMapper;

    private Selector<String> handleSel;

    private final Map<String, String> itemNameMap = new HashMap<>();

    protected abstract List<Hold> data();

    private final List<Table.Header<Hold>> headers = Arrays.asList(
            Table.header("项目", d -> TextUtil.text(this.getItem(d)), C, S, this::getItem),
            Table.header("规则", d -> TextUtil.text(d.getRule()), C, S, Hold::getRule),
            Table.header("操作", d -> TextUtil.text(this.getHandle(d)), C, C, this::getHandle),
            Table.header("日期", C, Arrays.asList(
                    Table.header("买入", d -> TextUtil.text(d.getDateBuy()), C, S, Hold::getDateBuy),
                    Table.header("卖出", d -> TextUtil.text(d.getDateSell()), C, E, Hold::getDateSell))
            ),
            Table.header("价格", C, Arrays.asList(
                    Table.header("标记", d -> TextUtil.price(d.getMark()), C, E, Hold::getMark),
                    Table.header("买入", d -> TextUtil.price(d.getPriceBuy()), C, E, Hold::getPriceBuy),
                    Table.header("卖出", d -> TextUtil.price(d.getPriceSell()), C, E, Hold::getPriceSell))
            ),
            Table.header("份额", d -> TextUtil.text(d.getShareBuy()), C, E, Hold::getShareBuy),
            Table.header("金额", C, Arrays.asList(
                    Table.header("买入", d -> TextUtil.amount(this.getAmountBuy(d)), C, E, this::getAmountBuy),
                    Table.header("卖出", d -> TextUtil.amount(this.getAmountSell(d)), C, E, this::getAmountSell),
                    Table.header("盈利", d -> TextUtil.amount(d.getProfit()), C, E, Hold::getProfit))
            )
    );

    @Override
    protected List<Table.Header<Hold>> define() {
        return headers;
    }

    @Override
    protected List<Hold> listData() {
        List<Hold> list = this.data();
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
        searchBar.addConditionView(handleSel = template.selector(8, 7));
        handleSel.mapper(i -> {
            if ("1".equals(i)) {
                return "买";
            }
            if ("2".equals(i)) {
                return "卖";
            }
            return "请选择";
        });
        handleSel.refreshData(Arrays.asList("0", "1", "2"));
    }

    @Override
    protected void initHeaderBehaviours() {
        List<Item> items = itemMapper.listAll();
        itemNameMap.clear();
        itemNameMap.putAll(items.stream()
                .collect(Collectors.toMap(i -> String.join(":", i.getCode(), i.getType()), Item::getName)));
    }

    @Override
    protected int getTotalColumns() {
        return 12;
    }

    @Override
    protected int getFixedColumns() {
        return 3;
    }

    /**
     * 获取项目
     * @param d 持有数据
     * @return String
     */
    private String getItem(Hold d) {
        return itemNameMap.get(String.join(":", d.getCode(), d.getType()));
    }

    /**
     * 获取操作类型
     * @param d 持有数据
     * @return String
     */
    private String getHandle(Hold d) {
        return d.getDateSell() == null ? "买" : "卖";
    }

    /**
     * 获取买入金额
     * @param d 持有数据
     * @return BigDecimal
     */
    private BigDecimal getAmountBuy(Hold d) {
        return d.getPriceBuy().multiply(d.getShareBuy());
    }

    /**
     * 获取卖出金额
     * @param d 持有数据
     * @return BigDecimal
     */
    private BigDecimal getAmountSell(Hold d) {
        return d.getPriceSell() == null ? null : d.getPriceSell().multiply(d.getShareBuy());
    }

}
