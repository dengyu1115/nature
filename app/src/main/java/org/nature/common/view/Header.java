package org.nature.common.view;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

public class Header extends BasicView {

    private final LayoutParams params;

    private LinearLayout left;

    private LinearLayout center;

    private LinearLayout right;

    public Header(Context context) {
        super(context);
        this.context = context;
        this.params = new LayoutParams(MATCH_PARENT, dpToPx(50));
        this.setLayoutParams(params);
        this.setOrientation(HORIZONTAL);
        this.makeStructure();
    }

    private void makeStructure() {
        left = part(2);
        center = part(1);
        right = part(2);
        this.addView(left);
        this.addView(center);
        this.addView(right);
    }

    public void leftAddView(View view) {
        this.left.addView(view);
    }

    public void centerAddView(View view) {
        this.center.addView(view);
    }

    public void rightAddView(View view) {
        this.right.addView(view);
    }

    private LinearLayout part(int weight) {
        LinearLayout layout = new LinearLayout(context);
        LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        params.weight = weight;
        layout.setLayoutParams(params);
        layout.setGravity(Gravity.CENTER);
        return layout;
    }

    public void setWidth(float width) {
        this.params.width = this.dpToPx(width);
    }

    public void setHeight(float height) {
        this.params.height = this.dpToPx(height);
    }

}
