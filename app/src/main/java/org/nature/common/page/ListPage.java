package org.nature.common.page;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import org.nature.common.view.Button;
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

    private Table<T> excel;
    private Button query;
    private TextView total;
    private final Handler handler = new Handler(Looper.myLooper(), msg -> {
        this.total.setText(String.valueOf(this.excel.getDataSize()));
        return false;
    });

    @Override
    protected void makeStructure() {
        page.setOrientation(LinearLayout.VERTICAL);
        this.header();
        this.body();
        this.footer();
    }

    @Override
    protected void onShow() {
        this.initBehaviours();
        this.refreshData();
    }

    /**
     * 初始化按钮行为
     */
    private void initBehaviours() {
        this.query.onClick(this::refreshData);
        this.excel.setLongClick(this.longClick());
        this.initHeaderBehaviours();
    }

    /**
     * 头部布局
     */
    private void header() {
        LinearLayout condition = template.line(90, 7);
        condition.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        condition.setPadding(3, 0, 3, 0);
        LinearLayout handle = template.line(10, 7);
        handle.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        handle.setPadding(3, 0, 7, 0);
        LinearLayout header = template.line(100, 7, condition, handle);
        page.addView(header);
        handle.addView(query = template.button("查询", 5, 7));
        this.initHeaderViews(condition);
    }

    /**
     * 主体布局
     */
    private void body() {
        this.excel = template.table(100, 87, this.getTotalRows(), this.getTotalColumns());
        this.excel.setHeaders(this.headers(), this.getFixedColumns());
        page.addView(this.excel);
    }

    /**
     * 底部布局
     */
    private void footer() {
        LinearLayout footer = template.line(100, 6);
        page.addView(footer);
        footer.setGravity(Gravity.CENTER);
        total = template.text("0", 10, 6);
        footer.addView(total);
    }

    /**
     * 刷新数据
     */
    protected void refreshData() {
        new Thread(() -> {
            try {
                query.setBtnClickable(false);
                this.excel.data(this.listData());
                this.refreshTotal();
            } catch (Exception e) {
                Looper.prepare();
                String message = e.getMessage();
                message = StringUtils.isBlank(message) ? "未知错误" : message;
                template.alert(message);
            } finally {
                query.setBtnClickable(true);
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
     * 表格行数
     * @return int
     */
    protected int getTotalRows() {
        return 10;
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
    protected abstract List<Table.Header<T>> headers();

    /**
     * 查询数据
     * @return list
     */
    protected abstract List<T> listData();

    /**
     * 初始化头部视图
     * @param condition 搜索组件
     */
    protected abstract void initHeaderViews(LinearLayout condition);

    /**
     * 初始化头部按钮行为
     */
    protected abstract void initHeaderBehaviours();

}
