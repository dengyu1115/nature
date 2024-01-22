package org.nature.common.chart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import org.nature.common.exception.Warn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 折线图
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/22
 */
public class LineChart<T> extends View {

    /**
     * 坐标：整图、图形框
     */
    private final XY all, rect;
    /**
     * 画笔
     */
    private final Paint paint;
    /**
     * 数据量：默认、最小、最大
     */
    private int sizeDefault = 90, sizeMin = 30, sizeMax = 1800;
    /**
     * 指标数据集合
     */
    private List<List<Q<T>>> qs;
    /**
     * 内容数据集合
     */
    private List<org.nature.common.chart.R<T>> rs;
    /**
     * X轴文案获取函数
     */
    private Function<T, String> xText;
    /**
     * 空数据
     */
    private T empty;
    /**
     * 数据：全部、展示
     */
    private List<T> data, list;
    /**
     * 当前数据
     */
    private T curr;
    /**
     * X轴文案集合
     */
    private List<String> xTexts;
    /**
     * X单位长度、下标、集合大小、展示开始下标、展示结束下标
     */
    private int intervalX, index, listSize = sizeDefault, listStart, listEnd;
    /**
     * X单位长度
     */
    private float unitX, dx, lx, ly;
    /**
     * 状态：长按、移动
     */
    private boolean longPressed, moving;

    public LineChart(Context context) {
        super(context);
        this.all = new XY();
        this.rect = new XY();
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void sizeDefault(int sizeDefault, int sizeMin, int sizeMax) {
        this.sizeDefault = sizeDefault;
        this.sizeMin = sizeMin;
        this.sizeMax = sizeMax;
    }

    public void init(List<List<Q<T>>> qs, List<org.nature.common.chart.R<T>> rs, Function<T, String> xText, T empty) {
        this.qs = qs;
        this.rs = rs;
        this.xText = xText;
        this.empty = empty;
    }

    public void data(List<T> data) {
        this.data = data;
        if (data == null) {
            throw new Warn("data is null");
        }
        int size = data.size();
        if (listSize < size) {  // 数量超出的截取尾部展示
            this.list = data.subList(listStart = size - listSize, listEnd = size);
        } else if (size >= sizeMin) { // 数量超过最小size全部展示
            this.list = data;
            listStart = 0;
            listEnd = size;
        } else {    // 数据量不足，补全后展示
            this.list = new ArrayList<>();
            for (int i = 0; i < sizeMin - size; i++) {
                list.add(this.empty);
            }
            list.addAll(data);
            this.data = list;
            listStart = 0;
            listEnd = list.size();
        }
        this.listSize = list.size();
        this.index = list.size() - 1;
        this.curr = this.list.get(this.index);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            this.actionDown(event);
        } else if (action == MotionEvent.ACTION_UP) {
            this.actionUp(event);
        } else if (action == MotionEvent.ACTION_MOVE) {
            this.actionMove(event);    // 长按处理
        }
        return true;
    }

    private void actionMove(MotionEvent event) {
        if (event.getPointerCount() == 2) { // 双指操作放大缩小
            this.doScaleList(event);
        } else {
            if (!moving) {
                float mx = Math.abs(event.getX() - lx);
                float my = Math.abs(event.getY() - ly);
                if (mx < 10 && my < 10) {
                    longPressed = (event.getEventTime() - event.getDownTime()) > 500L;
                } else {
                    moving = true;
                    ly = event.getY();
                    lx = event.getX();
                }
            }
            if (longPressed) {  // 长按移动下标
                this.doMoveIndex(event);
            } else if (moving) {
                this.doMoveList(event);
            }
        }
    }

    private void actionUp(MotionEvent event) {
        longPressed = false;
        moving = false;
        dx = 0;
    }

    private void actionDown(MotionEvent event) {
        ly = event.getY();
        lx = event.getX();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            this.fixAll();
            this.fixRect();
            this.fixTexts();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.calcParams(list);
        this.drawBase(canvas);
    }

    private void doScaleList(MotionEvent event) {
        float dx = Math.abs(event.getX(0) - event.getX(1));
        if (this.dx == 0) {
            this.dx = dx;
            return;
        }
        float diff = this.dx - dx;
        if (diff == 0) {
            return;
        }
        this.dx = dx;
        if (Math.abs(diff) > 200) {
            return;
        }
        if (diff < 0) { // 缩小
            if (listSize <= sizeMin) {
                return;
            }
            int unit = this.moveUnit(-diff);
            if (unit == 0) {
                return;
            }
            listSize -= unit;
            if (listSize < sizeMin) {
                unit -= sizeMin - listSize;
                listSize = sizeMin;
            }
            listStart += unit / 2;
            listEnd -= unit - unit / 2;
        } else {
            int size = this.data.size();
            int sizeMax = Math.min(this.sizeMax, size);
            if (listSize >= sizeMax) {
                return;
            }
            int unit = this.moveUnit(diff);
            if (unit == 0) {
                return;
            }
            listSize += unit;
            if (listSize > sizeMax) {
                unit -= listSize - sizeMax;
                listSize = sizeMax;
            }
            listStart -= unit / 2;
            if (listStart < 0) {
                listEnd += unit - unit / 2 - listStart;
                listStart = 0;
            } else {
                listEnd += unit - unit / 2;
                if (listEnd > size) {
                    listStart -= listEnd - size;
                    listEnd = size;
                }
            }
        }
        list = data.subList(listStart, listEnd);
        index = list.size() - 1;
        this.curr = this.list.get(index);
        this.invalidate();
    }

