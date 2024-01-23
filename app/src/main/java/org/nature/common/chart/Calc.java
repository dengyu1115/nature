package org.nature.common.chart;

import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 参数计算
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/23
 */
public class Calc<T> {

    /**
     * 参数
     */
    private final P<T> p;
    /**
     * 坐标：整图、图形框
     */
    private final XY all, rect;

    private final View view;

    public Calc(P<T> p, View view, XY rect) {
        this.p = p;
        this.view = view;
        this.all = new XY();
        this.rect = rect;
    }

    /**
     * 参数计算
     * @param data 数据
     */
    public void params(List<T> data) {
        this.xParams(data);
        for (R<T> r : p.rs) {
            r.calcParams(data);
        }
    }

    /**
     * 计算X轴参数
     * @param data 数据集合
     */
    private void xParams(List<T> data) {
        List<String> dates = data.stream().map(p.xText).collect(Collectors.toList());
        p.xTexts = new ArrayList<>();
        int size = dates.size();
        int middle = size % 2 == 0 ? size / 2 : size / 2 + 1;
        if (middle > size - 1) middle = size - 1;
        p.xTexts.addAll(Arrays.asList(dates.get(0), dates.get(middle), dates.get(size - 1)));
        p.intervalX = (int) ((rect.ex - rect.sx) / (double) (p.xTexts.size() - 1) + 0.5d);
        p.unitX = (float) (rect.ex - rect.sx) / (data.size() - 1);
    }

    /**
     * 固定整体
     */
    public void allIndex() {
        int width = view.getWidth();
        int height = view.getHeight();
        if ((float) width / (float) height > 1.8f) {
            // 宽高比保持4：3
            all.sy = 0;
            all.ey = height;
            all.sx = (int) (width / 2f - height / 2f * 2f + 0.5f);
            all.ex = (int) (width / 2f + height / 2f * 2f + 0.5f);
        } else {
            all.sx = 0;
            all.ex = width;
            all.sy = (int) (height / 2f - width / 8f * 3f + 0.5f);
            all.ey = (int) (height / 2f + width / 8f * 3f + 0.5f);
        }
    }

    /**
     * 固定图形部分
     */
    public void rectIndex() {
        rect.sx = (int) (all.sx / 20f * 18f + all.ex / 20f * 2f + 0.5f);
        rect.ex = (int) (all.sx / 20f + all.ex / 20f * 19f + 0.5f);
        rect.sy = (int) (all.sy / 20f * 17f + all.ey / 20f * 3f + 0.5f);
        rect.ey = (int) (all.sy / 20f + all.ey / 20f * 19f + 0.5f);
        double total = p.rs.stream().mapToDouble(d -> d.weight).sum();
        int unit = (int) ((rect.ey - rect.sy) / total + 0.5d);
        org.nature.common.chart.R<T> r = p.rs.get(0);
        int sy = rect.sy, ey = sy;
        r.fix(sy, ey);
        for (int i = 0; i < p.rs.size() - 1; i++) {
            r = p.rs.get(i);
            ey = ey + r.weight * unit;
            r.fix(sy, ey);
            sy = ey;
        }
        ey = rect.ey;
        p.rs.get(p.rs.size() - 1).fix(sy, ey);
    }

    /**
     * 固定指标部分
     */
    public void quotaIndex() {
        int[] sxs = new int[5];
        int[] exs = new int[5];
        int[] ys = new int[3];
        int sx = (int) (all.sx * 19f / 20f + all.ex * 1f / 20f + 0.5f);
        int ex = (int) (all.sx * 1f / 20f + all.ex * 19f / 20f + 0.5f);
        float diff = (ex - sx) / 5f;
        for (int i = 0; i < 5; i++) {
            sxs[i] = (int) (sx / 5 * (5 - i) + ex / 5 * i + 3.5f);
            exs[i] = (int) (sx / 5 * (5 - i) + ex / 5 * i + diff - 3.5f);
        }
        int sy = (int) (all.sy / 10f * 8f + rect.sy / 10f * 2f + 0.5f);
        int ey = (int) (all.sy / 10f + rect.sy / 10f * 9f + 0.5f);
        int dif = (ey - sy) / p.qs.size();
        for (int i = 0; i < p.qs.size(); i++) {
            ys[i] = sy + dif * i;
        }
        for (int i = 0; i < p.qs.size(); i++) {
            List<Q<T>> q = p.qs.get(i);
            for (int j = 0; j < q.size(); j++) {
                q.get(j).fix(sxs[j], exs[j], ys[i]);
            }
        }
    }

}
