package org.nature.biz.etf.page;

import android.view.View;
import android.widget.Button;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.nature.biz.etf.manager.ProfitManager;
import org.nature.biz.etf.mapper.ItemMapper;
import org.nature.biz.etf.model.Item;
import org.nature.biz.etf.model.Profit;
import org.nature.biz.etf.model.Rule;
import org.nature.common.constant.Const;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.DateUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.common.view.Table;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 收益列表
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@PageView(name = "收益列表", group = "ETF", col = 2, row = 2)
public class ProfitListPage extends ListPage<Profit> {

    @Injection
    private ProfitManager profitManager;
    @Injection
    private ItemMapper itemMapper;

    private Button dateStart, dateEnd, item, rule, total;
    private Selector<String> dateRule;

    private final List<Table.Header<Profit>> headers = Arrays.asList(
            Table.header("项目", d -> TextUtil.text(d.getName()), C, S, Profit::getName),
            Table.header("规则", d -> TextUtil.text(d.getRule()), C, S, Profit::getRule),
            Table.header("日期", C, Arrays.asList(
                    Table.header("开始", d -> TextUtil.text(d.getDateStart()), C, C, Profit::getDateStart),
                    Table.header("结束", d -> TextUtil.text(d.getDateEnd()), C, C, Profit::getDateEnd))
            ),
            Table.header("收益", C, Arrays.asList(
                    Table.header("已卖出", d -> TextUtil.amount(d.getProfitSold()), C, E, Profit::getProfitSold),
                    Table.header("持有中", d -> TextUtil.amount(d.getProfitHold()), C, E, Profit::getProfitHold),
                    Table.header("卖出+持有", d -> TextUtil.amount(d.getProfitTotal()), C, E, Profit::getProfitTotal),
                    Table.header("卖出/最大", d -> TextUtil.hundred(d.getProfitRatio()), C, E, Profit::getProfitRatio))
            ),
            Table.header("操作次数", C, Arrays.asList(
                    Table.header("买入", d -> TextUtil.text(d.getTimesBuy()), C, C, Profit::getTimesBuy),
                    Table.header("卖出", d -> TextUtil.text(d.getTimesSell()), C, C, Profit::getTimesSell))
            ),
            Table.header("份额", d -> TextUtil.amount(d.getShareTotal()), C, E, Profit::getShareTotal),
            Table.header("资金", C, Arrays.asList(
                    Table.header("投入-最大", d -> TextUtil.amount(d.getPaidMax()), C, E, Profit::getPaidMax),
                    Table.header("投入-持有", d -> TextUtil.amount(d.getPaidLeft()), C, E, Profit::getPaidTotal),
                    Table.header("投入-总额", d -> TextUtil.amount(d.getPaidTotal()), C, E, Profit::getTimesSell),
                    Table.header("回收-总额", d -> TextUtil.amount(d.getReturned()), C, E, Profit::getReturned))
            )
    );

    private final List<String> dateRuleList = List.of("0", "", "1", "2", "3", "4");
    private final Map<String, String> dateRuleMap = Map.of(
            "", "自定义",
            "0", "总计",
            "1", "近1月每日",
            "2", "近1年每周",
            "3", "近3年每月",
            "4", "近10年每年");

    private Map<String, String> nameMap;

    @Override
    protected List<Table.Header<Profit>> define() {
        return headers;
    }

    @Override
    protected List<Profit> listData() {
        Rule rule = this.getParam();
        List<String> dates = this.buildDates(this.dateRule.getValue());
        if (rule == null) {
            return this.merge(profitManager.list(dates));
        }
        return this.merge(profitManager.list(rule, dates));
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(total = template.button("总述", 40, 30));
        searchBar.addConditionView(dateRule = template.selector(80, 30));
        searchBar.addConditionView(dateStart = template.datePiker(80, 30));
        searchBar.addConditionView(dateEnd = template.datePiker(80, 30));
        searchBar.addConditionView(item = template.radio("项目", 40, 30));
        searchBar.addConditionView(rule = template.radio("规则", 40, 30));
    }

    @Override
    protected void initHeaderBehaviours() {
        total.setOnClickListener(v -> this.show(ProfitViewPage.class, this.getParam()));
        dateRule.init().mapper(dateRuleMap::get).refreshData(dateRuleList);
        dateRule.onChangeRun(this::toggleDateBtn);
        this.toggleDateBtn();
        this.nameMap = itemMapper.listAll().stream()
                .collect(Collectors.toMap(i -> String.join(Const.DELIMITER, i.getCode(), i.getType()), Item::getName));
    }

    @Override
    protected int getTotalColumns() {
        return 10;
    }

    @Override
    protected int getFixedColumns() {
        return 3;
    }

