package org.nature.biz.page;

import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.nature.biz.mapper.BoundMapper;
import org.nature.biz.model.Bound;
import org.nature.common.exception.Warn;
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
import java.util.function.Consumer;

import static org.nature.common.constant.Const.*;

/**
 * 项目维护
 * @author Nature
 * @version 1.0.0
 * @since 2023/12/29
 */
@PageView(name = "债券列表", group = "ETF", col = 3, row = 1)
public class BoundListPage extends ListPage<Bound> {

    @Injection
    private BoundMapper boundMapper;

    /**
     * 编辑弹窗
     */
    private LinearLayout editPop;
    /**
     * 编号、名称
     */
    private EditText code, name, bound, ratio;
    /**
     * 类型下拉选项
     */
    private Selector<String> type;
    /**
     * 新增、加载K线、重新加载K线、计算规则按钮
     */
    private Button add;
    /**
     * 表头
     */
    private final List<TableView.D<Bound>> ds = Arrays.asList(
            TableView.row("名称", d -> TextUtil.text(d.getName()), C, S, Bound::getName),
            TableView.row("编号", d -> TextUtil.text(d.getCode()), C, C, Bound::getCode),
            TableView.row("类型", d -> TextUtil.text(d.getType()), C, C, Bound::getType),
            TableView.row("基金编号", d -> TextUtil.text(d.getBound()), C, C, Bound::getBound),
            TableView.row("系数", d -> TextUtil.text(d.getRatio()), C, C, Bound::getRatio),
            TableView.row("编辑", d -> "+", C, C, this::edit),
            TableView.row("删除", d -> "-", C, C, this::delete)
    );

    @Override
    protected List<TableView.D<Bound>> define() {
        return ds;
    }

    @Override
    protected List<Bound> listData() {
        return boundMapper.listAll();
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
        return 7;
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
     * 保存
     * @param bound 数据
     */
    private void save(Bound bound) {
        Bound exist = boundMapper.findById(bound);
        if (exist != null) {
            throw new Warn("数据已存在：" + exist);
        }
        boundMapper.save(bound);
    }

    /**
     * 编辑
     * @param d 数据
     */
    private void edit(Bound d) {
        this.makeWindowStructure();
        this.code.setText(d.getCode());
        this.name.setText(d.getName());
        this.type.setValue(d.getType());
        PopUtil.confirm(context, "编辑项目-" + d.getName(), editPop, () -> this.doEdit(boundMapper::merge));
    }

    /**
     * 编辑操作
     * @param consumer 编辑逻辑
     */
    private void doEdit(Consumer<Bound> consumer) {
        String code = this.code.getText().toString();
        if (code.isEmpty()) {
            throw new Warn("请填写编号");
        }
        String name = this.name.getText().toString();
        if (name.isEmpty()) {
            throw new Warn("请填写名称");
        }
        String type = this.type.getValue();
        if (type.isEmpty()) {
            throw new Warn("请选择类型");
        }
        String bound = this.bound.getText().toString();
        if (bound.isEmpty()) {
            throw new Warn("请填写基金编号");
        }
        String ratio = this.ratio.getText().toString();
        if (ratio.isEmpty()) {
            throw new Warn("请选择分组");
        }
        Bound item = new Bound();
        item.setCode(code);
        item.setName(name);
        item.setType(type);
        item.setBound(bound);
        item.setRatio(new BigDecimal(ratio));
        consumer.accept(item);
        this.refreshData();
        PopUtil.alert(context, "编辑成功！");
    }

    /**
     * 删除
     * @param d 数据
     */
    private void delete(Bound d) {
        PopUtil.confirm(context, "删除项目-" + d.getName(), "确认删除吗？", () -> {
            boundMapper.deleteById(d);
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
                t.line(L_W, L_H, t.textView("编号：", L_W_T, L_H), code = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("名称：", L_W_T, L_H), name = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("类型：", L_W_T, L_H), type = t.selector(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("基金编号：", L_W_T, L_H), bound = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("系数：", L_W_T, L_H), ratio = t.numeric(L_W_C, L_H)),
                t.line(L_W, L_H)
        );
        type.mapper(i -> i).init().refreshData(Arrays.asList("0", "1"));
    }

}