    private void doMoveIndex(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        if (x < rect.sx - unitX / 2f || x > rect.ex + unitX / 2f || y < rect.sy || y > rect.ey) {
            return;
        }
        int index = Math.round((x - rect.sx) / unitX);
        if (this.index == index) {
            return;
        }
        this.index = index;
        this.curr = this.list.get(this.index);
        this.invalidate();
    }

    private void doMoveList(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        if (x < rect.sx - unitX / 2f || x > rect.ex + unitX / 2f || y < rect.sy || y > rect.ey) {
            return;
        }
        float diff = x - this.lx;
        if (diff == 0) {
            return;
        }
        this.lx = x;
        int size = this.data.size();
        if (diff < 0) {
            if (this.listEnd == size) {
                return;
            }
            int unit = this.moveUnit(-diff);
            if (unit == 0) {
                return;
            }
            this.listEnd += unit;
            if (this.listEnd > size) {
                unit -= (this.listEnd - size);
                this.listEnd = size;
            }
            this.listStart += unit;
        } else {
            if (this.listStart == 0) {
                return;
            }
            int unit = this.moveUnit(diff);
            if (unit == 0) {
                return;
            }
            this.listStart -= unit;
            if (this.listStart < 0) {
                unit += this.listStart;
                this.listStart = 0;
            }
            this.listEnd -= unit;
        }
        this.list = this.data.subList(this.listStart, this.listEnd);
        this.index = this.list.size() - 1;
        this.curr = this.list.get(this.index);
        this.invalidate();
    }

    private int moveUnit(float diff) {
        return (int) (this.listSize / (float) (this.rect.ex - this.rect.sx) * diff + 0.5f);
    }

    private void calcParams(List<T> data) {
        this.calcXParams(data);
        for (org.nature.common.chart.R<T> r : rs) {
            r.calculate(data);
        }
    }


    private void calcXParams(List<T> data) {
        List<String> dates = data.stream().map(this.xText).collect(Collectors.toList());
        this.xTexts = new ArrayList<>();
        int size = dates.size();
        int middle = size % 2 == 0 ? size / 2 : size / 2 + 1;
        if (middle > size - 1) middle = size - 1;
        this.xTexts.addAll(Arrays.asList(dates.get(0), dates.get(middle), dates.get(size - 1)));
        this.intervalX = (int) ((rect.ex - rect.sx) / (double) (xTexts.size() - 1) + 0.5d);
        this.unitX = (float) (rect.ex - rect.sx) / (data.size() - 1);
    }