    /**
     * 日期按钮展示控制
     */
    private void toggleDateBtn() {
        String value = dateRule.getValue();
        if ("".equals(value)) {
            dateStart.setVisibility(View.VISIBLE);
            dateEnd.setVisibility(View.VISIBLE);
        } else if ("0".equals(value)) {
            dateStart.setVisibility(View.GONE);
            dateEnd.setVisibility(View.VISIBLE);
        } else {
            dateStart.setVisibility(View.GONE);
            dateEnd.setVisibility(View.GONE);
        }
    }

    /**
     * 合并收益数据处理
     * @param profits 原集合
     * @return 合并后集合
     */
    private List<Profit> merge(List<Profit> profits) {
        boolean itemFlag = StringUtils.isNotBlank(this.item.getHint());
        boolean ruleFlag = StringUtils.isNotBlank(this.rule.getHint());
        Function<Profit, String> groupKey;
        if (!itemFlag && !ruleFlag) {
            groupKey = i -> String.join(Const.DELIMITER, i.getDate(), i.getCode(), i.getType(), i.getRule(), this.getItemName(i));
        } else if (itemFlag && ruleFlag) {
            groupKey = i -> String.join(Const.DELIMITER, i.getDate(), Const.EMPTY, Const.EMPTY, Const.TOTAL, Const.TOTAL);
        } else if (itemFlag) {
            groupKey = i -> String.join(Const.DELIMITER, i.getDate(), Const.EMPTY, Const.EMPTY, i.getRule(), Const.TOTAL);
        } else {
            groupKey = i -> String.join(Const.DELIMITER, i.getDate(), i.getCode(), i.getType(), Const.TOTAL, this.getItemName(i));
        }
        Map<String, List<Profit>> group = profits.stream().collect(Collectors.groupingBy(groupKey));
        return group.entrySet().stream().map(i -> {
                    String[] split = i.getKey().split(Const.DELIMITER, 5);
                    Profit profit = profitManager.merge(i.getValue());
                    profit.setDate(split[0]);
                    profit.setCode(split[1]);
                    profit.setType(split[2]);
                    profit.setRule(split[3]);
                    profit.setName(split[4]);
                    return profit;
                }).sorted(Comparator.comparing(Profit::getDate).reversed().thenComparing(Profit::getCode))
                .collect(Collectors.toList());
    }

    private List<String> buildDates(String dateRule) {
        String today = DateUtil.today();
        String dateEnd = this.dateEnd.getText().toString();
        if ("".equals(dateRule)) {
            Warn.check(() -> StringUtils.isNotBlank(dateEnd), "请选择结束日期");
            String dateStart = this.dateStart.getText().toString();
            Warn.check(() -> StringUtils.isNotBlank(dateStart), "请选择开始日期");
            Warn.check(() -> dateStart.compareTo(dateEnd) >= 0, "结束日期不可早于开始日期");
            Warn.check(() -> dateStart.compareTo(today) >= 0, "开始日期不可晚于今天");
            return List.of(dateStart, dateEnd);
        }
        if ("0".equals(dateRule)) {
            return List.of(dateEnd.isEmpty() ? today : dateEnd);
        }
        List<String> dates = new LinkedList<>();
        if ("1".equals(dateRule)) {
            for (int i = 0; i < 32; i++) {
                dates.add(0, DateUtil.addDays(today, -i));
            }
            return dates;
        }
        Calendar calendar = Calendar.getInstance();
        if ("2".equals(dateRule)) {
            Date date = DateUtils.addWeeks(new Date(), 2);
            for (int i = 0; i < 53; i++) {
                date = DateUtils.addWeeks(date, -1);
                calendar.setTime(date);
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                dates.add(0, DateUtil.format(calendar.getTime(), Const.FORMAT_DAY));
            }
            return dates;
        }
        if ("3".equals(dateRule)) {
            Date date = DateUtils.addMonths(new Date(), 2);
            for (int i = 0; i < 37; i++) {
                date = DateUtils.addMonths(date, -1);
                calendar.setTime(date);
                calendar.set(Calendar.DAY_OF_MONTH, 0);
                dates.add(0, DateUtil.format(calendar.getTime(), Const.FORMAT_DAY));
            }
            return dates;
        }
        if ("4".equals(dateRule)) {
            Date date = DateUtils.addYears(new Date(), 2);
            for (int i = 0; i < 11; i++) {
                date = DateUtils.addYears(date, -1);
                calendar.setTime(date);
                calendar.set(Calendar.DAY_OF_YEAR, 0);
                dates.add(0, DateUtil.format(calendar.getTime(), Const.FORMAT_DAY));
            }
            return dates;
        }
        throw new Warn("日期规则不支持：" + dateRule);
    }

    /**
     * 获取项目名称
     * @param i 收益对象
     * @return String
     */
    private String getItemName(Profit i) {
        return nameMap.get(String.join(Const.DELIMITER, i.getCode(), i.getType()));
    }

}
