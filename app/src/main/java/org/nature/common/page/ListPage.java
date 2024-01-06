package org.nature.common.page;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import org.nature.common.util.PopUtil;
import org.nature.common.view.ExcelView;
import org.nature.common.view.SearchBar;
import org.nature.common.view.ViewTemplate;

import java.util.List;

public abstract class ListPage<T> extends Page {

    protected Context context;
    protected ViewTemplate template;
    private LinearLayout page;
    private ExcelView<T> excel;
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

    private void makeStructure() {
        template = ViewTemplate.build(context);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        page.setOrientation(LinearLayout.VERTICAL);
        height = metrics.heightPixels;
        density = metrics.density;
        this.header();
        this.body();
        this.footer();
    }

    private void initBehaviours() {
        this.button.setOnClickListener(v -> this.refreshData());
        this.excel.define(this.define());
        this.initHeaderBehaviours();
    }

    private void header() {
        LinearLayout header = new LinearLayout(context);
        page.addView(header);
        header.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, (int) (40 * density)));
        SearchBar searchBar = new SearchBar(context);
        header.addView(searchBar);
        button = template.button("查询", 50, 30);
        searchBar.addHandleView(button);
        this.initHeaderViews(searchBar);
    }

    private void body() {
        LinearLayout body = new LinearLayout(context);
        page.addView(body);
        body.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, height - (int) (60 * density)));
        this.excel = new ExcelView<>(context, this.getExcelColumns());
        body.addView(this.excel);
    }

    private void footer() {
        LinearLayout footer = new LinearLayout(context);
        page.addView(footer);
        footer.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, (int) (20 * density)));
        footer.setGravity(Gravity.CENTER);
        total = new TextView(context);
        footer.addView(total);
        total.setGravity(Gravity.CENTER);
    }

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
                PopUtil.alert(context, message);
            } finally {
                button.setClickable(true);
            }
        }).start();
    }

    private void refreshTotal() {
        handler.sendMessage(new Message());
    }

    protected int getExcelColumns() {
        return 9;
    }

    protected abstract List<ExcelView.D<T>> define();

    protected abstract List<T> listData();

    protected abstract void initHeaderViews(SearchBar searchBar);

    protected abstract void initHeaderBehaviours();

}
