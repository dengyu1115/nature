package org.nature.common.chart;

import android.graphics.*;
import org.nature.common.constant.Const;

import java.util.List;
import java.util.function.Function;

/**
 * 绘制
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/23
 */
public class Draw<T> {

    private final P<T> p;
    private final Paint paint;
    private final XY rect;

    public Draw(P<T> p, XY rect) {
        this.p = p;
        this.rect = rect;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    /**
     * 绘制
     * @param canvas canvas
     */
    public void start(Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0f);
        int sx = rect.sx - Const.PAD;
        int ex = rect.ex + Const.PAD;
        int sy = rect.sy;
        int ey = rect.ey;
        // x轴线
        canvas.drawLine(sx, sy, ex, sy, paint);
        // x轴线
        canvas.drawLine(sx, ey, ex, ey, paint);
        for (int i = 1; i < p.rs.size(); i++) {
            BR<T> r = p.rs.get(i);
            canvas.drawLine(sx, r.sy, ex, r.sy, paint);
        }
        // y轴线
        canvas.drawLine(sx, sy, sx, ey, paint);
        // y轴线
        canvas.drawLine(ex, sy, ex, ey, paint);
        // x轴刻度
        this.drawXIndex(canvas);
        for (BR<T> r : p.rs) {
            // y轴刻度平线
            this.drawYIndexLine(canvas, r);
            this.drawYIndex(canvas, r);
        }
        this.drawXIndexLine(canvas);
        paint.setTextSize(25f);
        // 顶端指标数据
        for (List<Q<T>> arr : p.qs) {
            for (Q<T> q : arr) {
                this.doDrawText(canvas, q);
            }
        }
        paint.setColor(Color.DKGRAY);
        paint.setStyle(Paint.Style.STROKE);
        for (BR<T> r : p.rs) {
            this.doDrawLine(canvas, r);
        }
    }

    /**
     * 绘制X轴指标刻度
     * @param canvas canvas
     */
    private void drawXIndexLine(Canvas canvas) {
        paint.setColor(Color.DKGRAY);
        paint.setPathEffect(new DashPathEffect(new float[]{8, 10, 8, 10}, 0));
        float x = rect.sx + p.index * p.unitX;
        canvas.drawLine(x, rect.sy, x, rect.ey, paint);
        paint.setPathEffect(null);
    }

