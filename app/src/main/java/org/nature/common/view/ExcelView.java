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
import org.nature.common.util.PopUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static android.graphics.drawable.GradientDrawable.Orientation.RIGHT_LEFT;

/**
 * 表格
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/5
 */
@SuppressLint("DefaultLocale")
public class ExcelView<T> extends BasicView {

    public static final int HEIGHT = 33;
    public static final int PADDING = 8;
    public static final int SCROLL_BAR_SIZE = 3;
    private final int columns;
    private final float widthRate;
    private final LayoutParams param = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
    private final List<HorizontalScrollView> horizontalScrollViews = new ArrayList<>();
    private final AtomicInteger sc = new AtomicInteger(-1);
    private final AtomicBoolean canceled = new AtomicBoolean();
    private final AtomicBoolean running = new AtomicBoolean();
    private final Adapter adapter = new Adapter();
    private final Handler handler = new Handler(this::handleMessage);
    private List<D<T>> ds;
    private List<T> list = new ArrayList<>();
    private List<T> tempList;
    private float colWidth;
    private HorizontalScrollView touchView;
    private int scrollX;
    private final OnScrollChangeListener scrollChangeListener = (v, x, y, ox, oy) -> this.scrollAll(this.scrollX = x);
    private int oldScrollX;
    private int childCount;
    private Comparator<T> comparator;
    private int sortCol;
    private boolean sortClicked;
    private float clickX, clickY;
    private int titleGroup, titleCol;

    public ExcelView(Context context) {
        this(context, 3);
    }

    public ExcelView(Context context, int columns) {
        this(context, columns, 1);
    }

    public ExcelView(Context context, int columns, float widthRate) {
        super(context);
        this.widthRate = widthRate;
        this.context = context;
        this.columns = columns;
    }

    public void define(List<D<T>> ds) {
        this.ds = ds;
        this.init();
    }

    public void data(List<T> list) {
        this.tempList = list;
        handler.sendMessage(new Message());
    }

    private void init() {
        this.calculateColumnWidth();
        this.setBaselineAligned(true);
        this.setLayoutParams(param);
        this.setOrientation(VERTICAL);
        this.addView(this.titleView());
        this.addView(this.hDivider());
        this.addView(this.listView());
    }

    private void calculateColumnWidth() {
//        context.getResources().getDisplayMetrics().widthPixels;
        this.colWidth = (2228 - columns) * widthRate / DENSITY / columns + 0.5f; //  - 2
    }

    private LinearLayout titleView() {
        LinearLayout line = this.line();
        HorizontalScrollView scrollView = this.horizontalScrollView();
        scrollView.setOnScrollChangeListener(scrollChangeListener);
        LinearLayout innerLine = this.line();
        scrollView.addView(innerLine);
        horizontalScrollViews.add(scrollView);
        this.scrollFix(scrollView);
        for (D<T> d : ds) {
            List<D<T>> ds = d.ds;
            if (ds == null || ds.isEmpty()) {
                TextView content = this.titleView(d);
                if (titleGroup == 0) {
                    line.addView(content);
                    line.addView(vDivider());
                    line.addView(scrollView);
                } else if (titleGroup == this.ds.size() - 1) {
                    innerLine.addView(content);
                } else {
                    innerLine.addView(content);
                    innerLine.addView(vDivider());
                }
            } else {
                if (StringUtils.isNotBlank(d.title)) {
                    LinearLayout rect = this.rect(d.title, d.titleAlign, d.ds);
                    if (titleGroup == 0) {
                        line.addView(rect);
                        line.addView(vDivider());
                        line.addView(scrollView);
                    } else if (titleGroup == this.ds.size() - 1) {
                        innerLine.addView(rect);
                    } else {
                        innerLine.addView(rect);
                        innerLine.addView(vDivider());
                    }
                } else if (titleGroup == 0) {
                    int j = 0;
                    for (D<T> td : ds) {
                        TextView content = this.titleView(td);
                        if (j == ds.size() - 1) {
                            line.addView(content);
                            line.addView(vDivider());
                            line.addView(scrollView);
                        } else {
                            line.addView(content);
                            line.addView(vDivider());
                        }
                        j++;
                    }
                } else if (titleGroup == this.ds.size() - 1) {
                    int j = 0;
                    for (D<T> td : ds) {
                        TextView content = this.titleView(td);
                        if (j == ds.size() - 1) {
                            innerLine.addView(content);
                            innerLine.addView(vDivider());
                        } else {
                            innerLine.addView(content);
                        }
                        j++;
                    }
                } else {
                    for (D<T> td : ds) {
                        TextView content = this.titleView(td);
                        innerLine.addView(content);
                        innerLine.addView(vDivider());
                    }
                }

            }
            titleGroup++;
        }
        return line;
    }

