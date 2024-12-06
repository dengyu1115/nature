package org.nature.common.page;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import org.nature.common.view.ViewTemplate;

/**
 * 页面基类
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/9
 */
public abstract class Page {

    protected static final int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;
    protected static final int C = 0, S = 1, E = 2;
    protected Context context;
    protected ViewTemplate template;
    protected LinearLayout page;
    private BasicPage basic;
    private Object param;

    /**
     * 创建页面
     * @param basic 页面上下文
     */
    public void doCreate(BasicPage basic) {
        this.basic = basic;
        this.context = basic.getContext();
        this.template = ViewTemplate.build(context);
        this.page = new LinearLayout(context);
        this.makeStructure();
    }

    /**
     * 获取页面view
     * @return View
     */
    public View get() {
        return this.page;
    }

    /**
     * 获取页面入参
     * @return 入参
     */
    @SuppressWarnings("unchecked")
    protected <P> P getParam() {
        return (P) this.param;
    }

    /**
     * 设置页面入参
     * @param param 入参
     */
    public <P> void setParam(P param) {
        this.param = param;
    }

    /**
     * 设置页面布局方向
     * @param orientation 布局方向
     */
    protected void setOrientation(int orientation) {
        this.page.setOrientation(orientation);
    }

    /**
     * 展示
     * @param clz 页面类
     */
    protected <T extends Page> void show(Class<T> clz) {
        this.basic.show(clz);
    }

    /**
     * 展示
     * @param clz   页面类
     * @param param 参数
     */
    protected <T extends Page, P> void show(Class<T> clz, P param) {
        this.basic.show(clz, param);
    }

    /**
     * 页面布局创建
     */
    protected abstract void makeStructure();

    /**
     * 展示前处理
     */
    protected abstract void onShow();

    /**
     * 是否多实例
     * @return true多实例
     */
    protected boolean isProtocol() {
        return false;
    }

}
