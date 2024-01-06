package org.nature.common.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

public class SearchBar extends BasicView {

    private final LayoutParams params;

    private LinearLayout conditions;

    private LinearLayout handles;

    public SearchBar(Context context) {
        super(context);
        this.context = context;
        this.params = new LayoutParams(MATCH_PARENT, dpToPx(50));
        this.setLayoutParams(params);
        this.setOrientation(HORIZONTAL);
        this.makeStructure();
    }

    private void makeStructure() {
        conditions = part(1);
        conditions.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        handles = part(5);
        handles.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        this.addView(conditions);
        this.addView(handles);
    }

    public void addConditionView(View view) {
        this.conditions.addView(view);
    }

    public void addHandleView(View view) {
        this.handles.addView(view);
    }

    private LinearLayout part(int weight) {
        LinearLayout layout = new LinearLayout(context);
        LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        params.weight = weight;
        layout.setLayoutParams(params);
        return layout;
    }

    public void setWidth(float width) {
        this.params.width = this.dpToPx(width);
    }

    public void setHeight(float height) {
        this.params.height = this.dpToPx(height);
    }

}
