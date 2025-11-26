package org.nature.common.chart;

import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
    private final Param<T> param;
    /**
     * 坐标：整图、图形框
     */
    private final XyIndex all, rect;

    private final View view;

    public Calc(Param<T> param, View view, XyIndex rect) {
        this.param = param;
        this.view = view;
        this.all = new XyIndex();
        this.rect = rect;
    }

    /**
     * 参数计算
     * @param data 数据
     */
    public void params(List<T> data) {
        this.xParams(data);
        for (BaseRect<T> r : param.rs) {
            r.calcParams(data);
        }
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
        double total = param.rs.stream().mapToDouble(d -> d.weight).sum();
        int unit = (int) ((rect.ey - rect.sy) / total + 0.5d);
        BaseRect<T> r = param.rs.get(0);
        int sy = rect.sy, ey = sy;
        r.fix(sy, ey);
        for (int i = 0; i < param.rs.size() - 1; i++) {
            r = param.rs.get(i);
            ey = ey + r.weight * unit;
            r.fix(sy, ey);
            sy = ey;
        }
        ey = rect.ey;
        param.rs.get(param.rs.size() - 1).fix(sy, ey);
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
            sxs[i] = (int) ((float) sx / 5 * (5 - i) + (float) ex / 5 * i + 3.5f);
            exs[i] = (int) ((float) sx / 5 * (5 - i) + (float) ex / 5 * i + diff - 3.5f);
        }
        int sy = (int) (all.sy / 10f * 8f + rect.sy / 10f * 2f + 0.5f);
        int ey = (int) (all.sy / 10f + rect.sy / 10f * 9f + 0.5f);
        int dif = (ey - sy) / param.qs.size();
        for (int i = 0; i < param.qs.size(); i++) {
            ys[i] = sy + dif * i;
        }
        for (int i = 0; i < param.qs.size(); i++) {
            List<Quota<T>> quota = param.qs.get(i);
            for (int j = 0; j < quota.size(); j++) {
                quota.get(j).fix(sxs[j], exs[j], ys[i]);
            }
        }
    }

    /**
     * 计算X轴参数
     * @param data 数据集合
     */
    private void xParams(List<T> data) {
        List<String> dates = data.stream().filter(Objects::nonNull).map(param.xText).collect(Collectors.toList());
        param.xTexts = new ArrayList<>();
        int size = dates.size();
        int middle = size % 2 == 0 ? size / 2 : size / 2 + 1;
        if (middle > size - 1) middle = size - 1;
        param.xTexts.addAll(Arrays.asList(dates.get(0), dates.get(middle), dates.get(size - 1)));
        param.intervalX = (int) ((rect.ex - rect.sx) / (double) (param.xTexts.size() - 1) + 0.5d);
        param.unitX = (float) (rect.ex - rect.sx) / (data.size() - 1);
    }

}
