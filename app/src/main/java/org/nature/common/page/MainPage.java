package org.nature.common.page;

import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.holder.PageHolder;
import org.nature.common.model.PageInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private final List<Button> tabBtnList = new ArrayList<>();

    private LinearLayout body;

    @Override
    protected void makeStructure() {
        page.setOrientation(LinearLayout.VERTICAL);
        this.header();
        this.body();
    }

    @Override
    protected void onShow() {
        this.showMain(tabBtnList.get(0));
    }

    /**
     * 头部布局
     */
    private void header() {
        LinearLayout header = template.line(100, 7);
        header.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        page.addView(header);
        for (String i : tabs) {
            header.addView(this.tabBtn(i));
        }
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
     * @param btn tab按钮
     */
    private void showMain(Button btn) {
        this.body.removeAllViews();
        List<List<PageInfo>> tag = (List<List<PageInfo>>) btn.getTag();
        if (tag == null) {
            return;
        }
        tabBtnList.forEach(b -> {
            b.setClickable(b != btn);
            if (b == btn) {
                b.setBackground(template.background("success"));
            } else {
                b.setBackground(template.background("primary"));
            }
        });
        for (List<PageInfo> list : tag) {
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
     * TAB按钮
     * @param name 名称
     * @return Button
     */
    private Button tabBtn(String name) {
        Button btn = template.button(name, 10, 7);
        tabBtnList.add(btn);
        btn.setTag(PageHolder.get(name));
        btn.setOnClickListener(v -> this.showMain(btn));
        return btn;
    }

    /**
     * 菜单按钮
     * @param name 名称
     * @param clz  页面类
     * @return Button
     */
    private Button menuBtn(String name, Class<? extends Page> clz) {
        Button btn = template.button(name, 10, 7);
        btn.setOnClickListener(v -> this.show(clz));
        return btn;
    }
}
