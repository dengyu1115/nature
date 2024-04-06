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
import org.nature.common.util.PopUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.common.view.TableView;
import org.nature.common.view.ViewTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.nature.common.constant.Const.*;

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

    private final List<TableView.D<Rule>> ds = Arrays.asList(
            TableView.row("名称", d -> TextUtil.text(d.getName()), C, S, Rule::getName),
            TableView.row("规则类型", d -> TextUtil.text(this.typeName(d.getRuleType())), C, C, Rule::getRuleType),
            TableView.row("状态", d -> TextUtil.text(this.statusName(d.getStatus())), C, C, Rule::getStatus),
            TableView.row("编辑", d -> "+", C, C, this::edit),
            TableView.row("删除", d -> "-", C, C, this::delete),
            TableView.row("持仓计算", d -> "计算", C, C, this::calcProfit),
            TableView.row("持仓查看", d -> "查看", C, C, this::showHold),
            TableView.row("收益查看", d -> "查看", C, C, this::showProfit),
            TableView.row("开始日期", d -> TextUtil.text(d.getDate()), C, C, Rule::getDate),
            TableView.row("金额基数", d -> TextUtil.text(d.getBase()), C, C, Rule::getBase),
            TableView.row("波动比率", d -> TextUtil.text(d.getRatio()), C, C, Rule::getRatio),
            TableView.row("扩大幅度", d -> TextUtil.text(d.getExpansion()), C, C, Rule::getExpansion)
    );

    @Override
    protected List<TableView.D<Rule>> define() {
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
    protected int getTotalColumns() {
        return 11;
    }

    @Override
    protected int getFixedColumns() {
        return 3;
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
     * @param d 数据
     */
    private void edit(Rule d) {
        this.makeWindowStructure();
        this.name.setText(d.getName());
        this.date.setText(d.getDate());
        this.base.setText(d.getBase().toPlainString());
        this.ratio.setText(d.getRatio().toPlainString());
        this.expansion.setText(d.getExpansion().toPlainString());
        this.statusSel.setValue(d.getStatus());
        PopUtil.confirm(context, "编辑-" + d.getName(), page, () -> this.doEdit(ruleManager::edit));
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
     * @param d 数据
     */
    private void delete(Rule d) {
        PopUtil.confirm(context, "删除项目-" + d.getName(), "确认删除吗？", () -> {
            ruleManager.delete(d);
            this.refreshData();
            PopUtil.alert(context, "删除成功！");
        });
    }

    /**
     * 计算收益
     * @param d 数据
     */
    private void calcProfit(Rule d) {
        int i = holdManager.calc(d);
        PopUtil.alert(context, "持仓收益计算完成，数据量：" + i);
    }

    /**
     * 展示持有数据
     * @param d 数据
     */
    private void showHold(Rule d) {
        this.show(HoldListPage.class, d);
    }

    /**
     * 展示收益数据
     * @param d 数据
     */
    private void showProfit(Rule d) {
        this.show(ProfitListPage.class, d);
    }

    /**
     * 构建弹窗
     */
    private void makeWindowStructure() {
        ViewTemplate t = template;
        page = t.linearPage(Gravity.CENTER,
                t.line(L_W, L_H, t.textView("名称：", L_W_T, L_H), name = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("开始日期：", L_W_T, L_H), date = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("金额基数：", L_W_T, L_H), base = t.numeric(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("波动比率：", L_W_T, L_H), ratio = t.numeric(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("扩大幅度：", L_W_T, L_H), expansion = t.numeric(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("状态：", L_W_T, L_H), statusSel = t.selector(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("规则类型：", L_W_T, L_H), typeSel = t.selector(L_W_C, L_H))
        );
        statusSel.init().mapper(this::statusName).refreshData(Arrays.asList("1", "0"));
        typeSel.init().mapper(this::typeName).refreshData(Arrays.asList("0", "1", "2"));
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
