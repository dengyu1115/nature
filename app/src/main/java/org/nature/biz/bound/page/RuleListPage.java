package org.nature.biz.bound.page;

import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.nature.biz.bound.manager.RuleManager;
import org.nature.biz.bound.mapper.RuleMapper;
import org.nature.biz.bound.model.Rule;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.ClickUtil;
import org.nature.common.view.Popup;
import org.nature.common.util.TextUtil;
import org.nature.common.view.Selector;
import org.nature.common.view.Table;
import org.nature.common.view.ViewTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 规则页面
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/6
 */
@PageView(name = "规则配置", group = "债券", col = 1, row = 1)
public class RuleListPage extends ListPage<Rule> {

    @Injection
    private RuleMapper ruleMapper;
    @Injection
    private RuleManager ruleManager;

    private LinearLayout page;
    private EditText code, name, dateStart, dateEnd, days, diff;
    private Selector<String> statusSel;
    private Button add;

    private final List<Table.Header<Rule>> headers = Arrays.asList(
            Table.header("编号", d -> TextUtil.text(d.getCode()), C, S, Rule::getCode),
            Table.header("名称", d -> TextUtil.text(d.getName()), C, S, Rule::getName),
            Table.header("开始日期", d -> TextUtil.text(d.getDateStart()), C, C, Rule::getDateStart),
            Table.header("结束日期", d -> TextUtil.text(d.getDateEnd()), C, C, Rule::getDateEnd),
            Table.header("数据天数", d -> TextUtil.text(d.getDays()), C, C, Rule::getDays),
            Table.header("出发差值", d -> TextUtil.text(d.getDiff()), C, C, Rule::getDiff),
            Table.header("状态", d -> TextUtil.text(this.statusName(d.getStatus())), C, C, Rule::getStatus),
            Table.header("项目集合", d -> "+", C, C, this::items),
            Table.header("计算", d -> "+", C, C, this::calc),
            Table.header("编辑", d -> "+", C, C, this::edit),
            Table.header("删除", d -> "-", C, C, this::delete)
    );

    @Override
    protected List<Table.Header<Rule>> headers() {
        return headers;
    }

    @Override
    protected List<Rule> listData() {
        return ruleMapper.listAll();
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
        return 9;
    }

    @Override
    protected int getFixedColumns() {
        return 7;
    }

    /**
     * 添加操作
     */
    private void add() {
        this.makeWindowStructure();
        this.popup.confirm("新增", page, () -> this.doEdit(this::save));
    }

    /**
     * 编辑操作
     * @param d 数据
     */
    private void edit(Rule d) {
        this.makeWindowStructure();
        this.code.setText(d.getCode());
        this.name.setText(d.getName());
        this.dateStart.setText(d.getDateStart());
        this.dateEnd.setText(d.getDateEnd());
        this.days.setText(String.valueOf(d.getDays()));
        this.diff.setText(d.getDiff().toPlainString());
        this.statusSel.setValue(d.getStatus());
        this.popup.confirm("编辑-" + d.getName(), page, () -> this.doEdit(ruleMapper::merge));
    }

    /**
     * 执行编辑操作
     * @param consumer 处理逻辑
     */
    private void doEdit(Consumer<Rule> consumer) {
        String code = this.code.getText().toString();
        Warn.check(code::isEmpty, "请填写名称");
        String name = this.name.getText().toString();
        Warn.check(name::isEmpty, "请填写名称");
        String dateStart = this.dateStart.getText().toString();
        Warn.check(dateStart::isEmpty, "请填写开始日期");
        String dateEnd = this.dateEnd.getText().toString();
        String days = this.days.getText().toString();
        Warn.check(days::isEmpty, "请数据天数");
        String diff = this.diff.getText().toString();
        Warn.check(diff::isEmpty, "请填写触发差值");
        String status = this.statusSel.getValue();
        Warn.check(status::isEmpty, "请选择状态");
        Rule rule = new Rule();
        rule.setCode(code);
        rule.setName(name);
        rule.setDateStart(dateStart);
        rule.setDateEnd(dateEnd);
        rule.setDays(Integer.parseInt(days));
        rule.setDiff(new BigDecimal(diff));
        rule.setStatus(status);
        consumer.accept(rule);
        this.refreshData();
        this.popup.alert("编辑成功！");
    }

    /**
     * 展示持有数据
     * @param d 数据
     */
    private void calc(Rule d) {
        this.show(ResultListPage.class, d);
    }

    /**
     * 删除操作
     * @param d 数据
     */
    private void delete(Rule d) {
        this.popup.confirm("删除项目-" + d.getName(), "确认删除吗？", () -> {
            ruleMapper.deleteById(d.getCode());
            this.refreshData();
            this.popup.alert("删除成功！");
        });
    }

    /**
     * 展示持有数据
     * @param d 数据
     */
    private void items(Rule d) {
        this.show(ItemListPage.class, d.getCode());
    }

    /**
     * 构建弹窗
     */
    private void makeWindowStructure() {
        ViewTemplate t = template;
        page = t.block(Gravity.CENTER,
                t.line(21, 7 , t.text("编号：", 8, 7), code = t.input(12, 7 )),
                t.line(21, 7 , t.text("名称：", 8, 7), name = t.input(12, 7 )),
                t.line(21, 7 , t.text("开始日期：", 8, 7), dateStart = t.input(12, 7 )),
                t.line(21, 7 , t.text("结束日期：", 8, 7), dateEnd = t.input(12, 7 )),
                t.line(21, 7 , t.text("数据天数：", 8, 7), days = t.number(12, 7 )),
                t.line(21, 7 , t.text("触发差值：", 8, 7), diff = t.decimal(12, 7 )),
                t.line(21, 7 , t.text("状态：", 8, 7), statusSel = t.selector(12, 7 ))
        );
        statusSel.mapper(this::statusName);
        statusSel.refreshData(Arrays.asList("1", "0"));
    }

    /**
     * 获取状态名称
     * @param i 状态code
     * @return String
     */
    private String statusName(String i) {
        return "1".equals(i) ? "启用" : "暂停";
    }

    private void save(Rule rule) {
        Rule exists = ruleMapper.findById(rule.getCode());
        // 数据已存在
        Warn.check(() -> exists != null, "数据已存在");
        ruleMapper.save(rule);
    }

}
