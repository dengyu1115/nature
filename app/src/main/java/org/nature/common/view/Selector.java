package org.nature.common.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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
public class Selector<T> extends LinearLayout {

    private final Context context;
    private final LayoutParams params, tps;
    private final int height;
    private final Adapter adapter;
    private PopupWindow popup;
    private TextView valueView;
    private Function<T, String> mapper;
    private Runnable changeRun;
    private List<T> data;
    private T value;

    public Selector(Context context, int width, int height) {
        super(context);
        this.context = context;
        this.data = new ArrayList<>();
        this.height = height * 5;
        this.params = new LayoutParams(width, height);
        this.tps = new LayoutParams(width, height - 3);
        this.adapter = new Adapter();
        this.makeStructure();
    }

    public void mapper(Function<T, String> mapper) {
        this.mapper = mapper;
    }

    public void onChangeRun(Runnable run) {
        this.changeRun = run;
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
        this.setLayoutParams(params);
        this.setBackground(context.getDrawable(R.drawable.bg_normal));
        this.addView(valueView = this.textView());
        this.buildPopup();
    }

    private void buildPopup() {
        popup = new PopupWindow(context);
        popup.setContentView(this.buildListView());
        popup.setHeight(this.height);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(context.getDrawable(R.drawable.bg_normal));
        this.setOnClickListener(v -> {
            popup.setWidth(this.getWidth());
            popup.showAsDropDown(this, 0, 1);
        });
    }

    private ListView buildListView() {
        ListView listView = new ListView(context);
        listView.setAdapter(adapter);
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
        return listView;
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
        textView.setPadding(30, 1, 1, 1);
        return textView;
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