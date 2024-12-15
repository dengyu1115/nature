package org.nature.common.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.nature.common.exception.Warn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static org.nature.common.constant.Const.PAD;

/**
 * Tab
 * @author Nature
 * @version 1.0.0
 * @since 2024/12/15
 */
@SuppressLint({"ViewConstructor", "UseCompatLoadingForDrawables", "DefaultLocale", "ResourceType"})
public class Tab<T> extends LinearLayout {

    private static final int BG_COLOR = Color.parseColor("#ff99cc00");

    private final Context context;
    private final int height, colWidth;
    private final LinearLayout row;
    private final List<LinearLayout> tabs;


    private Consumer<T> onChange;
    private Function<T, String> mapper;

    private T value;

    public Tab(Context context, int width, int height, int columns) {
        super(context);
        this.context = context;
        this.colWidth = (int) ((width - PAD * 2) / (float) columns + 0.5f);
        this.height = height;
        this.setLayoutParams(new LayoutParams(width, height));
        this.setPadding(PAD, PAD, PAD, PAD);
        HorizontalScrollView hsv;
        this.addView(hsv = this.buildHorizontalscrollview());
        hsv.addView(this.row = this.buildRowView());
        this.tabs = new ArrayList<>();
    }

    public void setMapper(Function<T, String> mapper) {
        this.mapper = mapper;
    }

    public void setData(List<T> data) {
        Warn.check(() -> data == null, "data is null");
        Warn.check(() -> mapper == null, "mapper is null");
        this.row.removeAllViews();
        this.tabs.clear();
        for (int i = data.size(); i > 0; i--) {
            T datum = data.get(i - 1);
            LinearLayout tab = this.buildTabView(datum);
            this.row.addView(tab, 0);
            this.tabs.add(tab);
            if (i == 1) {
                this.doSelectItem(datum, tab);
            }
        }
    }

    public T getValue() {
        return value;
    }

    public void onChange(Consumer<T> consumer) {
        this.onChange = consumer;
    }

    /**
     * 水平滚动view
     * @return HorizontalScrollView
     */
    private HorizontalScrollView buildHorizontalscrollview() {
        HorizontalScrollView hsv = new HorizontalScrollView(context);
        hsv.setScrollBarSize(0);
        hsv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        return hsv;
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

    private LinearLayout buildTabView(T datum) {
        LinearLayout tab = new LinearLayout(context);
        tab.setLayoutParams(new LayoutParams(colWidth, height - PAD * 2));
        tab.setOrientation(VERTICAL);
        TextView text = new TextView(context);
        text.setGravity(Gravity.CENTER);
        text.setLayoutParams(new LayoutParams(colWidth, height - PAD * 2 - 1));
        text.setTag(datum);
        text.setText(this.mapper.apply(datum));
        tab.addView(text);
        View line = new View(context);
        line.setLayoutParams(new LayoutParams(colWidth, 1));
        line.setBackgroundColor(BG_COLOR);
        tab.addView(line);
        text.setOnClickListener(v -> this.doSelectItem(datum, tab));
        return tab;
    }

    private void doSelectItem(T datum, LinearLayout tab) {
        if (!Objects.equals(value, datum)) {
            value = datum;
            if (this.onChange != null) {
                this.onChange.accept(value);
            }
        }
        this.tabs.forEach(i -> {
            i.setBackgroundColor(Color.TRANSPARENT);
            i.getChildAt(1).setVisibility(i == tab ? VISIBLE : GONE);
        });
    }

}
