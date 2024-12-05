package org.nature.biz.etf.page;

import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.nature.biz.etf.manager.HoldManager;
import org.nature.biz.etf.mapper.RuleMapper;
import org.nature.biz.etf.model.Item;
import org.nature.biz.etf.model.Rule;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.PopupUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.common.view.Table;
import org.nature.common.view.ViewTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.nature.common.constant.Const.L_H;
import static org.nature.common.constant.Const.L_W;

/**
 * 规则页面
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@PageView(name = "规则", group = "", col = 0, row = 0)
public class RuleListPage extends ListPage<Rule> {

    @Injection
    private RuleMapper ruleMapper;
    @Injection
    private HoldManager holdManager;

    private LinearLayout page;
    private EditText name, base, ratio, date, expansion;
    private Selector<String> statusSel, typeSel;
    private Button add;

    private final List<Table.Header<Rule>> headers = Arrays.asList(
            Table.header("名称", d -> TextUtil.text(d.getName()), C, S, Rule::getName),
            Table.header("规则类型", d -> TextUtil.text(this.typeName(d.getRuleType())), C, C, Rule::getRuleType),
            Table.header("状态", d -> TextUtil.text(this.statusName(d.getStatus())), C, C, Rule::getStatus),
            Table.header("编辑", d -> "+", C, C, this::edit),
            Table.header("删除", d -> "-", C, C, this::delete),
            Table.header("持仓计算", d -> "计算", C, C, this::calcProfit),
            Table.header("持仓查看", d -> "查看", C, C, this::showHold),
            Table.header("收益查看", d -> "查看", C, C, this::showProfit),
            Table.header("开始日期", d -> TextUtil.text(d.getDate()), C, C, Rule::getDate),
            Table.header("金额基数", d -> TextUtil.text(d.getBase()), C, C, Rule::getBase),
            Table.header("波动比率", d -> TextUtil.text(d.getRatio()), C, C, Rule::getRatio),
            Table.header("扩大幅度", d -> TextUtil.text(d.getExpansion()), C, C, Rule::getExpansion)
    );

    @Override
    protected List<Table.Header<Rule>> define() {
        return headers;
    }

    @Override
    protected List<Rule> listData() {
        Item d = this.getParam();
        return ruleMapper.listByItem(d.getCode(), d.getType());
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(add = template.button("+", 3, 7));
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
        PopupUtil.confirm(context, "新增", page, () -> this.doEdit(this::save));
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
        PopupUtil.confirm(context, "编辑-" + d.getName(), page, () -> this.doEdit(ruleMapper::merge));
    }

    /**
     * 执行编辑操作
     * @param consumer 处理逻辑
     */
    private void doEdit(Consumer<Rule> consumer) {
        String name = this.name.getText().toString();
        Warn.check(name::isEmpty, "请填写名称");
        String date = this.date.getText().toString();
        if (date.isEmpty()) {
            date = null;
        }
        String base = this.base.getText().toString();
        Warn.check(base::isEmpty, "请填写金额基数");
        String ratio = this.ratio.getText().toString();
        Warn.check(ratio::isEmpty, "请填写波动比率");
        String expansion = this.expansion.getText().toString();
        Warn.check(expansion::isEmpty, "请填写扩大幅度");
        String status = this.statusSel.getValue();
        Warn.check(status::isEmpty, "请选择状态");
        String type = this.typeSel.getValue();
        Warn.check(type::isEmpty, "请选择规则类型");
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
        PopupUtil.alert(context, "编辑成功！");
    }

    /**
     * 删除操作
     * @param d 数据
     */
    private void delete(Rule d) {
        PopupUtil.confirm(context, "删除项目-" + d.getName(), "确认删除吗？", () -> {
            ruleMapper.deleteById(d);
            this.refreshData();
            PopupUtil.alert(context, "删除成功！");
        });
    }

    /**
     * 计算收益
     * @param d 数据
     */
    private void calcProfit(Rule d) {
        int i = holdManager.calc(d);
        PopupUtil.alert(context, "持仓收益计算完成，数据量：" + i);
    }

    /**
     * 展示持有数据
     * @param d 数据
     */
    private void showHold(Rule d) {
        this.show(HistoryBsPage.class, d);
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
        page = t.block(Gravity.CENTER,
                t.line(21, 7 , t.text("名称：", 8, 7), name = t.input(12, 7 )),
                t.line(21, 7 , t.text("开始日期：", 8, 7), date = t.input(12, 7 )),
                t.line(21, 7 , t.text("金额基数：", 8, 7), base = t.decimal(12, 7 )),
                t.line(21, 7 , t.text("波动比率：", 8, 7), ratio = t.decimal(12, 7 )),
                t.line(21, 7 , t.text("扩大幅度：", 8, 7), expansion = t.decimal(12, 7 )),
                t.line(21, 7 , t.text("状态：", 8, 7), statusSel = t.selector(12, 7 )),
                t.line(21, 7 , t.text("规则类型：", 8, 7), typeSel = t.selector(12, 7 ))
        );
        statusSel.mapper(this::statusName);
        statusSel.refreshData(Arrays.asList("1", "0"));
        typeSel.mapper(this::typeName);
        typeSel.refreshData(Arrays.asList("0", "1", "2"));
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

    private void save(Rule rule) {
        Rule exists = ruleMapper.findById(rule);
        // 数据已存在
        Warn.check(() -> exists != null, "数据已存在");
        ruleMapper.save(rule);
    }

}
