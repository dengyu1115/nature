package org.nature.common.page;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import org.nature.common.ioc.holder.InstanceHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;


public class BasicPage extends LinearLayout {

    private final Stack<Page> pages;

    private final Map<Class<?>, Page> map;

    public BasicPage(Context context) {
        super(context);
        this.pages = new Stack<>();
        this.map = new HashMap<>();
        ViewGroup.LayoutParams params = new LayoutParams(2228, 1080);
        this.setLayoutParams(params);
    }

    public <T extends Page> void show(Class<T> clz) {
        this.show(clz, null);
    }

    public <T extends Page, P> void show(Class<T> clz, P param) {
        Page page = map.get(clz);
        if (page != null) {
            page.setParam(param);
            this.show(page);
        } else {
            try {
                page = InstanceHolder.get(clz);
                page.doCreate(this);
                page.setParam(param);
                if (!page.isProtocol()) {
                    map.put(clz, page);
                }
                this.show(page);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void show(Page page) {
        this.viewHandle(v -> v.setVisibility(GONE));
        this.addView(page.get());
        pages.push(page);
        page.onShow();
    }

    public int viewSize() {
        return pages.size();
    }

    public void dispose() {
        Page page = pages.pop();
        this.removeView(page.get());
        this.viewHandle(v -> v.setVisibility(VISIBLE));
    }

    private void viewHandle(Consumer<View> consumer) {
        if (pages.isEmpty()) {
            return;
        }
        Page p = pages.peek();
        consumer.accept(p.get());
    }

}
