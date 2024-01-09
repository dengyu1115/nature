package org.nature.common.page;

import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.LinearLayout;
import org.nature.common.ioc.annotation.Component;
import org.nature.common.ioc.holder.PageHolder;
import org.nature.common.model.PageInfo;
import org.nature.common.view.ViewTemplate;

import java.util.List;

/**
 * 主页面
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/9
 */
@Component
public class MainPage extends Page {

    private Context context;
    private LinearLayout page, body;
    private ViewTemplate template;
    private int height;
    private float density;

    @Override
    protected void makeStructure(LinearLayout page, Context context) {
        this.context = context;
        this.page = page;
        this.makeStructure();
    }

    @Override
    protected void onShow() {
        this.showMain(PageHolder.get("基础"));
    }

    /**
     * 布局
     */
    private void makeStructure() {
        template = ViewTemplate.build(context);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        page.setOrientation(LinearLayout.VERTICAL);
        height = metrics.heightPixels;
        density = metrics.density;
        this.header();
        this.body();
    }

    /**
     * 头部布局
     */
    private void header() {
        LinearLayout header = new LinearLayout(context);
        page.addView(header);
        header.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, (int) (40 * density)));
        header.addView(this.tabBtn("基础"));
        header.addView(this.tabBtn("ETF"));
    }

    /**
     * 主体布局
     */
    private void body() {
        this.body = new LinearLayout(context);
        page.addView(body);
        body.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, height - (int) (40 * density)));
    }

    /**
     * 展示主体
     * @param tag 页面菜单内容
     */
    private void showMain(List<List<PageInfo>> tag) {
        this.body.removeAllViews();
        if (tag == null) {
            return;
        }
        for (List<PageInfo> list : tag) {
            this.listMenu(list);
        }
    }

    /**
     * 菜单列表
     * @param pages 页面信息集合
     */
    private void listMenu(List<PageInfo> pages) {
        LinearLayout line = template.line(200, MATCH_PARENT);
        line.setOrientation(LinearLayout.VERTICAL);
        this.body.addView(line);
        for (PageInfo page : pages) {
            line.addView(this.menuBtn(page.getName(), page.getCls()));
            line.addView(template.textView("", 200, 5));
        }
    }

    /**
     * TAB按钮
     * @param name 名称
     * @return Button
     */
    private Button tabBtn(String name) {
        Button btn = template.button(name, 80, 30);
        btn.setOnClickListener(v -> this.showMain(PageHolder.get(name)));
        return btn;
    }

    /**
     * 菜单按钮
     * @param name 名称
     * @param clz  页面类
     * @return Button
     */
    private Button menuBtn(String name, Class<? extends Page> clz) {
        Button btn = template.button(name, 100, 35);
        btn.setOnClickListener(v -> this.show(clz));
        return btn;
    }
}
