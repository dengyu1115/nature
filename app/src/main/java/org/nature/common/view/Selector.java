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
@SuppressWarnings("unchecked")
@SuppressLint({"ResourceAsColor", "UseCompatLoadingForDrawables", "ViewConstructor"})
public class Selector<T> extends BasicView {

    private final Context context;
    private final LayoutParams params, tps;
    private final Drawable drawable;
    private final int height;
    private final Adapter adapter;
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
        this.drawable = context.getDrawable(R.drawable.common_background);
        this.adapter = new Adapter();
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

    public void refreshData(List<T> data) {
        this.data = data;
        this.adapter.notifyDataSetChanged();
        if (!data.isEmpty() && (valueView.getText() == null || valueView.getText().length() == 0)) {
            this.doSelect(0, valueView);
            value = (T) valueView.getTag();
            if (changeRun != null) {
                changeRun.run();
            }
        }
    }


    public T getValue() {
        return (T) valueView.getTag();
    }

    public void setValue(T t) {
        valueView.setTag(t);
        valueView.setText(mapper.apply(t));
    }

    private void makeStructure() {
        this.setOrientation(VERTICAL);
        this.setLayoutParams(params);
        this.setBackground(drawable);
        valueView = this.textView();
        this.addView(valueView);
        this.addView(this.divider());
        popup = new PopupWindow(context);
        listView = new ListView(context);
        listView.setAdapter(adapter);
        popup.setContentView(listView);
        popup.setHeight(this.height);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(drawable);
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

    private TextView textView() {
        TextView textView = new TextView(context);
        textView.setLayoutParams(this.tps);
        textView.setGravity(Gravity.START | Gravity.CENTER);
        textView.setPadding(this.dpToPx(10), 1, 1, 1);
        return textView;
    }

    private View divider() {
        View view = new View(context);
        view.setLayoutParams(new LayoutParams(MATCH_PARENT, 3));
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