package org.nature.biz.etf.page;

import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.apache.commons.lang3.StringUtils;
import org.nature.biz.common.model.KInfo;
import org.nature.biz.common.page.KlineListPage;
import org.nature.biz.etf.manager.HoldManager;
import org.nature.biz.etf.mapper.ItemMapper;
import org.nature.biz.etf.model.Item;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.PopUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.common.view.TableView;
import org.nature.common.view.ViewTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.nature.common.constant.Const.*;

/**
 * 项目维护
 * @author Nature
 * @version 1.0.0
 * @since 2023/12/29
 */
@PageView(name = "项目", group = "ETF", col = 1, row = 1)
public class ItemListPage extends ListPage<Item> {

    @Injection
    private ItemMapper itemMapper;
    @Injection
    private HoldManager holdManager;
    /**
     * 关键字
     */
    private EditText keyword;
    /**
     * 编辑弹窗
     */
    private LinearLayout editPop;
    /**
     * 编号、名称
     */
    private EditText code, name;
    /**
     * 类型下拉选项
     */
    private Selector<String> type;
    /**
     * 新增、加载K线、重新加载K线、计算规则按钮
     */
    private Button add, calcRule;
    /**
     * 表头
     */
    private final List<TableView.D<Item>> ds = Arrays.asList(
            TableView.row("名称", d -> TextUtil.text(d.getName()), C, S, Item::getName),
            TableView.row("编号", d -> TextUtil.text(d.getCode()), C, C, Item::getCode),
            TableView.row("类型", d -> TextUtil.text(d.getType()), C, C, Item::getType),
            TableView.row("编辑", d -> "+", C, C, this::edit),
            TableView.row("删除", d -> "-", C, C, this::delete),
            TableView.row("K线查看", d -> "查看", C, C, this::showKline),
            TableView.row("规则查看", d -> "查看", C, C, this::showRule)
    );

    @Override
    protected List<TableView.D<Item>> define() {
        return ds;
    }

    @Override
    protected List<Item> listData() {
        List<Item> list = itemMapper.listAll();
        String keyword = this.keyword.getText().toString();
        if (StringUtils.isNotBlank(keyword)) {
            list = list.stream().filter(i -> i.getName().contains(keyword)).collect(Collectors.toList());
        }
        return list;
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(add = template.button("+", 30, 30));
        searchBar.addConditionView(keyword = template.editText(100, 30));
        searchBar.addConditionView(calcRule = template.button("规则计算", 80, 30));
    }

    @Override
    protected void initHeaderBehaviours() {
        ClickUtil.onClick(add, this::add);
        ClickUtil.onAsyncClick(calcRule, this::calcHold);
    }

    @Override
    protected int getTotalColumns() {
        return ds.size();
    }

    @Override
    protected int getFixedColumns() {
        return 3;
    }

    /**
     * 添加
     */
    private void add() {
        this.makeWindowStructure();
        PopUtil.confirm(context, "新增项目", editPop, () -> this.doEdit(this::save));
    }

    /**
     * 编辑
     * @param d 数据
     */
    private void edit(Item d) {
        this.makeWindowStructure();
        this.code.setText(d.getCode());
        this.name.setText(d.getName());
        this.type.setValue(d.getType());
        PopUtil.confirm(context, "编辑项目-" + d.getName(), editPop, () -> this.doEdit(itemMapper::merge));
    }

    /**
     * 编辑操作
     * @param consumer 编辑逻辑
     */
    private void doEdit(Consumer<Item> consumer) {
        String code = this.code.getText().toString();
        Warn.check(code::isEmpty, "请填写编号");
        String name = this.name.getText().toString();
        Warn.check(name::isEmpty, "请填写名称");
        String type = this.type.getValue();
        Warn.check(type::isEmpty, "请选择类型");
        Item item = new Item();
        item.setCode(code);
        item.setName(name);
        item.setType(type);
        consumer.accept(item);
        this.refreshData();
        PopUtil.alert(context, "编辑成功！");
    }

    /**
     * 删除操作
     * @param d 数据
     */
    private void delete(Item d) {
        PopUtil.confirm(context, "删除项目-" + d.getName(), "确认删除吗？", () -> {
            itemMapper.deleteById(d);
            this.refreshData();
            PopUtil.alert(context, "删除成功！");
        });
    }

    /**
     * 计算持仓数据
     * @return 提示信息
     */
    private String calcHold() {
        return "计算完成，数据量：" + holdManager.calc();
    }

    /**
     * 显示K线
     * @param d 数据
     */
    private void showKline(Item d) {
        KInfo info = new KInfo();
        info.setCode(d.getCode());
        info.setType(d.getType());
        info.setName(d.getName());
        this.show(KlineListPage.class, info);
    }

    /**
     * 显示规则
     * @param d 数据
     */
    private void showRule(Item d) {
        this.show(RuleListPage.class, d);
    }

    /**
     * 构建弹窗结构
     */
    private void makeWindowStructure() {
        ViewTemplate t = template;
        editPop = t.linearPage(Gravity.CENTER,
                t.line(L_W, L_H, t.textView("编号：", L_W_T, L_H), code = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("名称：", L_W_T, L_H), name = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("类型：", L_W_T, L_H), type = t.selector(L_W_C, L_H)),
                t.line(L_W, L_H)
        );
        type.mapper(i -> i).init().refreshData(Arrays.asList("0", "1"));
    }

    /**
     * 保存
     * @param item 项目
     */
    private void save(Item item) {
        Item exists = itemMapper.findById(item);
        // 数据已存在
        Warn.check(() -> exists != null, "数据已存在");
        // 保存
        itemMapper.save(item);
    }

}
