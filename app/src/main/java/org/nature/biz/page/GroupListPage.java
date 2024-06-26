package org.nature.biz.page;

import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.apache.commons.lang3.StringUtils;
import org.nature.biz.mapper.GroupMapper;
import org.nature.biz.model.Group;
import org.nature.common.exception.Warn;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.PopUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.TableView;
import org.nature.common.view.ViewTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.nature.common.constant.Const.*;

/**
 * 项目分组
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@PageView(name = "分组", group = "ETF", col = 1, row = 1)
public class GroupListPage extends ListPage<Group> {

    @Injection
    private GroupMapper groupMapper;

    private EditText keyword;
    private LinearLayout page;
    private EditText code, name;
    private Button add;

    private final List<TableView.D<Group>> ds = Arrays.asList(
            TableView.row("名称", d -> TextUtil.text(d.getName()), C, S, Group::getName),
            TableView.row("code", d -> TextUtil.text(d.getCode()), C, C, Group::getCode),
            TableView.row("编辑", d -> "+", C, C, this::edit),
            TableView.row("删除", d -> "-", C, C, this::delete)
    );

    @Override
    protected List<TableView.D<Group>> define() {
        return ds;
    }

    @Override
    protected List<Group> listData() {
        List<Group> list = groupMapper.listAll();
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
    }

    @Override
    protected void initHeaderBehaviours() {
        add.setOnClickListener(v -> this.add());
    }

    @Override
    protected int getTotalColumns() {
        return ds.size();
    }

    /**
     * 添加操作
     */
    private void add() {
        this.makeWindowStructure();
        PopUtil.confirm(context, "新增项目", page, () -> this.doEdit(this::save));
    }

    /**
     * 编辑操作
     * @param d 数据
     */
    private void edit(Group d) {
        this.makeWindowStructure();
        this.code.setText(d.getCode());
        this.name.setText(d.getName());
        PopUtil.confirm(context, "编辑项目-" + d.getName(), page, () -> this.doEdit(groupMapper::merge));
    }

    /**
     * 执行编辑操作
     * @param consumer 处理逻辑
     */
    private void doEdit(Consumer<Group> consumer) {
        String code = this.code.getText().toString();
        if (code.isEmpty()) {
            throw new RuntimeException("请填写编号");
        }
        String name = this.name.getText().toString();
        if (name.isEmpty()) {
            throw new RuntimeException("请填写名称");
        }
        Group item = new Group();
        item.setCode(code);
        item.setName(name);
        consumer.accept(item);
        this.refreshData();
        PopUtil.alert(context, "编辑成功！");
    }

    /**
     * 删除操作
     * @param d 删除对象
     */
    private void delete(Group d) {
        PopUtil.confirm(context, "删除项目-" + d.getName(), "确认删除吗？", () -> {
            groupMapper.deleteById(d.getCode());
            this.refreshData();
            PopUtil.alert(context, "删除成功！");
        });
    }

    /**
     * 创建弹窗
     */
    private void makeWindowStructure() {
        ViewTemplate t = template;
        page = t.linearPage(Gravity.CENTER,
                t.line(L_W, L_H, t.textView("分组编号：", L_W_T, L_H), code = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H, t.textView("分组名称：", L_W_T, L_H), name = t.editText(L_W_C, L_H)),
                t.line(L_W, L_H)
        );
    }

    /**
     * 保存
     * @param group 分组
     */
    private void save(Group group) {
        Group exists = groupMapper.findById(group.getCode());
        // 分组已存在
        if (exists != null) {
            throw new Warn("datum exists");
        }
        // 保存
        groupMapper.save(group);
    }

}
