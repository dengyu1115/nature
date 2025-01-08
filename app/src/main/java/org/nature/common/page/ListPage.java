package org.nature.common.page;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import org.nature.common.util.ClickUtil;
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

    private Table<T> table;
    private Button query;
    private TextView total;
    private final Handler handler = new Handler(Looper.myLooper(), msg -> {
        int handle = msg.getData().getInt("handle");
        if (handle == 2) {
            this.query.setClickable(false);
        } else if (handle == 1) {
            this.query.setClickable(true);
        } else {
            this.table.data(this.listData());
            this.total.setText(String.valueOf(this.table.getDataSize()));
        }
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
        ClickUtil.onClick(this.query, this::refreshData);
        this.table.setLongClick(this.longClick());
        this.initHeaderBehaviours();
    }

    /**
     * 头部布局
     */
    private void header() {
        LinearLayout condition = template.line(90, 7);
        condition.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        LinearLayout handle = template.line(10, 7);
        handle.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        LinearLayout header = template.line(100, 7, condition, handle);
        page.addView(header);
        handle.addView(query = template.button("查询", 5, 7));
        this.initHeaderViews(condition);
    }

    /**
     * 主体布局
     */
    private void body() {
        this.table = template.table(100, 87, this.getTotalRows(), this.getTotalColumns());
        this.table.setHeaders(this.headers(), this.getFixedColumns());
        page.addView(this.table);
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
            Looper.prepare();
            try {
                this.setQueryClickable(false);
                this.refreshTotal();
            } catch (Exception e) {
                template.alert(StringUtils.isBlank(e.getMessage()) ? "未知错误" : e.getMessage());
            } finally {
                this.setQueryClickable(true);
            }
        }).start();
    }

    /**
     * 刷新汇总值
     */
    private void refreshTotal() {
        Message msg = new Message();
        msg.getData().putInt("handle", 3);
        handler.sendMessage(msg);
    }

    private void setQueryClickable(boolean clickable) {
        Message msg = new Message();
        msg.getData().putInt("handle", clickable ? 1 : 2);
        handler.sendMessage(msg);
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
