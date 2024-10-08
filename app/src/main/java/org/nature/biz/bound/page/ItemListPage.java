package org.nature.biz.bound.page;

import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.nature.biz.bound.mapper.ItemMapper;
import org.nature.biz.bound.model.Item;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.nature.common.constant.Const.*;

/**
 * 项目维护
 * @author Nature
 * @version 1.0.0
 * @since 2023/12/29
 */
@PageView(name = "项目", group = "债券", col = 0, row = 0)
public class ItemListPage extends ListPage<Item> {

    @Injection
    private ItemMapper itemMapper;

    /**
     * 编辑弹窗
     */
    private LinearLayout editPop;
    /**
     * 编号、名称
     */
    private EditText code, name, fund, ratio;
    /**
     * 类型下拉选项
     */
    private Selector<String> type;
    /**
     * 新增、加载K线、重新加载K线、计算规则按钮
     */
    private Button add;

    private Selector<Item> item;
    /**
     * 表头
     */
    private final List<TableView.D<Item>> ds = Arrays.asList(
            TableView.row("名称", d -> TextUtil.text(d.getName()), C, S, Item::getName),
            TableView.row("编号", d -> TextUtil.text(d.getCode()), C, C, Item::getCode),
            TableView.row("类型", d -> TextUtil.text(d.getType()), C, C, Item::getType),
            TableView.row("基金编号", d -> TextUtil.text(d.getFund()), C, C, Item::getType),
            TableView.row("比例系数", d -> TextUtil.text(d.getRatio()), C, C, Item::getRatio),
            TableView.row("编辑", d -> "+", C, C, this::edit),
            TableView.row("删除", d -> "-", C, C, this::delete)
    );

    @Override
    protected List<TableView.D<Item>> define() {
        return ds;
    }

    @Override
    protected List<Item> listData() {
        String rule = this.getParam();
        return itemMapper.listByRule(rule);
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(add = template.button("+", 30, 30));
    }

    @Override
    protected void initHeaderBehaviours() {
        ClickUtil.onClick(add, this::add);
    }

    @Override
    protected int getTotalColumns() {
        return ds.size();
    }

    @Override
    protected int getFixedColumns() {
        return ds.size();
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
        this.fund.setText(d.getFund());
        this.ratio.setText(d.getRatio().toPlainString());
        PopUtil.confirm(context, "编辑项目-" + d.getName(), editPop, () -> this.doEdit(itemMapper::merge));
    }

    /**
     * 编辑操作
     * @param consumer 编辑逻辑
     */
    private void doEdit(Consumer<Item> consumer) {
        String code = this.code.getText().toString();
        Warn.check(code::isEmpty, "请填写编号");
        String type = this.type.getValue();
        Warn.check(type::isEmpty, "请选择类型");
        String name = this.name.getText().toString();
        Warn.check(name::isEmpty, "请填写名称");
        String fund = this.fund.getText().toString();
        Warn.check(fund::isEmpty, "请填写基金编号");
        String ratio = this.ratio.getText().toString();
        Warn.check(ratio::isEmpty, "请填写比例系数");
        Item item = new Item();
        item.setRule(this.getParam());
        item.setCode(code);
        item.setType(type);
        item.setName(name);
        item.setFund(fund);
        item.setRatio(new BigDecimal(ratio));
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
     * 构建弹窗结构
     */
    private void makeWindowStructure() {
        ViewTemplate t = template;
        editPop = t.linearPage(Gravity.CENTER,
                t.line(L_W, L_H, t.textView("选择复制:", L_W_T, L_H), item = t.selector(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("名称：", L_W_T, L_H), name = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("编号：", L_W_T, L_H), code = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("类型：", L_W_T, L_H), type = t.selector(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("基金编号：", L_W_T, L_H), fund = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("比例系数：", L_W_T, L_H), ratio = t.numeric(L_W_C, L_H)),
                t.line(L_W, L_H)
        );
        type.mapper(i -> i).init().refreshData(Arrays.asList("0", "1"));
        item.mapper(i -> {
            if (i == null) {
                return "请选择";
            }
            return i.getName();
        }).init().refreshData(this.listItems());
        item.onChangeRun(() -> {
            Item d = item.getValue();
            if (d == null) {
                return;
            }
            this.code.setText(d.getCode());
            this.name.setText(d.getName());
            this.type.setValue(d.getType());
            this.fund.setText(d.getFund());
            this.ratio.setText(d.getRatio().toPlainString());
        });
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

    /**
     * 查询所有可选择项目集合
     * @return list
     */
    private List<Item> listItems() {
        List<Item> all = itemMapper.listAll();
        List<Item> list = itemMapper.listByRule(this.getParam());
        Set<String> set = list.stream().map(i -> String.join(DELIMITER, i.getCode(), i.getType()))
                .collect(Collectors.toSet());
        all = all.stream().filter(i -> {
            String key = String.join(DELIMITER, i.getCode(), i.getType());
            if (set.contains(key)) {
                return false;
            }
            set.add(key);
            return true;
        }).collect(Collectors.toList());
        all.add(0, null);
        return all;
    }

}
