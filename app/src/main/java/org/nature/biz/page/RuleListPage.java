package org.nature.biz.page;

import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.nature.biz.manager.HoldManager;
import org.nature.biz.manager.RuleManager;
import org.nature.biz.model.Item;
import org.nature.biz.model.Rule;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.CommonUtil;
import org.nature.common.util.PopUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.ExcelView;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.common.view.ViewTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 规则页面
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@PageView(name = "规则", group = "", col = 0, row = 0)
public class RuleListPage extends ListPage<Rule> {

    @Injection
    private RuleManager ruleManager;
    @Injection
    private HoldManager holdManager;

    private LinearLayout page;
    private EditText name, base, ratio, date, expansion;
    private Selector<String> statusSel, typeSel;
    private Button add;

    private final List<ExcelView.D<Rule>> ds = Arrays.asList(
            ExcelView.row("名称", d -> TextUtil.text(d.getName()), C, S, CommonUtil.nullsLast(Rule::getName)),
            ExcelView.row("开始日期", d -> TextUtil.text(d.getDate()), C, C, CommonUtil.nullsLast(Rule::getDate)),
            ExcelView.row("金额基数", d -> TextUtil.text(d.getBase()), C, C, CommonUtil.nullsLast(Rule::getBase)),
            ExcelView.row("波动比率", d -> TextUtil.text(d.getRatio()), C, C, CommonUtil.nullsLast(Rule::getRatio)),
            ExcelView.row("扩大幅度", d -> TextUtil.text(d.getExpansion()), C, C, CommonUtil.nullsLast(Rule::getExpansion)),
            ExcelView.row("状态", d -> TextUtil.text(this.statusName(d.getStatus())), C, C, CommonUtil.nullsLast(Rule::getStatus)),
            ExcelView.row("规则类型", d -> TextUtil.text(this.typeName(d.getRuleType())), C, C, CommonUtil.nullsLast(Rule::getRuleType)),
            ExcelView.row("编辑", d -> "+", C, C, this.edit()),
            ExcelView.row("删除", d -> "-", C, C, this.delete()),
            ExcelView.row("持仓计算", d -> "计算", C, C, this.calcProfit()),
            ExcelView.row("持仓查看", d -> "查看", C, C, this.showHold()),
            ExcelView.row("收益查看", d -> "查看", C, C, this.showProfit())
    );

    @Override
    protected List<ExcelView.D<Rule>> define() {
        return ds;
    }

    @Override
    protected List<Rule> listData() {
        return ruleManager.listByItem(this.getParam());
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(add = template.button("+", 30, 30));
    }

    @Override
    protected void initHeaderBehaviours() {
        add.setOnClickListener(v -> this.add());
    }

    @Override
    protected int getExcelColumns() {
        return 11;
    }

    /**
     * 添加操作
     */
    private void add() {
        this.makeWindowStructure();
        PopUtil.confirm(context, "新增", page, () -> this.doEdit(ruleManager::save));
    }

    /**
     * 编辑操作
     * @return 操作逻辑
     */
    private Consumer<Rule> edit() {
        return d -> {
            this.makeWindowStructure();
            this.name.setText(d.getName());
            this.date.setText(d.getDate());
            this.base.setText(d.getBase().toPlainString());
            this.ratio.setText(d.getRatio().toPlainString());
            this.expansion.setText(d.getExpansion().toPlainString());
            this.statusSel.setValue(d.getStatus());
            PopUtil.confirm(context, "编辑-" + d.getName(), page, () -> this.doEdit(ruleManager::edit));
        };
    }

