package org.nature.common.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import org.nature.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * 下拉选择器
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/14
 */
@SuppressLint("ViewConstructor")
public class Selector<T> extends BasicView {

    private final Context context;
    private final LayoutParams params, tps;
    private final int height;
    private PopupWindow popup;
    private ListView listView;
    private TextView valueView;
    private Function<T, String> mapper;
    private Runnable changeRun;
    private List<T> data;
    private T value;

    public Selector(Context context, int w, int h) {
        super(context);
        this.context = context;
        this.data = new ArrayList<>();
        this.height = this.dpToPx(h * 5);
        this.params = new LayoutParams(this.dpToPx(w), this.dpToPx(h));
        this.tps = new LayoutParams(this.dpToPx(w), this.dpToPx(h) - 3);
        this.makeStructure();
    }

    public Selector<T> mapper(Function<T, String> mapper) {
        this.mapper = mapper;
        return this;
    }

    public Selector<T> onChangeRun(Runnable run) {
        this.changeRun = run;
        return this;
    }

    public Selector<T> init() {
        Adapter adapter = new Adapter();
        listView.setAdapter(adapter);
        return this;
    }

    public void setHeight(float height) {
        this.params.height = this.dpToPx(height);
    }

    public void setWidth(float width) {
        this.params.width = this.dpToPx(width);
    }

    @SuppressWarnings("unchecked")
    public T getValue() {
        return (T) valueView.getTag();
    }

    public void setValue(T t) {
        valueView.setTag(t);
        valueView.setText(mapper.apply(t));
    }

    @SuppressWarnings("unchecked")
    @SuppressLint({"ResourceAsColor", "UseCompatLoadingForDrawables"})
    private void makeStructure() {
        Drawable drawable = context.getDrawable(R.drawable.common_background);
        popup = new PopupWindow(context);
        valueView = this.textView();
        this.setOrientation(VERTICAL);
        this.addView(valueView);
        this.addView(this.divider(MATCH_PARENT, 3));
        listView = new ListView(context);
        LayoutParams param = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        listView.setLayoutParams(param);
        popup.setContentView(listView);
        popup.setHeight(this.height);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(drawable);
        this.setLayoutParams(params);
        this.setBackground(drawable);
        this.setOnClickListener(v -> {
            popup.setWidth(this.getWidth());
            popup.showAsDropDown(this, 0, 1);
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            popup.dismiss();
            TextView textView = (TextView) view;
            valueView.setText(textView.getText());
            Object tag = textView.getTag();
            valueView.setTag(tag);
            if (!Objects.equals(value, tag)) {
                value = (T) tag;
                if (changeRun != null) {
                    changeRun.run();
                }
            }
        });
    }

    private void doSelect(int i, TextView valueView) {
        T t = data.get(i);
        valueView.setTag(t);
        valueView.setText(mapper.apply(t));
    }

    @SuppressWarnings("unchecked")
    public void refreshData(List<T> data) {
        this.data = data;
        if (!data.isEmpty() && (valueView.getText() == null || valueView.getText().length() == 0)) {
            this.doSelect(0, valueView);
            value = (T) valueView.getTag();
            if (changeRun != null) {
                changeRun.run();
            }
        }
    }

    private TextView textView() {
        TextView textView = new TextView(context);
        textView.setLayoutParams(this.tps);
        textView.setGravity(Gravity.START | Gravity.CENTER);
        textView.setPadding(this.dpToPx(10), 1, 1, 1);
        return textView;
    }

    private View divider(int w, int h) {
        View view = new View(context);
        LayoutParams param = new LayoutParams(w, h);
        view.setLayoutParams(param);
        view.setBackgroundColor(BG_COLOR);
        return view;
    }

    class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public T getItem(int pos) {
            return data.get(pos);
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = textView();
            }
            Selector.this.doSelect(pos, (TextView) convertView);
            return convertView;
        }
    }

}