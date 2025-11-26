package org.nature.biz.etf.page;

import android.view.Gravity;
import android.widget.LinearLayout;
import org.nature.biz.etf.manager.HoldManager;
import org.nature.biz.etf.mapper.RuleMapper;
import org.nature.biz.etf.model.Item;
import org.nature.biz.etf.model.Rule;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.*;

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
    private RuleMapper ruleMapper;
    @Injection
    private HoldManager holdManager;

    private LinearLayout page;
    private Input name, base, ratio, date, expansion;
    private Selector<String> statusSel, typeSel;
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
    private Button add;

    @Override
    protected List<Table.Header<Rule>> headers() {
        return headers;
    }

    @Override
    protected List<Rule> listData() {
        Item d = this.getParam();
        return ruleMapper.listByItem(d.getCode(), d.getType());
    }

    @Override
    protected void initHeaderViews(LinearLayout condition) {
        condition.addView(add = template.button("+", 3, 7));
    }

    @Override
    protected void initHeaderBehaviours() {
        ClickUtil.onClick(add, this::add);
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
        template.confirm("新增", page, () -> this.doEdit(this::save));
    }

    /**
     * 编辑操作
     * @param d 数据
     */
    private void edit(Rule d) {
        this.makeWindowStructure();
        this.name.setValue(d.getName());
        this.date.setValue(d.getDate());
        this.base.setValue(d.getBase().toPlainString());
        this.ratio.setValue(d.getRatio().toPlainString());
        this.expansion.setValue(d.getExpansion().toPlainString());
        this.typeSel.setValue(d.getRuleType());
        this.statusSel.setValue(d.getStatus());
        template.confirm("编辑-" + d.getName(), page, () -> this.doEdit(ruleMapper::merge));
    }

    /**
     * 执行编辑操作
     * @param consumer 处理逻辑
     */
    private void doEdit(Consumer<Rule> consumer) {
        String name = this.name.getValue();
        Warn.check(name::isEmpty, "请填写名称");
        String date = this.date.getValue();
        if (date.isEmpty()) {
            date = null;
        }
        String base = this.base.getValue();
        Warn.check(base::isEmpty, "请填写金额基数");
        String ratio = this.ratio.getValue();
        Warn.check(ratio::isEmpty, "请填写波动比率");
        String expansion = this.expansion.getValue();
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
        template.alert("编辑成功！");
    }

    /**
     * 删除操作
     * @param d 数据
     */
    private void delete(Rule d) {
        template.confirm("删除项目-" + d.getName(), "确认删除吗？", () -> {
            ruleMapper.deleteById(d);
            this.refreshData();
            template.alert("删除成功！");
        });
    }

    /**
     * 计算收益
     * @param d 数据
     */
    private void calcProfit(Rule d) {
        int i = holdManager.calc(d);
        template.alert("持仓收益计算完成，数据量：" + i);
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
                t.line(L_W, L_H, t.text("名称：", L_W_T, L_H), name = t.input(L_W_C, L_H)),
                t.line(L_W, L_H, t.text("开始日期：", L_W_T, L_H), date = t.input(L_W_C, L_H)),
                t.line(L_W, L_H, t.text("金额基数：", L_W_T, L_H), base = t.decimal(L_W_C, L_H)),
                t.line(L_W, L_H, t.text("波动比率：", L_W_T, L_H), ratio = t.decimal(L_W_C, L_H)),
                t.line(L_W, L_H, t.text("扩大幅度：", L_W_T, L_H), expansion = t.decimal(L_W_C, L_H)),
                t.line(L_W, L_H, t.text("状态：", L_W_T, L_H), statusSel = t.selector(L_W_C, L_H)),
                t.line(L_W, L_H, t.text("规则类型：", L_W_T, L_H), typeSel = t.selector(L_W_C, L_H))
        );
        statusSel.mapper(this::statusName);
        statusSel.setData(Arrays.asList("1", "0"));
        typeSel.mapper(this::typeName);
        typeSel.setData(Arrays.asList("0", "1", "2", "3"));
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
        return Map.of("0", "网格", "1", "网格定投", "3", "网格加投", "2", "复利").get(i);
    }

    private void save(Rule rule) {
        Rule exists = ruleMapper.findById(rule);
        // 数据已存在
        Warn.check(() -> exists != null, "数据已存在");
        ruleMapper.save(rule);
    }

}