    private void fixAll() {
        int width = this.getWidth();
        int height = this.getHeight();
        if ((float) width / (float) height > 1.8f) {   // 宽高比保持4：3
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

    private void fixRect() {
        rect.sx = (int) (all.sx / 20f * 18f + all.ex / 20f * 2f + 0.5f);
        rect.ex = (int) (all.sx / 20f + all.ex / 20f * 19f + 0.5f);
        rect.sy = (int) (all.sy / 20f * 17f + all.ey / 20f * 3f + 0.5f);
        rect.ey = (int) (all.sy / 20f + all.ey / 20f * 19f + 0.5f);
        double total = rs.stream().mapToDouble(d -> d.weight).sum();
        int unit = (int) ((rect.ey - rect.sy) / total + 0.5d);
        org.nature.common.chart.R<T> r = rs.get(0);
        int sy = rect.sy, ey = sy;
        r.fix(sy, ey);
        for (int i = 0; i < rs.size() - 1; i++) {
            r = rs.get(i);
            ey = ey + r.weight * unit;
            r.fix(sy, ey);
            sy = ey;
        }
        ey = rect.ey;
        rs.get(rs.size() - 1).fix(sy, ey);
    }

    private void fixTexts() {
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
        int dif = (ey - sy) / qs.size();
        for (int i = 0; i < qs.size(); i++) {
            ys[i] = sy + dif * i;
        }
        for (int i = 0; i < qs.size(); i++) {
            List<Q<T>> q = qs.get(i);
            for (int j = 0; j < q.size(); j++) {
                q.get(j).fix(sxs[j], exs[j], ys[i]);
            }
        }
    }

    private void drawBase(Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0f);
        // x轴线
        canvas.drawLine(rect.sx, rect.sy, rect.ex, rect.sy, paint);
        // x轴线
        canvas.drawLine(rect.sx, rect.ey, rect.ex, rect.ey, paint);
        for (int i = 1; i < rs.size(); i++) {
            org.nature.common.chart.R<T> r = rs.get(i);
            canvas.drawLine(rect.sx, r.sy, rect.ex, r.sy, paint);
        }
        // y轴线
        canvas.drawLine(rect.sx, rect.sy, rect.sx, rect.ey, paint);
        // y轴线
        canvas.drawLine(rect.ex, rect.sy, rect.ex, rect.ey, paint);
        // x轴刻度
        this.drawXIndex(canvas);
        for (org.nature.common.chart.R<T> r : rs) {
            // y轴刻度平线
            this.drawYIndexLine(canvas, r);
            this.drawYIndex(canvas, r);
        }
        this.drawXIndexLine(canvas);
        paint.setTextSize(25f);
        // 顶端指标数据
        for (List<Q<T>> arr : qs) {
            for (Q<T> q : arr) {
                this.doDrawText(canvas, q);
            }
        }
        paint.setColor(Color.DKGRAY);
        paint.setStyle(Paint.Style.STROKE);
        for (org.nature.common.chart.R<T> r : rs) {
            this.doDrawLine(canvas, r);
        }
    }

    private void drawXIndexLine(Canvas canvas) {
        paint.setColor(Color.DKGRAY);
        paint.setPathEffect(new DashPathEffect(new float[]{8, 10, 8, 10}, 0));
        float x = rect.sx + index * unitX;
        canvas.drawLine(x, rect.sy, x, rect.ey, paint);
        paint.setPathEffect(null);
    }

    private void drawYIndex(Canvas canvas, org.nature.common.chart.R<T> r) {
        paint.setColor(Color.BLACK);
        paint.setTextSize(20f);
        List<String> texts = r.texts;
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            float x = rect.sx - this.getTextWidth(paint, text) - 20;
            float y;
            if (i == texts.size() - 1) {
                y = r.ey - i * r.interval + this.getTextHeight(paint, text) + 3;
            } else if (i == 0) {
                y = r.ey - 3;
            } else {
                y = r.ey - i * r.interval + this.getTextHeight(paint, text) / 2f;
            }
            canvas.drawText(text, x, y, paint);
        }
    }

    private void drawXIndex(Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setTextSize(20f);
        // x轴刻度
        for (int i = 0; i < xTexts.size(); i++) {
            // x轴上的文字
            String text = xTexts.get(i);
            if (text == null) {
                continue;
            }
            float x = rect.sx + i * intervalX - this.getTextWidth(paint, text) / 2f;
            float y = rect.ey + this.getTextHeight(paint, text) / 2f * 3f + 15;
            canvas.drawText(text, x, y, paint);
        }
    }

    private void drawYIndexLine(Canvas canvas, org.nature.common.chart.R<T> r) {
        paint.setColor(Color.LTGRAY);
        paint.setPathEffect(new DashPathEffect(new float[]{8, 10, 8, 10}, 0));
        for (int i = 1; i < r.texts.size() - 1; i++) {
            int indexY = r.ey - i * r.interval;
            canvas.drawLine(rect.sx, indexY, rect.ex, indexY, paint);
        }
        paint.setPathEffect(null);
    }

    private void doDrawLine(Canvas canvas, org.nature.common.chart.R<T> r) {
        Double min = r.min;
        float unit = r.unit;
        int ey = r.ey;
        for (C<T> c : r.cs) {
            this.doDrawLine(canvas, min, unit, ey, c.color, c.func);
        }
    }

    private void doDrawLine(Canvas canvas, double min, float unit, int ey, int color, Function<T, Double> func) {
        // 折线
        Path path = new Path();
        boolean moved = false;
        for (int i = 0; i < list.size(); i++) {
            T k = list.get(i);
            float x = i * unitX + rect.sx;
            Double d = func.apply(k);
            if (d == null) {
                continue;
            }
            float y = (float) ((min - d) * unit + ey);
            if (!moved) {   // 先移动到第一个点的位置
                moved = true;
                path.moveTo(x, y);
            } else {    // 然后点与点连线
                path.lineTo(x, y);
            }
        }
        paint.setColor(color);
        canvas.drawPath(path, paint);
    }

    private void doDrawText(Canvas canvas, Q<T> q) {
        paint.setColor(Color.DKGRAY);
        String title = q.title;
        String content = q.content(curr);
        float y = q.y + this.getTextHeight(paint, title) / 2f;
        canvas.drawText(title, q.sx, y, paint);
        paint.setColor(q.color);
        canvas.drawText(content, q.ex - this.getTextWidth(paint, content), y, paint);
    }

    private int getTextWidth(Paint paint, String text) {
        return (int) paint.measureText(text);
    }

    private int getTextHeight(Paint paint, String text) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }

}
