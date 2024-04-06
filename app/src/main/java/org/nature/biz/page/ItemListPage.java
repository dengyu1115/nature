package org.nature.biz.page;

import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.apache.commons.lang3.StringUtils;
import org.nature.biz.manager.HoldManager;
import org.nature.biz.manager.KlineManager;
import org.nature.biz.mapper.GroupMapper;
import org.nature.biz.mapper.ItemMapper;
import org.nature.biz.model.Group;
import org.nature.biz.model.Item;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.PopUtil;
import org.nature.common.util.Sorter;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Selector;
import org.nature.common.view.TableView;
import org.nature.common.view.ViewTemplate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.nature.common.constant.Const.*;

/**
 * 项目维护
 * @author Nature
 * @version 1.0.0
 * @since 2023/12/29
 */
@PageView(name = "项目", group = "ETF", col = 1, row = 2)
public class ItemListPage extends ListPage<Item> {

    @Injection
    private ItemMapper itemMapper;
    @Injection
    private GroupMapper groupMapper;
    @Injection
    private KlineManager klineManager;
    @Injection
    private HoldManager holdManager;

    /**
     * 项目分组
     */
    private Selector<Group> itemGroup;
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
     * 分组下拉选项
     */
    private Selector<Group> group;
    /**
     * 新增、加载K线、重新加载K线、计算规则按钮
     */
    private Button add, loadKline, reloadKline, calcRule;
    /**
     * 分组信息map
     */
    private Map<String, String> groupMap;
    /**
     * 表头
     */
    private final List<TableView.D<Item>> ds = Arrays.asList(
            TableView.row("名称", d -> TextUtil.text(d.getName()), C, S, Item::getName),
            TableView.row("编号", d -> TextUtil.text(d.getCode()), C, C, Item::getCode),
            TableView.row("类型", d -> TextUtil.text(d.getType()), C, C, Item::getType),
            TableView.row("分组", d -> TextUtil.text(groupMap.get(d.getGroup())), C, C, Sorter.nullsLast(d -> groupMap.get(d.getGroup()))),
            TableView.row("编辑", d -> "+", C, C, this::edit),
            TableView.row("删除", d -> "-", C, C, this::delete),
            TableView.row("K线加载", d -> "加载", C, C, this::loadKline),
            TableView.row("K线重载", d -> "加载", C, C, this::reloadKline),
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
        Group group = this.itemGroup.getValue();
        if (StringUtils.isNotBlank(group.getCode())) {
            list = list.stream().filter(i -> group.getCode().equals(i.getGroup())).collect(Collectors.toList());
        }
        return list;
    }

    @Override
    protected void initHeaderViews(SearchBar searchBar) {
        searchBar.addConditionView(add = template.button("+", 30, 30));
        searchBar.addConditionView(itemGroup = template.selector(80, 30));
        searchBar.addConditionView(keyword = template.editText(100, 30));
        searchBar.addConditionView(loadKline = template.button("K线加载", 80, 30));
        searchBar.addConditionView(reloadKline = template.button("K线重载", 80, 30));
        searchBar.addConditionView(calcRule = template.button("规则计算", 80, 30));
    }

    @Override
    protected void initHeaderBehaviours() {
        ClickUtil.onClick(add, this::add);
        ClickUtil.onAsyncClick(loadKline, this::loadKlineAll);
        ClickUtil.onPopConfirm(reloadKline, "K线重载", "确定重新加载全部K线数据？", this::reloadKlineAll);
        ClickUtil.onAsyncClick(calcRule, this::calcHold);
        List<Group> groups = groupMapper.listAll();
        groupMap = groups.stream().collect(Collectors.toMap(Group::getCode, Group::getName));
        groups.sort(Comparator.comparing(Group::getCode));
        groups.add(0, new Group());
        itemGroup.mapper(Group::getName).init().refreshData(groups);
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
        if (code.isEmpty()) {
            throw new RuntimeException("请填写编号");
        }
        String name = this.name.getText().toString();
        if (name.isEmpty()) {
            throw new RuntimeException("请填写名称");
        }
        String type = this.type.getValue();
        if (type.isEmpty()) {
            throw new RuntimeException("请选择类型");
        }
        Group group = this.group.getValue();
        if (group == null) {
            throw new RuntimeException("请选择分组");
        }
        Item item = new Item();
        item.setCode(code);
        item.setName(name);
        item.setType(type);
        item.setGroup(group.getCode());
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
     * 加载K线
     * @param d 数据
     */
    private void loadKline(Item d) {
        PopUtil.alert(context, "K线加载完成，数据量：" + klineManager.loadByItem(d));
    }

    /**
     * 重载K线
     * @param d 数据
     */
    private void reloadKline(Item d) {
        PopUtil.alert(context, "K线重载完成，数据量：" + klineManager.reloadByItem(d));
    }

    /**
     * 加载全部K线
     * @return 提示信息
     */
    private String loadKlineAll() {
        return "所有K线加载完成，数据量：" + klineManager.load();
    }

    /**
     * 重载全部K线
     * @return 提示信息
     */
    private String reloadKlineAll() {
        return "所有K线重载完成，数据量：" + klineManager.reload();
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
        this.show(KlineListPage.class, d);
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
                t.line(L_W, L_H, t.textView("分组：", L_W_T, L_H), group = t.selector(L_W_C, L_H)),
                t.line(L_W, L_H)
        );
        type.mapper(i -> i).init().refreshData(Arrays.asList("0", "1"));
        group.mapper(Group::getName).init().refreshData(groupMapper.listAll());
    }

    /**
     * 保存
     * @param item 项目
     */
    private void save(Item item) {
        Item exists = itemMapper.findById(item);
        // 数据已存在
        if (exists != null) {
            throw new Warn("数据已存在");
        }
        // 保存
        itemMapper.save(item);
    }

}
