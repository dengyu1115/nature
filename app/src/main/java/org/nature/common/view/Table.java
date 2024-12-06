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
import org.nature.common.util.ClickUtil;
import org.nature.common.util.Sorter;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static android.graphics.drawable.GradientDrawable.Orientation.RIGHT_LEFT;

/**
 * 表格
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
@SuppressLint({"DefaultLocale", "ClickableViewAccessibility", "ViewConstructor"})
public class Table<T> extends BasicView {

    public static final int PADDING = 10, SCROLL_BAR_SIZE = 3;
    private final int rowHeight, colWidth;
    /**
     * 水平滚动的view集合
     */
    private final Set<HorizontalScrollView> horizontalScrollViews = new HashSet<>();
    private final OnScrollChangeListener scrollChangeListener = (v, x, y, ox, oy) -> this.scrollAll(this.scrollX = x);
    /**
     * 排序点击计数
     */
    private final AtomicReference<Header<T>> sc = new AtomicReference<>();
    /**
     * listview适配器
     */
    private final Adapter adapter = new Adapter();
    /**
     * 异步处理类
     */
    private final Handler handler = new Handler(this::handleMessage);
    /**
     * 表格需要展示的数据集合
     */
    private List<T> data = new ArrayList<>();

    private HorizontalScrollView touchView;
    private int scrollX, oldScrollX;
    private Comparator<T> comparator;

    private Consumer<T> longClick;
    private Header<T> sortCol;
    /**
     * 表格定义
     */
    private List<Header<T>> headers;
    /**
     * 平铺后最底层的表格定义数据
     */
    private List<Header<T>> flatHeaders;
    /**
     * 是否需要水平滚动
     */
    private boolean needHScroll;
    /**
     * 固定表头量
     */
    private int fixedMainHeaders, fixedSubHeaders;

    private Timer timer;

    public Table(Context context, int width, int height, int rows, int columns) {
        super(context);
        this.context = context;
        this.rowHeight = (int) (height / (float) (rows + 1) + 0.5f);
        this.colWidth = (int) (width / (float) columns + 0.5f);
        // 设置行间线
        this.setBaselineAligned(true);
        // 设置布局参数
        this.setLayoutParams(new LayoutParams(colWidth * columns - 1, rowHeight * (rows + 1)));
        // 设置布局方向
        this.setOrientation(VERTICAL);
    }

    /**
     * 设置表头，规定固定表头量
     * @param headers 列定义集合
     */
    public void setHeaders(List<Header<T>> headers, int fixedHeaders) {
        this.headers = headers;
        this.needHScroll = headers.size() > fixedHeaders;
        this.flatHeaders = this.flatHeaders(headers);
        this.fixedMainHeaders = fixedHeaders;
        this.fixedSubHeaders = this.calcFixedSubHeaders(headers, fixedHeaders);
        this.init();
    }

    /**
     * 设置数据
     * @param data 数据
     */
    public void data(List<T> data) {
        this.data = data;
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
        // 设置表头
        this.addView(this.buildHeadersView());
        // 设置分割线
        this.addView(this.divider(MATCH_PARENT, 1));
        // 设置数据行
        this.addView(this.buildDataView());
    }

    /**
     * 表头view部分
     * @return LinearLayout
     */
    private LinearLayout buildHeadersView() {
        LinearLayout line = this.buildRowView();
        boolean needScroll = headers.size() > fixedMainHeaders;
        int size = needScroll ? fixedMainHeaders : headers.size();
        // 遍历不可滚动的部分处理
        for (int i = 0; i < size; i++) {
            this.doAddHeaderView(line, headers.get(i), i, size);
        }
        // 没有滚动部分，构建完成返回
        if (!needScroll) {
            return line;
        }
        // 有滚动部分添加滚动view
        line.addView(this.divider(1, MATCH_PARENT));
        HorizontalScrollView scrollView = this.horizontalScrollView();
        scrollView.setOnScrollChangeListener(scrollChangeListener);
        LinearLayout innerLine = this.buildRowView();
        scrollView.addView(innerLine);
        horizontalScrollViews.add(scrollView);
        this.scrollFix(scrollView);
        line.addView(scrollView);
        size = headers.size();
        // 遍历可滚动的部分处理
        for (int i = fixedMainHeaders; i < size; i++) {
            this.doAddHeaderView(innerLine, headers.get(i), i, size);
        }
        return line;
    }

    /**
     * 数据view
     * @return ListView
     */
    private ListView buildDataView() {
        ListView listView = new ListView(context);
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
     * 内容view
     * @return LinearLayout
     */
    private LinearLayout buildDatumView() {
        LinearLayout line = this.buildRowView();
        List<View> textViews = new ArrayList<>();
        line.setTag(textViews);
        boolean needScroll = flatHeaders.size() > fixedSubHeaders;
        int size = needScroll ? fixedSubHeaders : flatHeaders.size();
        // 遍历不可滚动的部分处理
        for (int i = 0; i < size; i++) {
            this.doAddDatumView(line, textViews, i, size);
        }
        // 没有滚动部分，构建完成返回
        if (!needScroll) {
            return line;
        }
        // 有滚动部分添加滚动view
        line.addView(this.divider(1, MATCH_PARENT));
        HorizontalScrollView scrollView = this.horizontalScrollView();
        scrollView.setOnScrollChangeListener(scrollChangeListener);
        LinearLayout innerLine = this.buildRowView();
        scrollView.addView(innerLine);
        horizontalScrollViews.add(scrollView);
        this.scrollFix(scrollView);
        line.addView(scrollView);
        size = flatHeaders.size();
        // 遍历可滚动的部分处理
        for (int i = fixedSubHeaders; i < size; i++) {
            this.doAddDatumView(innerLine, textViews, i, size);
        }
        return line;
    }

    /**
     * 添加表头
     * @param line   容器view
     * @param header 表头
     * @param i      位置
     * @param size   总量
     */
    private void doAddHeaderView(LinearLayout line, Header<T> header, int i, int size) {
        LinearLayout headerView = this.buildHeaderView(header, this.rowHeight);
        line.addView(headerView);
        // 不是最后一个需要添加分隔线
        if (i < size - 1) {
            line.addView(this.divider(1, MATCH_PARENT));
        }
    }

    /**
     * 添加表头
     * @param line  容器view
     * @param views view集合
     * @param i     位置
     * @param size  总量
     */
    private void doAddDatumView(LinearLayout line, List<View> views, int i, int size) {
        TextView textView = this.buildDatumColView();
        line.addView(textView);
        views.add(textView);
        // 不是最后一个需要添加分隔线
        if (i < size - 1) {
            line.addView(this.divider(1, MATCH_PARENT));
        }
    }

    /**
     * 添加点击排序事件
     * @param view view
     */
    private void addSortClickEvent(TextView view, Header<T> header) {
        if (header.sort == null) {
            return;
        }
        view.setOnClickListener(v -> {
            this.comparator = header.sort;
            this.sortCol = header;
            this.sortClick(v);
        });
    }

    /**
     * 排序点击
     * @param view view
     */
    private void sortClick(View view) {
        ClickUtil.click(view, () -> {
            if (sc.get() != null && sc.get() == this.sortCol) {
                sc.set(null);
                this.data.sort(this.comparator.reversed());
            } else {
                sc.set(this.sortCol);
                this.data.sort(this.comparator);
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
                (touchView = scrollView).setOnScrollChangeListener(scrollChangeListener);
                touchView.fling(0);
            } else if (action == MotionEvent.ACTION_MOVE) {
                // 手指放开事件处理
                if (timer == null) {
                    timer = new Timer();
                    timer.schedule(this.moveFixTask(scrollView), 500, 100);
                }
            }
            return false;
        });
    }

    /**
     * 移动至指定位置任务
     * @param scrollView 滚动view
     * @return TimerTask
     */
    private TimerTask moveFixTask(HorizontalScrollView scrollView) {
        return new TimerTask() {
            @Override
            public void run() {
                synchronized (Table.this) {
                    int x = scrollView.getScrollX();
                    // 根据滚动位置计算需要停止的位置
                    int stopPos = Table.this.calcScrollStopPos(x);
                    if (oldScrollX != x) {
                        // 未停止，记录上一个位置
                        oldScrollX = x;
                    } else {
                        // 已停止滚动，取消滚动变更监听，滚动所有行到固定位置
                        scrollView.setOnScrollChangeListener(null);
                        Table.this.scrollAll(stopPos);
                        // 滚动位置固定后关闭定时器
                        timer.cancel();
                        timer = null;
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
    private int calcScrollStopPos(int x) {
        // 计算滚动至第几个
        int num = x / colWidth;
        // 计算位置：
        return colWidth * ((x - colWidth * num < colWidth / 2) ? num : num + 1);
    }

    /**
     * 行
     * @return LinearLayout
     */
    private LinearLayout buildRowView() {
        LinearLayout line = new LinearLayout(context);
        line.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        return line;
    }

    /**
     * 水平滚动view
     * @return HorizontalScrollView
     */
    private HorizontalScrollView horizontalScrollView() {
        HorizontalScrollView hsv = new HorizontalScrollView(context);
        hsv.setScrollBarSize(0);
        hsv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        return hsv;
    }

    /**
     * 获取数据size
     * @return int
     */
    public int getListSize() {
        return data.size();
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
        this.adapter.notifyDataSetChanged();
        return false;
    }

    /**
     * 展开表头，收集最底层表头
     * @param headers 表头数据集合
     * @return list
     */
    private List<Header<T>> flatHeaders(List<Header<T>> headers) {
        List<Header<T>> list = new ArrayList<>();
        for (Header<T> i : headers) {
            List<Header<T>> hs = i.headers;
            if (hs == null || hs.isEmpty()) {
                // 没有下级说明是最底层的表头
                list.add(i);
            } else {
                // 有下级继续处理下级
                list.addAll(this.flatHeaders(hs));
            }
        }
        return list;
    }

    /**
     * 计算固定表头量
     * @param headers      表头数据集合
     * @param fixedHeaders 固定列数
     * @return int
     */
    private int calcFixedSubHeaders(List<Header<T>> headers, int fixedHeaders) {
        // 所有列固定
        if (!this.needHScroll) {
            return this.flatHeaders.size();
        }
        // 计算需要固定的底层表头量
        return this.flatHeaders(headers.subList(0, fixedHeaders)).size();
    }

    /**
     * 构建表头view
     * @param header    表头
     * @param rowHeight 行高
     * @return LinearLayout
     */
    private LinearLayout buildHeaderView(Header<T> header, int rowHeight) {
        // 计算表头需要占用的列宽
        int subHeaders = this.calcSubHeaders(header);
        // 计算表头共有几层
        int headerLevel = this.calcHeaderLevel(header);
        LinearLayout line = new LinearLayout(context);
        int width = subHeaders * colWidth - 1;
        int height = (int) (rowHeight / (float) headerLevel + 0.5) - (headerLevel > 1 ? 1 : 0);
        line.setLayoutParams(new LayoutParams(width, rowHeight));
        line.setOrientation(VERTICAL);
        TextView textView = new TextView(context);
        textView.setPadding(PADDING, 0, PADDING, 0);
        textView.setWidth(width);
        textView.setHeight(height);
        textView.setText(header.title);
        textView.setGravity(this.textAlign(header.titleAlign));
        this.addSortClickEvent(textView, header);
        line.addView(textView);
        // 单层表头返回
        if (headerLevel == 1) {
            return line;
        }
        // 增加分隔线
        line.addView(this.divider(MATCH_PARENT, 1));
        LinearLayout bottom = new LinearLayout(context);
        // 剩余行高
        rowHeight = rowHeight - height;
        bottom.setLayoutParams(new LayoutParams(MATCH_PARENT, rowHeight));
        line.addView(bottom);
        // 多级表头继续遍历下级处理
        int size = header.headers.size();
        for (int i = 0; i < size; i++) {
            Header<T> h = header.headers.get(i);
            bottom.addView(this.buildHeaderView(h, rowHeight));
            // 不是最后一个添加分隔线
            if (i < size - 1) {
                bottom.addView(this.divider(1, MATCH_PARENT));
            }
        }
        return line;
    }


    private TextView buildDatumColView() {
        TextView view = new TextView(context);
        view.setWidth(colWidth - 1);
        view.setHeight(rowHeight);
        view.setPadding(PADDING, 0, PADDING, 0);
        return view;
    }

    /**
     * 计算子级表头数量
     * @param header 表头
     * @return int
     */
    private int calcSubHeaders(Header<T> header) {
        List<Header<T>> list = header.headers;
        // 没有下级返回1
        if (list == null || list.isEmpty()) {
            return 1;
        }
        // 有下级则累加下级
        return list.stream().mapToInt(this::calcSubHeaders).sum();
    }

    /**
     * 计算子级表头数量
     * @param header 表头
     * @return int
     */
    private int calcHeaderLevel(Header<T> header) {
        List<Header<T>> list = header.headers;
        // 没有下级返回1
        if (list == null || list.isEmpty()) {
            return 1;
        }
        // 有下级则累加下级
        return 1 + list.stream().mapToInt(this::calcHeaderLevel).max().orElse(0);
    }

    public static <T> Header<T> header(String title, Function<T, String> content) {
        return new Header<>(title, content, 0, 0, null, null, null);
    }

    public static <T> Header<T> header(String title, Function<T, String> content, int titleAlign) {
        return new Header<>(title, content, titleAlign, 0, null, null, null);
    }

    public static <T> Header<T> header(String title, int titleAlign, List<Header<T>> headers) {
        return new Header<>(title, null, titleAlign, 0, null, null, headers);
    }

    public static <T> Header<T> header(String title, Function<T, String> content, int titleAlign, int contentAlign) {
        return new Header<>(title, content, titleAlign, contentAlign, null, null, null);
    }

    public static <T, U extends Comparable<? super U>> Header<T> header(String title, Function<T, String> content, int titleAlign,
                                                                        int contentAlign, Function<T, U> sort) {
        return new Header<>(title, content, titleAlign, contentAlign, Sorter.nullsLast(sort), null, null);
    }

    public static <T> Header<T> header(String title, Function<T, String> content, int titleAlign, int contentAlign,
                                       Comparator<T> sort) {
        return new Header<>(title, content, titleAlign, contentAlign, sort, null, null);
    }

    public static <T> Header<T> header(String title, Function<T, String> content, int titleAlign, int contentAlign,
                                       Consumer<T> click) {
        return new Header<>(title, content, titleAlign, contentAlign, null, click, null);
    }

    public static <T> Header<T> header(String title, Function<T, String> content, int titleAlign, int contentAlign,
                                       Comparator<T> sort, Consumer<T> click) {
        return new Header<>(title, content, titleAlign, contentAlign, sort, click, null);
    }

    public static <T> Header<T> header(String title, Function<T, String> content, int titleAlign, int contentAlign,
                                       Comparator<T> sort, Consumer<T> click, List<Header<T>> headers) {
        return new Header<>(title, content, titleAlign, contentAlign, sort, click, headers);
    }

    public static class Header<T> {

        private final String title;
        private final Function<T, String> content;
        private final int titleAlign;
        private final int contentAlign;
        private final Comparator<T> sort;
        private final Consumer<T> click;
        private final List<Header<T>> headers;

        public Header(String title, Function<T, String> content, int titleAlign, int contentAlign, Comparator<T> sort
                , Consumer<T> click, List<Header<T>> headers) {
            this.title = title;
            this.content = content;
            this.titleAlign = titleAlign;
            this.contentAlign = contentAlign;
            this.sort = sort;
            this.click = click;
            this.headers = headers;
        }
    }

    class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public T getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressWarnings("unchecked")
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = Table.this.buildDatumView();
            }
            List<TextView> textViews = (List<TextView>) view.getTag();
            T item = this.getItem(position);
            for (int i = 0; i < flatHeaders.size(); i++) {
                this.addAction(textViews, item, i, flatHeaders.get(i));
            }
            if (longClick != null) {
                view.setOnLongClickListener(v -> {
                    longClick.accept(item);
                    return false;
                });
            }
            return view;
        }

        /**
         * 添加点击处理事件
         * @param views  view集合
         * @param item   数据
         * @param num    第几个
         * @param header 定义信息
         */
        private void addAction(List<TextView> views, T item, int num, Header<T> header) {
            TextView textView = views.get(num);
            textView.setText(header.content.apply(item));
            textView.setGravity(textAlign(header.contentAlign));
            if (header.click == null) {
                return;
            }
            // 设置点击事件
            ClickUtil.onClick(textView, () -> header.click.accept(item));
        }

    }

}