    /**
     * 执行编辑操作
     * @param consumer 处理逻辑
     */
    private void doEdit(Consumer<Rule> consumer) {
        String name = this.name.getText().toString();
        if (name.isEmpty()) {
            throw new RuntimeException("请填写名称");
        }
        String date = this.date.getText().toString();
        if (date.isEmpty()) {
            date = null;
        }
        String base = this.base.getText().toString();
        if (base.isEmpty()) {
            throw new RuntimeException("请填写金额基数");
        }
        String ratio = this.ratio.getText().toString();
        if (ratio.isEmpty()) {
            throw new RuntimeException("请填写波动比率");
        }
        String expansion = this.expansion.getText().toString();
        if (expansion.isEmpty()) {
            throw new RuntimeException("请填写扩大幅度");
        }
        String status = this.statusSel.getValue();
        if (status.isEmpty()) {
            throw new RuntimeException("请选择状态");
        }
        String type = this.typeSel.getValue();
        if (type.isEmpty()) {
            throw new RuntimeException("请选择规则类型");
        }
        Item item = this.getParam();
        Rule rule = new Rule();
        rule.setCode(item.getCode());
        rule.setType(item.getType());
        rule.setName(name);
        rule.setDate(date);
        rule.setBase(new BigDecimal(base));
        rule.setRatio(new BigDecimal(ratio));
        rule.setExpansion(new BigDecimal(expansion));
        rule.setStatus(status);
        rule.setRuleType(type);
        consumer.accept(rule);
        this.refreshData();
        PopUtil.alert(context, "编辑成功！");
    }

    /**
     * 删除操作
     * @return 操作逻辑
     */
    private Consumer<Rule> delete() {
        return d -> PopUtil.confirm(context, "删除项目-" + d.getName(), "确认删除吗？", () -> {
            ruleManager.delete(d);
            this.refreshData();
            PopUtil.alert(context, "删除成功！");
        });
    }

    /**
     * 计算收益
     * @return 操作逻辑
     */
    private Consumer<Rule> calcProfit() {
        return d -> {
            int i = holdManager.calc(d);
            PopUtil.alert(context, "持仓收益计算完成，数据量：" + i);
        };
    }

    /**
     * 展示持有数据
     * @return 操作逻辑
     */
    private Consumer<Rule> showHold() {
        return d -> this.show(HoldListPage.class, d);
    }

    /**
     * 展示收益数据
     * @return 操作逻辑
     */
    private Consumer<Rule> showProfit() {
        return d -> this.show(ProfitListPage.class, d);
    }

    /**
     * 构建弹窗
     */
    private void makeWindowStructure() {
        ViewTemplate template = ViewTemplate.build(context);
        page = template.linearPage();
        page.setGravity(Gravity.CENTER);
        LinearLayout l1 = template.line(300, 30);
        LinearLayout l2 = template.line(300, 30);
        LinearLayout l3 = template.line(300, 30);
        LinearLayout l4 = template.line(300, 30);
        LinearLayout l5 = template.line(300, 30);
        LinearLayout l6 = template.line(300, 30);
        LinearLayout l7 = template.line(300, 30);
        l1.addView(template.textView("名称：", 100, 30));
        l1.addView(name = template.editText(200, 30));
        l2.addView(template.textView("开始日期：", 100, 30));
        l2.addView(date = template.editText(200, 30));
        l3.addView(template.textView("金额基数：", 100, 30));
        l3.addView(base = template.numeric(200, 30));
        l4.addView(template.textView("波动比率：", 100, 30));
        l4.addView(ratio = template.numeric(200, 30));
        l5.addView(template.textView("扩大幅度：", 100, 30));
        l5.addView(expansion = template.numeric(200, 30));
        l6.addView(template.textView("状态：", 100, 30));
        l6.addView(statusSel = template.selector(200, 30));
        l7.addView(template.textView("规则类型：", 100, 30));
        l7.addView(typeSel = template.selector(200, 30));
        statusSel.init().mapper(this::statusName).refreshData(Arrays.asList("1", "0"));
        typeSel.init().mapper(this::typeName).refreshData(Arrays.asList("0", "1", "2"));
        page.addView(l1);
        page.addView(l2);
        page.addView(l3);
        page.addView(l4);
        page.addView(l5);
        page.addView(l6);
        page.addView(l7);
    }

    /**
     * 获取状态名称
     * @param i 状态code
     * @return String
     */
    private String statusName(String i) {
        return "1".equals(i) ? "启用" : "暂停";
    }

    /**
     * 获取类型名称
     * @param i 类型code
     * @return String
     */
    private String typeName(String i) {
        return Map.of("0", "网格", "1", "网格定投", "2", "复利").get(i);
    }

}
