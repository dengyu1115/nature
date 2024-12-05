package org.nature.common.page;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import org.nature.common.util.PopupUtil;
import org.nature.common.view.SearchBar;
import org.nature.common.view.Table;

import java.util.List;
import java.util.function.Consumer;

/**
 * 列表页面
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/9
 */
public abstract class ListPage<T> extends Page {

    private LinearLayout page;
    private Table<T> excel;
    private Button button;
    private TextView total;
    private final Handler handler = new Handler(msg -> {
        this.total.setText(String.valueOf(this.excel.getListSize()));
        return false;
    });
    private int height;
    private float density;

    @Override
    protected void makeStructure(LinearLayout page, Context context) {
        this.page = page;
        this.context = context;
        this.makeStructure();
    }

    @Override
    protected void onShow() {
        this.initBehaviours();
        this.refreshData();
    }

    /**
     * 布局页面
     */
    private void makeStructure() {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        page.setOrientation(LinearLayout.VERTICAL);
        height = metrics.heightPixels;
        density = metrics.density;
        this.header();
        this.body();
        this.footer();
    }

    /**
     * 初始化按钮行为
     */
    private void initBehaviours() {
        this.button.setOnClickListener(v -> this.refreshData());
        this.excel.setLongClick(this.longClick());
        this.excel.define(this.define());
        this.initHeaderBehaviours();
    }

    /**
     * 头部布局
     */
    private void header() {
        LinearLayout header = new LinearLayout(context);
        page.addView(header);
        header.setLayoutParams(new LayoutParams(MATCH_PARENT, (int) (40 * density)));
        SearchBar searchBar = new SearchBar(context);
        header.addView(searchBar);
        button = template.button("查询", 5, 7);
        searchBar.addHandleView(button);
        this.initHeaderViews(searchBar);
    }

    /**
     * 主体布局
     */
    private void body() {
        LinearLayout body = new LinearLayout(context);
        page.addView(body);
        body.setLayoutParams(new LayoutParams(MATCH_PARENT, height - (int) (60 * density)));
        this.excel = new Table<>(context, this.getTotalColumns(), this.getFixedColumns());
        body.addView(this.excel);
    }

    /**
     * 底部布局
     */
    private void footer() {
        LinearLayout footer = new LinearLayout(context);
        page.addView(footer);
        footer.setLayoutParams(new LayoutParams(MATCH_PARENT, (int) (20 * density)));
        footer.setGravity(Gravity.CENTER);
        total = new TextView(context);
        footer.addView(total);
        total.setGravity(Gravity.CENTER);
    }

    /**
     * 刷新数据
     */
    protected void refreshData() {
        new Thread(() -> {
            try {
                button.setClickable(false);
                this.excel.data(this.listData());
                this.refreshTotal();
            } catch (Exception e) {
                e.printStackTrace(System.err);
                Looper.prepare();
                String message = e.getMessage();
                message = StringUtils.isBlank(message) ? "未知错误" : message;
                PopupUtil.alert(context, message);
            } finally {
                button.setClickable(true);
            }
        }).start();
    }

    /**
     * 刷新汇总值
     */
    private void refreshTotal() {
        handler.sendMessage(new Message());
    }

    /**
     * 表格列数
     * @return int
     */
    protected int getTotalColumns() {
        return 9;
    }

    /**
     * 表格固定列数
     * @return int
     */
    protected int getFixedColumns() {
        return 1;
    }

    /**
     * 长按操作
     * @return Consumer
     */
    protected Consumer<T> longClick() {
        return null;
    }

    /**
     * 表格定义
     * @return 表格定义信息
     */
    protected abstract List<Table.Header<T>> define();

    /**
     * 查询数据
     * @return list
     */
    protected abstract List<T> listData();

    /**
     * 初始化头部视图
     * @param searchBar 搜索组件
     */
    protected abstract void initHeaderViews(SearchBar searchBar);

    /**
     * 初始化头部按钮行为
     */
    protected abstract void initHeaderBehaviours();

}