    private TextView titleView(D<T> td) {
        TextView textView = textView();
        textView.setText(td.title);
        textView.setGravity(this.textAlign(td.titleAlign));
        this.addSortClickEvent(textView, td.sort);
        return textView;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addSortClickEvent(TextView textView, Comparator<T> comparator) {
        if (comparator == null) {
            return;
        }
        int col = titleCol++;
        if (this.titleGroup == 0) {
            textView.setOnClickListener((v) -> {
                this.comparator = comparator;
                this.sortCol = col;
                this.sortClick(v);
            });
        } else {
            textView.setOnTouchListener((v, event) -> {
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

    private void sortClick(View v) {
        ClickUtil.doClick(v, () -> {
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

    @SuppressLint("ClickableViewAccessibility")
    private void scrollFix(HorizontalScrollView scrollView) {
        OnTouchListener listener = (v, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) { // 手指点下时候事件处理
                clickX = event.getX();
                clickY = event.getY();
                synchronized (ExcelView.this) {
                    canceled.set(true);
                    if (touchView != null) {
                        touchView.fling(0);
                        touchView.setOnScrollChangeListener(null);
                    }
                    this.scrollAll(scrollView.getScrollX());
                    (touchView = scrollView).setOnScrollChangeListener(scrollChangeListener);
                }
            } else if (action == MotionEvent.ACTION_UP && sortClicked
                    && event.getX() == clickX && event.getY() == clickY) { // 手指放开事件处理
                this.sortClick(v);
                this.sortClicked = false;
            } else if (action != MotionEvent.ACTION_MOVE) { // 其他非移动情况处理
                synchronized (ExcelView.this) {
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
        };
        scrollView.setOnTouchListener(listener);
    }

    private TimerTask moveFixTask(HorizontalScrollView scrollView, Timer timer) {
        return new TimerTask() {
            @Override
            public void run() {
                synchronized (ExcelView.this) {
                    if (canceled.get()) {
                        timer.cancel();
                        running.set(false);
                        return;
                    }
                    int x = scrollView.getScrollX();
                    int scrollX = calculateFixScroll(x);
                    if (oldScrollX != x) {
                        oldScrollX = x;
                    } else {
                        scrollView.setOnScrollChangeListener(null);
                        scrollAll(scrollX);
                    }
                    if (scrollX == x) {
                        timer.cancel();
                        running.set(false);
                    }
                }
            }
        };
    }

    private int calculateFixScroll(int i) {
        float dp = pxToDp(i);
        int v = (int) (dp / colWidth);
        return dpToPx(colWidth * (dp - colWidth * v < colWidth / 2 ? v : v + 1)) + v; //
    }

    private LinearLayout line() {
        LinearLayout line = new LinearLayout(context);
        LayoutParams param = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        line.setLayoutParams(param);
        return line;
    }

    private LinearLayout rect(String text, int align, List<D<T>> ds) {
        LinearLayout line = new LinearLayout(context);
        line.setLayoutParams(new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        line.setOrientation(VERTICAL);
        TextView textView = this.textView();
        textView.setWidth(ds.size() * dpToPx(colWidth));
        textView.setHeight(dpToPx(HEIGHT / 2f) - 1);
        textView.setText(text);
        textView.setGravity(textAlign(align));
        line.addView(textView);
        line.addView(hDivider());
        LinearLayout bottom = new LinearLayout(context);
        bottom.setLayoutParams(new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        line.addView(bottom);
        int i = 0;
        for (D<T> d : ds) {
            TextView content = this.titleView(d);
            if (i != 0) {
                bottom.addView(vDivider());
            }
            bottom.addView(content);
            i++;
        }
        return line;
    }

    private HorizontalScrollView horizontalScrollView() {
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(context);
        horizontalScrollView.setLayoutParams(param);
        horizontalScrollView.setScrollBarSize(0);
        return horizontalScrollView;
    }

    private ListView listView() {
        ListView listView = new ListView(context);
        listView.setLayoutParams(param);
        listView.setScrollBarSize(SCROLL_BAR_SIZE);
        listView.setAdapter(adapter);
        listView.setDivider(new GradientDrawable(RIGHT_LEFT, new int[]{BG_COLOR, BG_COLOR, BG_COLOR}));
        listView.setDividerHeight(1);
        listView.setOnScrollChangeListener((v, x, y, ox, oy) -> {
            int count = listView.getChildCount();
            if (childCount < count) {
                childCount = count;
                this.scrollAll(this.scrollX);
            }
        });
        return listView;
    }

    public int getListSize() {
        return list.size();
    }

    private int textAlign(int textAlign) {
        if (textAlign == 1) {
            return Gravity.START | Gravity.CENTER;
        } else if (textAlign == 2) {
            return Gravity.END | Gravity.CENTER;
        } else {
            return Gravity.CENTER;
        }
    }

    private TextView textView() {
        TextView view = new TextView(context);
        view.setWidth(dpToPx(colWidth));
        view.setHeight(dpToPx(HEIGHT));
        view.setPadding(dpToPx(PADDING), 0, dpToPx(PADDING), 0);
        return view;
    }

    private View hDivider() {
        return divider(MATCH_PARENT, 1);
    }

    private View vDivider() {
        return divider(1, MATCH_PARENT);
    }

    private View divider(int w, int h) {
        View view = new View(context);
        LayoutParams param = new LayoutParams(w, h);
        view.setLayoutParams(param);
        view.setBackgroundColor(BG_COLOR);
        return view;
    }

    private LinearLayout contentView() {
        List<View> textViews = new ArrayList<>();
        LinearLayout line = this.line();
        line.setTag(textViews);
        HorizontalScrollView scrollView = this.horizontalScrollView();
        scrollView.setOnScrollChangeListener(scrollChangeListener);
        LinearLayout innerLine = this.line();
        scrollView.addView(innerLine);
        horizontalScrollViews.add(scrollView);
        this.scrollFix(scrollView);
        int size = ds.size();
        for (int i = 0; i < size; i++) {
            D<T> d = ds.get(i);
            List<D<T>> ds = d.ds;
            if (ds == null || ds.isEmpty()) {
                TextView content = textView();
                textViews.add(content);
                if (i == 0) {
                    line.addView(content);
                    line.addView(vDivider());
                    line.addView(scrollView);
                } else if (i == size - 1) {
                    innerLine.addView(content);
                } else {
                    innerLine.addView(content);
                    innerLine.addView(vDivider());
                }
            } else {
                if (i == 0) {
                    for (int j = 0; j < ds.size(); j++) {
                        TextView content = textView();
                        textViews.add(content);
                        if (j == ds.size() - 1) {
                            line.addView(content);
                            line.addView(vDivider());
                            line.addView(scrollView);
                        } else {
                            line.addView(content);
                            line.addView(vDivider());
                        }
                    }
                } else if (i == size - 1) {
                    for (int j = 0; j < ds.size(); j++) {
                        TextView content = textView();
                        textViews.add(content);
                        if (j == ds.size() - 1) {
                            innerLine.addView(content);
                        } else {
                            innerLine.addView(content);
                            innerLine.addView(vDivider());
                        }
                    }
                } else {
                    for (int j = 0; j < ds.size(); j++) {
                        TextView content = textView();
                        textViews.add(content);
                        innerLine.addView(content);
                        innerLine.addView(vDivider());
                    }
                }
            }
        }
        return line;
    }

    private void scrollAll(int x) {
        for (HorizontalScrollView v : horizontalScrollViews) {
            v.scrollTo(x, 0);
        }
    }

    private boolean handleMessage(Message msg) {
        list = tempList;
        adapter.notifyDataSetChanged();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = contentView();
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
            return convertView;
        }

        private void addAction(List<TextView> textViews, T item, int num, D<T> d) {
            TextView textView = textViews.get(num);
            textView.setText(d.content.apply(item));
            textView.setGravity(textAlign(d.contentAlign));
            if (d.click != null) {
                textView.setOnClickListener(v -> {
                    try {
                        d.click.accept(item);
                    } catch (Throwable t) {
                        PopUtil.alert(context, t.getMessage());
                    }
                });
            }
        }
    }

}
