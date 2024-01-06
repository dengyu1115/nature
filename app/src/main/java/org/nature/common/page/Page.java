package org.nature.common.page;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;


public abstract class Page {

    protected static final int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;
    protected static final int C = 0, S = 1, E = 2;
    private LinearLayout page;
    private BasicPage basic;
    private Object param;

    public void doCreate(BasicPage basic) {
        this.basic = basic;
        Context context = basic.getContext();
        this.page = new LinearLayout(context);
        this.makeStructure(this.page, context);
    }

    public View get() {
        return this.page;
    }

    @SuppressWarnings("unchecked")
    protected <P> P getParam() {
        return (P) this.param;
    }

    public <P> void setParam(P param) {
        this.param = param;
    }

    protected void setOrientation(int orientation) {
        this.page.setOrientation(orientation);
    }

    protected <T extends Page> void show(Class<T> clz) {
        this.basic.show(clz);
    }

    protected <T extends Page, P> void show(Class<T> clz, P param) {
        this.basic.show(clz, param);
    }

    protected abstract void makeStructure(LinearLayout page, Context context);

    protected abstract void onShow();

    protected boolean isProtocol() {
        return false;
    }

}
