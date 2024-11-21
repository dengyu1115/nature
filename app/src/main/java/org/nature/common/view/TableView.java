package org.nature.common.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.apache.commons.lang3.StringUtils;
import org.nature.common.util.ClickUtil;
import org.nature.common.util.Sorter;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static android.graphics.drawable.GradientDrawable.Orientation.RIGHT_LEFT;
import static org.nature.common.constant.Const.PAGE_WIDTH;

/**
 * 表格
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
@SuppressLint({"DefaultLocale", "ClickableViewAccessibility"})
public class TableView<T> extends BasicView {

    public static final int HEIGHT = 33, PADDING = 8, SCROLL_BAR_SIZE = 3;
    private final int columns, fixed;
    private final float widthRate;
    private final LayoutParams param = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
    /**
     * 水平滚动的view集合
     */
    private final Set<HorizontalScrollView> horizontalScrollViews = new HashSet<>();
    /**
     * 排序点击计数
     */
    private final AtomicInteger sc = new AtomicInteger(-1);
    /**
     * 移动取消标记
     */
    private final AtomicBoolean canceled = new AtomicBoolean();
    /**
     * 移动状态标记
     */
    private final AtomicBoolean running = new AtomicBoolean();
    /**
     * listview适配器
     */
    private final Adapter adapter = new Adapter();
    /**
     * 异步处理类
     */
    private final Handler handler = new Handler(this::handleMessage);
    /**
     * 表格定义
     */
    private List<D<T>> ds;
    /**
     * 表格需要展示的数据集合
     */
    private List<T> list = new ArrayList<>();
    /**
     * 临时集合
     */
    private List<T> tempList;
    private float colWidth;
    private HorizontalScrollView touchView;
    private int scrollX, oldScrollX;
    private final OnScrollChangeListener scrollChangeListener = (v, x, y, ox, oy) -> this.scrollAll(this.scrollX = x);
    private Comparator<T> comparator;
    private int sortCol;
    private boolean sortClicked;
    private float clickX, clickY;
    private int titleGroup, titleCol;

    private Consumer<T> longClick;

    public TableView(Context context) {
        this(context, 3, 1);
    }

    public TableView(Context context, int columns, int fixed) {
        this(context, columns, fixed, 1);
    }

    public TableView(Context context, int columns, int fixed, float widthRate) {
        super(context);
        this.fixed = fixed;
        this.widthRate = widthRate;
        this.context = context;
        this.columns = columns;
    }

    /**
     * 定义
     * @param ds 列定义集合
     */
    public void define(List<D<T>> ds) {
        this.ds = ds;
        this.init();
    }

    /**
     * 设置数据
     * @param list 数据
     */
    public void data(List<T> list) {
        this.tempList = list;
        handler.sendMessage(new Message());
    }

    /**
     * 设置长按事件
     * @param click 长按处理逻辑
     */
    public void setLongClick(Consumer<T> click) {
        this.longClick = click;
    }

    /**
     * 初始化
     */
    private void init() {
        // 计算列宽
        this.calculateColumnWidth();
        // 设置行间线
        this.setBaselineAligned(true);
        // 设置布局参数
        this.setLayoutParams(param);
        // 设置布局方向
        this.setOrientation(VERTICAL);
        // 设置表头
        this.addView(this.headerView());
        // 设置分割线
        this.addView(this.hDivider());
        // 设置数据行
        this.addView(this.dataView());
    }

    /**
     * 计算列宽度
     */
    private void calculateColumnWidth() {
        // context.getResources().getDisplayMetrics().widthPixels;
        this.colWidth = (PAGE_WIDTH - columns + 1) * widthRate / DENSITY / columns + 0.4f; //  - 2
    }

    /**
     * 表头view部分
     * @return LinearLayout
     */
    private LinearLayout headerView() {
        LinearLayout line = this.lineView();
        HorizontalScrollView scrollView = this.horizontalScrollView();
        scrollView.setOnScrollChangeListener(scrollChangeListener);
        LinearLayout innerLine = this.lineView();
        scrollView.addView(innerLine);
        horizontalScrollViews.add(scrollView);
        this.scrollFix(scrollView);
        for (D<T> d : ds) {
            List<D<T>> ds = d.ds;
            // 单层表头处理
            if (ds == null || ds.isEmpty()) {
                TextView content = this.titleView(d);
                if (titleGroup < fixed - 1) {
                    line.addView(content);
                    line.addView(this.vDivider());
                } else if (titleGroup == fixed - 1) {
                    line.addView(content);
                    line.addView(this.vDivider());
                    line.addView(scrollView);
                } else if (titleGroup == this.ds.size() - 1) {
                    innerLine.addView(content);
                } else {
                    innerLine.addView(content);
                    innerLine.addView(this.vDivider());
                }
            } else {
                // 多级表头处理
                if (StringUtils.isNotBlank(d.title)) {
                    LinearLayout rect = this.rectView(d.title, d.titleAlign, d.ds);
                    if (titleGroup < fixed - 1) {
                        line.addView(rect);
                        line.addView(this.vDivider());
                    } else if (titleGroup == fixed - 1) {
                        line.addView(rect);
                        line.addView(this.vDivider());
                        line.addView(scrollView);
                    } else if (titleGroup == this.ds.size() - 1) {
                        innerLine.addView(rect);
                    } else {
                        innerLine.addView(rect);
                        innerLine.addView(this.vDivider());
                    }
                } else if (titleGroup < fixed - 1) {
                    for (D<T> td : ds) {
                        TextView content = this.titleView(td);
                        line.addView(content);
                        line.addView(this.vDivider());
                    }
                } else if (titleGroup == fixed - 1) {
                    int j = 0;
                    for (D<T> td : ds) {
                        TextView content = this.titleView(td);
                        if (j == ds.size() - 1) {
                            line.addView(content);
                            line.addView(this.vDivider());
                            line.addView(scrollView);
                        } else {
                            line.addView(content);
                            line.addView(this.vDivider());
                        }
                        j++;
                    }
                } else if (titleGroup == this.ds.size() - 1) {
                    int j = 0;
                    for (D<T> td : ds) {
                        TextView content = this.titleView(td);
                        if (j == ds.size() - 1) {
                            innerLine.addView(content);
                            innerLine.addView(this.vDivider());
                        } else {
                            innerLine.addView(content);
                        }
                        j++;
                    }
                } else {
                    for (D<T> td : ds) {
                        TextView content = this.titleView(td);
                        innerLine.addView(content);
                        innerLine.addView(this.vDivider());
                    }
                }

            }
            titleGroup++;
        }
        return line;
    }

    /**
     * 内容view
     * @return LinearLayout
     */
    private LinearLayout contentView() {
        List<View> textViews = new ArrayList<>();
        LinearLayout line = this.lineView();
        line.setTag(textViews);
        HorizontalScrollView scrollView = this.horizontalScrollView();
        scrollView.setOnScrollChangeListener(scrollChangeListener);
        LinearLayout innerLine = this.lineView();
        scrollView.addView(innerLine);
        horizontalScrollViews.add(scrollView);
        this.scrollFix(scrollView);
        int size = ds.size();
        for (int i = 0; i < size; i++) {
            D<T> d = ds.get(i);
            List<D<T>> ds = d.ds;
            if (ds == null || ds.isEmpty()) {
                // 单层表头的处理
                TextView content = this.textView();
                textViews.add(content);
                if (i < this.fixed - 1) {
                    line.addView(content);
                    line.addView(this.vDivider());
                } else if (i == this.fixed - 1) {
                    line.addView(content);
                    line.addView(this.vDivider());
                    line.addView(scrollView);
                } else if (i == size - 1) {
                    innerLine.addView(content);
                } else {
                    innerLine.addView(content);
                    innerLine.addView(this.vDivider());
                }
            } else {
                // 多层表头的处理
                if (i < this.fixed - 1) {
                    for (int j = 0; j < ds.size(); j++) {
                        TextView content = this.textView();
                        textViews.add(content);
                        line.addView(content);
                        line.addView(this.vDivider());
                    }
                } else if (i == this.fixed - 1) {
                    for (int j = 0; j < ds.size(); j++) {
                        TextView content = this.textView();
                        textViews.add(content);
                        if (j == ds.size() - 1) {
                            line.addView(content);
                            line.addView(this.vDivider());
                            line.addView(scrollView);
                        } else {
                            line.addView(content);
                            line.addView(this.vDivider());
                        }
                    }
                } else if (i == size - 1) {
                    for (int j = 0; j < ds.size(); j++) {
                        TextView content = this.textView();
                        textViews.add(content);
                        if (j == ds.size() - 1) {
                            innerLine.addView(content);
                        } else {
                            innerLine.addView(content);
                            innerLine.addView(this.vDivider());
                        }
                    }
                } else {
                    for (int j = 0; j < ds.size(); j++) {
                        TextView content = this.textView();
                        textViews.add(content);
                        innerLine.addView(content);
                        innerLine.addView(this.vDivider());
                    }
                }
            }
        }
        return line;
    }

    /**
     * 标题view
     * @param td 定义信息
     * @return TextView
     */
    private TextView titleView(D<T> td) {
        TextView textView = this.textView();
        textView.setText(td.title);
        textView.setGravity(this.textAlign(td.titleAlign));
        this.addSortClickEvent(textView, td.sort);
        return textView;
    }


    /**
     * 添加点击排序事件
     * @param view       view
     * @param comparator 排序逻辑
     */
    private void addSortClickEvent(TextView view, Comparator<T> comparator) {
        if (comparator == null) {
            return;
        }
        int col = titleCol++;
        if (this.titleGroup < this.fixed) {
            view.setOnClickListener(v -> {
                this.comparator = comparator;
                this.sortCol = col;
                this.sortClick(v);
            });
        } else {
            view.setOnTouchListener((v, event) -> {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    this.comparator = comparator;
                    this.sortCol = col;
                    this.sortClicked = true;
                }
                return false;
            });
        }
    }

    /**
     * 排序点击
     * @param view view
     */
    private void sortClick(View view) {
        ClickUtil.click(view, () -> {
            if (sc.get() == this.sortCol) {
                sc.set(-1);
                this.list.sort(this.comparator.reversed());
            } else {
                sc.set(this.sortCol);
                this.list.sort(this.comparator);
            }
            adapter.notifyDataSetChanged();
        });
    }

    /**
     * 滚动到固定位置
     * @param scrollView 水平滚动view
     */
    private void scrollFix(HorizontalScrollView scrollView) {
        scrollView.setOnTouchListener((view, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                // 手指点下时候事件处理
                clickX = event.getX();
                clickY = event.getY();
                synchronized (TableView.this) {
                    canceled.set(true);
                    if (touchView != null) {
                        touchView.fling(0);
                        touchView.setOnScrollChangeListener(null);
                    }
                    this.scrollAll(scrollView.getScrollX());
                    (touchView = scrollView).setOnScrollChangeListener(scrollChangeListener);
                }
            } else if (action == MotionEvent.ACTION_UP && sortClicked
                    && event.getX() == clickX && event.getY() == clickY) {
                // 手指放开事件处理
                this.sortClick(view);
                this.sortClicked = false;
            } else if (action != MotionEvent.ACTION_MOVE) {
                // 其他非移动情况处理
                synchronized (TableView.this) {
                    canceled.set(false);
                    if (running.get()) return false;
                    Timer timer = new Timer();
                    running.set(true);
                    timer.schedule(this.moveFixTask(scrollView, timer), 500, 100);
                }
                this.sortClicked = false;
            } else {
                (touchView = scrollView).setOnScrollChangeListener(scrollChangeListener);
            }
            return false;
        });
    }

    /**
     * 移动至指定位置任务
     * @param scrollView 滚动view
     * @param timer      定时器
     * @return TimerTask
     */
    private TimerTask moveFixTask(HorizontalScrollView scrollView, Timer timer) {
        return new TimerTask() {
            @Override
            public void run() {
                synchronized (TableView.this) {
                    if (canceled.get()) {
                        timer.cancel();
                        running.set(false);
                        return;
                    }
                    int x = scrollView.getScrollX();
                    int scrollX = TableView.this.calculateFixScroll(x);
                    if (oldScrollX != x) {
                        oldScrollX = x;
                    } else {
                        scrollView.setOnScrollChangeListener(null);
                        TableView.this.scrollAll(scrollX);
                    }
                    if (scrollX == x) {
                        timer.cancel();
                        running.set(false);
                    }
                }
            }
        };
    }

    /**
     * 计算滚动固定位置
     * @param x 位置
     * @return int
     */
    private int calculateFixScroll(int x) {
        float dp = this.pxToDp(x);
        // 计算滚动至第几个
        int num = (int) (dp / (colWidth + 1));
        // 计算位置：
        return this.dpToPx((colWidth + 1) * (dp - colWidth * num < colWidth / 2 ? num : num + 1));
    }

    /**
     * 行
     * @return LinearLayout
     */
    private LinearLayout lineView() {
        LinearLayout line = new LinearLayout(context);
        line.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        return line;
    }

    /**
     * 列
     * @param text  文案
     * @param align 对其方式
     * @param ds    定义信息
     * @return LinearLayout
     */
    private LinearLayout rectView(String text, int align, List<D<T>> ds) {
        LinearLayout line = new LinearLayout(context);
        line.setLayoutParams(new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        line.setOrientation(VERTICAL);
        TextView textView = this.textView();
        textView.setWidth(ds.size() * this.dpToPx(colWidth));
        textView.setHeight(this.dpToPx(HEIGHT / 2f) - 1);
        textView.setText(text);
        textView.setGravity(this.textAlign(align));
        line.addView(textView);
        line.addView(this.hDivider());
        LinearLayout bottom = new LinearLayout(context);
        bottom.setLayoutParams(new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        line.addView(bottom);
        int i = 0;
        for (D<T> d : ds) {
            TextView content = this.titleView(d);
            if (i != 0) {
                bottom.addView(this.vDivider());
            }
            content.setHeight(this.dpToPx(HEIGHT / 2f) - 1);
            bottom.addView(content);
            i++;
        }
        return line;
    }

    /**
     * 水平滚动view
     * @return HorizontalScrollView
     */
    private HorizontalScrollView horizontalScrollView() {
        HorizontalScrollView hsv = new HorizontalScrollView(context);
        hsv.setLayoutParams(param);
        hsv.setScrollBarSize(0);
        hsv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        return hsv;
    }

    /**
     * 数据view
     * @return ListView
     */
    private ListView dataView() {
        ListView listView = new ListView(context);
        listView.setLayoutParams(param);
        listView.setScrollBarSize(SCROLL_BAR_SIZE);
        listView.setAdapter(adapter);
        listView.setDivider(new GradientDrawable(RIGHT_LEFT, new int[]{BG_COLOR, BG_COLOR, BG_COLOR}));
        listView.setDividerHeight(1);
        listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        // 数据刷新完成滚动所有view到同一位置
        listView.getViewTreeObserver().addOnGlobalLayoutListener(() -> this.scrollAll(this.scrollX));
        return listView;
    }

    /**
     * 获取数据size
     * @return int
     */
    public int getListSize() {
        return list.size();
    }

    /**
     * 文本排布
     * @param textAlign 排布方式
     * @return int
     */
    private int textAlign(int textAlign) {
        if (textAlign == 1) {
            return Gravity.START | Gravity.CENTER;
        }
        if (textAlign == 2) {
            return Gravity.END | Gravity.CENTER;
        }
        return Gravity.CENTER;
    }

    /**
     * 文本view
     * @return TextView
     */
    private TextView textView() {
        TextView view = new TextView(context);
        view.setWidth(this.dpToPx(colWidth));
        view.setHeight(this.dpToPx(HEIGHT));
        view.setPadding(this.dpToPx(PADDING), 0, this.dpToPx(PADDING), 0);
        return view;
    }

    /**
     * 水平分隔线
     * @return View
     */
    private View hDivider() {
        return this.divider(MATCH_PARENT, 1);
    }

    /**
     * 纵向分隔线
     * @return View
     */
    private View vDivider() {
        return this.divider(1, MATCH_PARENT);
    }

    /**
     * 分隔线
     * @param w 宽
     * @param h 高
     * @return View
     */
    private View divider(int w, int h) {
        View view = new View(context);
        view.setLayoutParams(new LayoutParams(w, h));
        view.setBackgroundColor(BG_COLOR);
        return view;
    }

    /**
     * 控制全部滚动至同一位置
     * @param x 位置坐标
     */
    private void scrollAll(int x) {
        horizontalScrollViews.forEach(i -> i.scrollTo(x, 0));
    }

    /**
     * 处理数据更新
     * @param msg 消息
     * @return boolean
     */
    private boolean handleMessage(Message msg) {
        this.list = this.tempList;
        this.adapter.notifyDataSetChanged();
        return false;
    }

    public static <T> D<T> row(String title, Function<T, String> content) {
        return new D<>(title, content, 0, 0, null, null, null);
    }

    public static <T> D<T> row(String title, Function<T, String> content, int titleAlign) {
        return new D<>(title, content, titleAlign, 0, null, null, null);
    }

    public static <T> D<T> row(String title, int titleAlign, List<D<T>> ds) {
        return new D<>(title, null, titleAlign, 0, null, null, ds);
    }

    public static <T> D<T> row(String title, Function<T, String> content, int titleAlign, int contentAlign) {
        return new D<>(title, content, titleAlign, contentAlign, null, null, null);
    }

    public static <T, U extends Comparable<? super U>> D<T> row(String title, Function<T, String> content, int titleAlign,
                                                                int contentAlign, Function<T, U> sort) {
        return new D<>(title, content, titleAlign, contentAlign, Sorter.nullsLast(sort), null, null);
    }

    public static <T> D<T> row(String title, Function<T, String> content, int titleAlign, int contentAlign,
                               Comparator<T> sort) {
        return new D<>(title, content, titleAlign, contentAlign, sort, null, null);
    }

    public static <T> D<T> row(String title, Function<T, String> content, int titleAlign, int contentAlign,
                               Consumer<T> click) {
        return new D<>(title, content, titleAlign, contentAlign, null, click, null);
    }

    public static <T> D<T> row(String title, Function<T, String> content, int titleAlign, int contentAlign,
                               Comparator<T> sort, Consumer<T> click) {
        return new D<>(title, content, titleAlign, contentAlign, sort, click, null);
    }

    public static <T> D<T> row(String title, Function<T, String> content, int titleAlign, int contentAlign,
                               Comparator<T> sort, Consumer<T> click, List<D<T>> ds) {
        return new D<>(title, content, titleAlign, contentAlign, sort, click, ds);
    }

    public static class D<T> {

        private final String title;
        private final Function<T, String> content;
        private final int titleAlign;
        private final int contentAlign;
        private final Comparator<T> sort;
        private final Consumer<T> click;
        private final List<D<T>> ds;

        public D(String title, Function<T, String> content, int titleAlign, int contentAlign, Comparator<T> sort
                , Consumer<T> click, List<D<T>> ds) {
            this.title = title;
            this.content = content;
            this.titleAlign = titleAlign;
            this.contentAlign = contentAlign;
            this.sort = sort;
            this.click = click;
            this.ds = ds;
        }
    }

    class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public T getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressWarnings("unchecked")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = TableView.this.contentView();
            }
            List<TextView> textViews = (List<TextView>) convertView.getTag();
            T item = this.getItem(position);
            int num = 0;
            for (D<T> d : ds) {
                List<D<T>> ds = d.ds;
                if (ds == null || ds.isEmpty()) {
                    this.addAction(textViews, item, num, d);
                    num++;
                } else {
                    for (D<T> di : ds) {
                        this.addAction(textViews, item, num, di);
                        num++;
                    }
                }
            }
            if (longClick != null) {
                convertView.setOnLongClickListener(v -> {
                    longClick.accept(item);
                    return false;
                });
            }
            return convertView;
        }

        /**
         * 添加点击处理事件
         * @param views view集合
         * @param item  数据
         * @param num   第几个
         * @param d     定义信息
         */
        private void addAction(List<TextView> views, T item, int num, D<T> d) {
            TextView textView = views.get(num);
            textView.setText(d.content.apply(item));
            textView.setGravity(textAlign(d.contentAlign));
            if (d.click == null) {
                return;
            }
            // 设置点击事件
            ClickUtil.onClick(textView, () -> d.click.accept(item));
        }

    }

}
