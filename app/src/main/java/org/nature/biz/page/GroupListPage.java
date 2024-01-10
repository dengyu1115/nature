package org.nature.biz.page;

import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.apache.commons.lang3.StringUtils;
import org.nature.biz.manager.GroupManager;
import org.nature.biz.model.Group;
import org.nature.common.ioc.annotation.Injection;
import org.nature.common.ioc.annotation.PageView;
import org.nature.common.page.ListPage;
import org.nature.common.util.CommonUtil;
import org.nature.common.util.PopUtil;
import org.nature.common.util.TextUtil;
import org.nature.common.view.ExcelView;
import org.nature.common.view.SearchBar;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 项目分组
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/8
 */
@PageView(name = "分组", group = "ETF", col = 1, row = 1)
public class GroupListPage extends ListPage<Group> {

    @Injection
    private GroupManager groupManager;

    private EditText keyword;
    private LinearLayout page;
    private EditText code, name;
    private Button add;

    private final List<ExcelView.D<Group>> ds = Arrays.asList(
            ExcelView.row("名称", d -> TextUtil.text(d.getName()), C, S, CommonUtil.nullsLast(Group::getName)),
            ExcelView.row("code", d -> TextUtil.text(d.getCode()), C, C, CommonUtil.nullsLast(Group::getCode)),
            ExcelView.row("编辑", d -> "+", C, C, this.edit()),
            ExcelView.row("删除", d -> "-", C, C, this.delete())
    );

    @Override
    protected List<ExcelView.D<Group>> define() {
        return ds;
    }

    @Override
    protected List<Group> listData() {
        List<Group> list = groupManager.listAll();
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

    /**
     * 添加操作
     */
    private void add() {
        this.makeWindowStructure();
        PopUtil.confirm(context, "新增项目", page, () -> this.doEdit(groupManager::save));
    }

    /**
     * 编辑操作
     * @return 编辑操作
     */
    private Consumer<Group> edit() {
        return d -> {
            this.makeWindowStructure();
            this.code.setText(d.getCode());
            this.name.setText(d.getName());
            PopUtil.confirm(context, "编辑项目-" + d.getName(), page, () -> this.doEdit(groupManager::edit));
        };
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
     * @return 删除逻辑
     */
    private Consumer<Group> delete() {
        return d -> PopUtil.confirm(context, "删除项目-" + d.getName(), "确认删除吗？", () -> {
            groupManager.delete(d.getCode());
            this.refreshData();
            PopUtil.alert(context, "删除成功！");
        });
    }

    /**
     * 创建弹窗
     */
    private void makeWindowStructure() {
        page = template.linearPage();
        page.setGravity(Gravity.CENTER);
        LinearLayout cl = template.line(300, 30);
        LinearLayout nl = template.line(300, 30);
        LinearLayout el = template.line(300, 30);
        cl.addView(template.textView("分组编号：", 100, 30));
        cl.addView(code = template.editText(200, 30));
        nl.addView(template.textView("分组名称：", 100, 30));
        nl.addView(name = template.editText(200, 30));
        page.addView(cl);
        page.addView(nl);
        page.addView(el);
    }

}
