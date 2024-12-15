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

import static org.nature.common.constant.Const.PAD;

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
    private final int width, height, popupHeight;
    private final TextView valueView;
    private final LinearLayout container;
    private final PopupWindow popup;
    private final ImageView arrow;
    private final Adapter adapter;
    private Function<T, String> mapper;
    private Runnable changeRun;
    private List<T> data;
    private T value;

    public Selector(Context context, int width, int height) {
        super(context);
        this.context = context;
        this.data = new ArrayList<>();
        this.width = width;
        this.height = height;
        this.popupHeight = (height - 10) * 5;
        this.setLayoutParams(new LayoutParams(width, height));
        this.setPadding(PAD, PAD, PAD, PAD);
        this.container = this.buildContainer();
        this.addView(container);
        this.adapter = new Adapter();
        this.valueView = this.buildTextView();
        this.valueView.setLayoutParams(new LayoutParams(width - 30 - PAD * 2, height - PAD * 2));
        this.arrow = this.buildArrow(context);
        container.addView(valueView);
        container.addView(arrow);
        this.popup = this.buildPopup();
    }

    public void mapper(Function<T, String> mapper) {
        this.mapper = mapper;
    }

    public void onChangeRun(Runnable run) {
        this.changeRun = run;
    }

    public void setData(List<T> data) {
        this.data = data;
        this.adapter.notifyDataSetChanged();
        if (!data.isEmpty() && valueView.getTag() == null) {
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

    private LinearLayout buildContainer() {
        LinearLayout container = new LinearLayout(this.context);
        container.setLayoutParams(new LayoutParams(width - PAD * 2, height - PAD * 2));
        container.setBackground(context.getDrawable(R.drawable.bg_normal));
        return container;
    }

    private PopupWindow buildPopup() {
        PopupWindow popup = new PopupWindow(context);
        popup.setContentView(this.buildListView());
        popup.setHeight(this.popupHeight);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(context.getDrawable(R.drawable.bg_normal));
        this.setOnClickListener(v -> {
            arrow.setImageDrawable(context.getDrawable(R.drawable.icon_arrow_up));
            popup.setWidth(width - 10);
            popup.showAsDropDown(container, 0, 1);
        });
        popup.setOnDismissListener(() -> arrow.setImageDrawable(context.getDrawable(R.drawable.icon_arrow_down)));
        return popup;
    }

    private ListView buildListView() {
        ListView listView = new ListView(context);
        listView.setAdapter(adapter);
        listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        listView.setScrollBarSize(1);
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

    private TextView buildTextView() {
        TextView textView = new TextView(context);
        textView.setLayoutParams(new LayoutParams(width - PAD * 2, height - PAD * 2 - 3));
        textView.setGravity(Gravity.START | Gravity.CENTER);
        textView.setPadding(30, 1, 1, 1);
        return textView;
    }

    private ImageView buildArrow(Context context) {
        ImageView image = new ImageView(context);
        image.setLayoutParams(new LayoutParams(30, height - PAD * 2));
        image.setImageDrawable(context.getDrawable(R.drawable.icon_arrow_down));
        return image;
    }

    private void doSelect(int i, TextView valueView) {
        T t = data.get(i);
        valueView.setTag(t);
        valueView.setText(mapper.apply(t));
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
                convertView = Selector.this.buildTextView();
            }
            Selector.this.doSelect(pos, (TextView) convertView);
            return convertView;
        }
    }

}