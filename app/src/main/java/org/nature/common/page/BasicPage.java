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

/**
 * 基础页面
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/9
 */
public class BasicPage extends LinearLayout {

    private final Stack<Page> pages;

    private final Map<Class<?>, Page> map;

    public BasicPage(Context context) {
        super(context);
        this.pages = new Stack<>();
        this.map = new HashMap<>();
        // 页面宽高设置
        ViewGroup.LayoutParams params = new LayoutParams(2228, 1080);
        this.setLayoutParams(params);
    }

    /**
     * 展示
     * @param clz 页面类
     */
    public <T extends Page> void show(Class<T> clz) {
        this.show(clz, null);
    }

    /**
     * 展示
     * @param clz   页面类
     * @param param 参数
     */
    public <T extends Page, P> void show(Class<T> clz, P param) {
        // 获取页面，已创建的页面直接获取
        Page page = map.get(clz);
        if (page != null) {
            page.setParam(param);
            this.show(page);
        } else {
            // 未创建的新创建
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

    /**
     * 展示页面
     * @param page 页面
     */
    public void show(Page page) {
        this.viewHandle(v -> v.setVisibility(GONE));
        this.addView(page.get());
        pages.push(page);
        page.onShow();
    }

    /**
     * 页面数量
     * @return int
     */
    public int viewSize() {
        return pages.size();
    }

    /**
     * 推出页面
     */
    public void dispose() {
        Page page = pages.pop();
        this.removeView(page.get());
        this.viewHandle(v -> v.setVisibility(VISIBLE));
    }

    /**
     * view处理
     * @param consumer 处理逻辑
     */
    private void viewHandle(Consumer<View> consumer) {
        if (pages.isEmpty()) {
            return;
        }
        Page p = pages.peek();
        consumer.accept(p.get());
    }

}
