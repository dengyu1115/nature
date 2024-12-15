package org.nature.common.page;

import android.view.Gravity;
import android.widget.LinearLayout;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.holder.PageHolder;
import org.nature.common.model.Menu;
import org.nature.common.model.PageInfo;
import org.nature.common.view.Button;
import org.nature.common.view.Tab;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 主页面
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/9
 */
@SuppressWarnings("unchecked")
@Component
public class MainPage extends Page {

    private final List<String> tabs = Arrays.asList("基础", "ETF", "债券");

    private LinearLayout body;

    private Tab<Menu> tab;

    @Override
    protected void makeStructure() {
        page.setOrientation(LinearLayout.VERTICAL);
        this.header();
        this.body();
    }

    @Override
    protected void onShow() {
        tab.onChange(this::showMain);
        tab.setData(tabs.stream().map(i -> {
            List<List<PageInfo>> list = PageHolder.get(i);
            Menu menu = new Menu();
            menu.setName(i);
            menu.setList(list);
            return menu;
        }).collect(Collectors.toList()));
    }

    /**
     * 头部布局
     */
    private void header() {
        this.tab = template.tab(100, 7, 10);
        this.tab.setMapper(Menu::getName);
        page.addView(tab);
    }

    /**
     * 主体布局
     */
    private void body() {
        this.body = template.line(100, 93);
        this.body.setGravity(Gravity.START);
        page.addView(body);
    }

    /**
     * 展示主体
     * @param menu 菜单数据
     */
    private void showMain(Menu menu) {
        this.body.removeAllViews();
        if (menu == null) {
            return;
        }
        for (List<PageInfo> list : menu.getList()) {
            this.listMenu(list);
        }
    }

    /**
     * 菜单列表
     * @param pages 页面信息集合
     */
    private void listMenu(List<PageInfo> pages) {
        LinearLayout line = template.line(20, 93);
        line.setOrientation(LinearLayout.VERTICAL);
        line.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        this.body.addView(line);
        for (PageInfo page : pages) {
            line.addView(template.text("", 10, 2));
            line.addView(this.menuBtn(page.getName(), page.getCls()));
        }
    }


    /**
     * 菜单按钮
     * @param name 名称
     * @param clz  页面类
     * @return Button
     */
    private Button menuBtn(String name, Class<? extends Page> clz) {
        Button btn = template.button(name, 10, 7);
        btn.setBtnBackground(template.background("empty"));
        btn.onClick(() -> this.show(clz));
        return btn;
    }
}