    /**
     * 绘制Y轴指标
     * @param canvas canvas
     * @param r      矩形
     */
    private void drawYIndex(Canvas canvas, BR<T> r) {
        paint.setColor(Color.BLACK);
        paint.setTextSize(20f);
        List<String> texts = r.texts;
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            float x = rect.sx - Const.PAD - this.getTextWidth(paint, text) - 20;
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

    /**
     * 绘制Y轴指标
     * @param canvas canvas
     */
    private void drawXIndex(Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setTextSize(20f);
        // x轴刻度
        for (int i = 0; i < p.xTexts.size(); i++) {
            // x轴上的文字
            String text = p.xTexts.get(i);
            if (text == null) {
                continue;
            }
            float x = rect.sx + i * p.intervalX - this.getTextWidth(paint, text) / 2f;
            float y = rect.ey + this.getTextHeight(paint, text) / 2f * 3f + 15;
            canvas.drawText(text, x, y, paint);
        }
    }

    /**
     * 绘制Y轴指标线
     * @param canvas canvas
     * @param r      矩形
     */
    private void drawYIndexLine(Canvas canvas, BR<T> r) {
        paint.setColor(Color.LTGRAY);
        paint.setPathEffect(new DashPathEffect(new float[]{8, 10, 8, 10}, 0));
        for (int i = 1; i < r.texts.size() - 1; i++) {
            int indexY = r.ey - i * r.interval;
            canvas.drawLine(rect.sx - Const.PAD, indexY, rect.ex + Const.PAD, indexY, paint);
        }
        paint.setPathEffect(null);
    }

    /**
     * 绘制矩形内容
     * @param canvas canvas
     * @param r      矩形
     */
    private void doDrawLine(Canvas canvas, BR<T> r) {
        Double min = r.min;
        float unit = r.unit;
        int ey = r.ey;
        if (r instanceof LR) {
            // 普通折线绘制
            for (C<T> c : ((LR<T>) r).cs) {
                this.doDrawLine(canvas, min, unit, ey, c.color, c.func);
            }
        } else if (r instanceof KR) {
            // K线绘制
            this.doDrawKline(canvas, min, unit, ey, ((KR<T>) r));
        }

    }

    /**
     * 绘制折线
     * @param canvas canvas
     * @param min    最小值
     * @param unit   单位
     * @param ey     y终止
     * @param color  颜色
     * @param func   指标值获取函数
     */
    private void doDrawLine(Canvas canvas, double min, float unit, int ey, int color, Function<T, Double> func) {
        // 折线
        paint.setStyle(Paint.Style.STROKE);
        Path path = new Path();
        boolean moved = false;
        for (int i = 0; i < p.list.size(); i++) {
            T t = p.list.get(i);
            if (t == null) {
                continue;
            }
            float x = i * p.unitX + rect.sx;
            Double d = func.apply(t);
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

    /**
     * 绘制K线
     * @param canvas canvas
     * @param min    最小刻度
     * @param unit   单位
     * @param ey     y终止
     * @param r      数据
     */
    private void doDrawKline(Canvas canvas, double min, float unit, int ey, KR<T> r) {
        // 折线
        float unitX = p.unitX;
        if (unitX < 10) {
            // 数据太密集蜡烛图展示不清楚直接展示折线
            this.doDrawLine(canvas, min, unit, ey, Color.BLUE, r.latest);
            return;
        }
        float w = (unitX - 4) / 2;
        if (w > Const.PAD) {
            w = Const.PAD;
        }
        for (int i = 0; i < p.list.size(); i++) {
            T t = p.list.get(i);
            if (t == null) {
                continue;
            }
            float x = i * unitX + rect.sx;
            Double latest = r.latest.apply(t);
            Double open = r.open.apply(t);
            Double high = r.high.apply(t);
            Double low = r.low.apply(t);
            if (latest == null || open == null || high == null || low == null) {
                continue;
            }
            float y1 = (float) ((min - high) * unit + ey);
            float y2 = (float) ((min - low) * unit + ey);
            float y3 = (float) ((min - open) * unit + ey);
            float y4 = (float) ((min - latest) * unit + ey);
            float up = Math.min(y3, y4);
            float down = Math.max(y3, y4);
            paint.setColor(y3 <= y4 ? Color.RED : Color.GREEN);
            // 绘制开盘、收盘圆柱
            paint.setStyle(y3 <= y4 ? Paint.Style.STROKE : Paint.Style.FILL);
            canvas.drawRect(x - w, up, x + w, down, paint);
            // 绘制最高价、最低价线段
            canvas.drawLine(x, y1, x, up, paint);
            canvas.drawLine(x, down, x, y2, paint);
            // 绘制均线
            for (C<T> c : r.cs) {
                this.doDrawLine(canvas, min, unit, ey, c.color, c.func);
            }
        }
    }

    /**
     * 绘制文本
     * @param canvas canvas
     * @param q      指标对象
     */
    private void doDrawText(Canvas canvas, Q<T> q) {
        paint.setColor(Color.DKGRAY);
        String title = q.title;
        String content = q.content(p.curr);
        float y = q.y + this.getTextHeight(paint, title) / 2f;
        // 绘制标题，留15间距
        canvas.drawText(title, q.sx + Const.PAD, y, paint);
        paint.setColor(q.color);
        // 绘制值，留15间距
        canvas.drawText(content, q.ex - this.getTextWidth(paint, content) - Const.PAD, y, paint);
    }

    /**
     * 获取文本的宽度
     * @param paint 画笔
     * @param text  文本
     * @return int
     */
    private int getTextWidth(Paint paint, String text) {
        return (int) paint.measureText(text);
    }

    /**
     * 获取文本的高度
     * @param paint 画笔
     * @param text  文本
     * @return int
     */
    private int getTextHeight(Paint paint, String text) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect.height();
    }
}
