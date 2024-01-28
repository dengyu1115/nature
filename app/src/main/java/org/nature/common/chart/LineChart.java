package org.nature.common.chart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import org.nature.common.exception.Warn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 折线图
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/22
 */
@SuppressLint("ClickableViewAccessibility")
public class LineChart<T> extends View {

    /**
     * 参数对象
     */
    private final P<T> p;

    private final Action<T> action;

    private final Calc<T> calc;

    private final Draw<T> draw;

    public LineChart(Context context) {
        super(context);
        XY rect = new XY();
        this.p = new P<>();
        this.action = new Action<>(p, this, rect);
        this.calc = new Calc<>(p, this, rect);
        this.draw = new Draw<>(p, rect);
    }

    /**
     * 修改数据量默认值
     * @param sizeDefault 默认展示量
     * @param sizeMin     最小展示量
     * @param sizeMax     最大展示量
     */
    public void sizeDefault(int sizeDefault, int sizeMin, int sizeMax) {
        p.sizeDefault = sizeDefault;
        p.listSize = sizeDefault;
        p.sizeMin = sizeMin;
        p.sizeMax = sizeMax;
    }

    /**
     * 初始化配置
     * @param qs    指标集合
     * @param rs    图形集合
     * @param xText X轴文案获取函数
     */
    public void init(List<List<Q<T>>> qs, List<BR<T>> rs, Function<T, String> xText) {
        p.qs = qs;
        p.rs = rs;
        p.xText = xText;
    }

    /**
     * 设置数据
     * @param data 数据集合
     */
    public void data(List<T> data) {
        if (data == null) {
            throw new Warn("data is null");
        }
        p.data = new ArrayList<>(data);
        int size = data.size();
        if (p.listSize < size) {
            // 数量超出的截取尾部展示
            p.list = data.subList(p.listStart = size - p.listSize, p.listEnd = size);
        } else if (size >= p.sizeMin) {
            // 数量超过最小size全部展示
            p.list = data;
            p.listStart = 0;
            p.listEnd = size;
        } else {
            // 数据量不足，补全后展示
            p.list = new ArrayList<>();
            for (int i = 0; i < p.sizeMin - size; i++) {
                p.list.add(null);
            }
            p.list.addAll(data);
            p.data = p.list;
            p.listStart = 0;
            p.listEnd = p.list.size();
        }
        p.listSize = p.list.size();
        p.index = p.list.size() - 1;
        p.curr = p.list.get(p.index);
        this.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            // 按下操作
            this.action.down(event);
        } else if (action == MotionEvent.ACTION_UP) {
            // 抬起操作
            this.action.up(event);
        } else if (action == MotionEvent.ACTION_MOVE) {
            // 移动操作
            this.action.move(event);
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            calc.allIndex();
            calc.rectIndex();
            calc.quotaIndex();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        calc.params(p.list);
        draw.start(canvas);
    }

}
