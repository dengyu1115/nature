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
    private final Param<T> param;

    private final Action<T> action;

    private final Calc<T> calc;

    private final Draw<T> draw;

    public LineChart(Context context) {
        super(context);
        XyIndex rect = new XyIndex();
        this.param = new Param<>();
        this.action = new Action<>(param, this, rect);
        this.calc = new Calc<>(param, this, rect);
        this.draw = new Draw<>(param, rect);
    }

    /**
     * 修改数据量默认值
     * @param sizeDefault 默认展示量
     * @param sizeMin     最小展示量
     * @param sizeMax     最大展示量
     */
    public void sizeDefault(int sizeDefault, int sizeMin, int sizeMax) {
        param.sizeDefault = sizeDefault;
        param.listSize = sizeDefault;
        param.sizeMin = sizeMin;
        param.sizeMax = sizeMax;
    }

    /**
     * 初始化配置
     * @param qs    指标集合
     * @param rs    图形集合
     * @param xText X轴文案获取函数
     */
    public void init(List<List<Quota<T>>> qs, List<BaseRect<T>> rs, Function<T, String> xText) {
        param.qs = qs;
        param.rs = rs;
        param.xText = xText;
    }

    /**
     * 设置数据
     * @param data 数据集合
     */
    public void data(List<T> data) {
        if (data == null) {
            throw new Warn("data is null");
        }
        param.data = new ArrayList<>(data);
        int size = data.size();
        if (param.listSize < size) {
            // 数量超出的截取尾部展示
            param.list = data.subList(param.listStart = size - param.listSize, param.listEnd = size);
        } else if (size >= param.sizeMin) {
            // 数量超过最小size全部展示
            param.list = data;
            param.listStart = 0;
            param.listEnd = size;
        } else {
            // 数据量不足，补全后展示
            param.list = new ArrayList<>();
            for (int i = 0; i < param.sizeMin - size; i++) {
                param.list.add(null);
            }
            param.list.addAll(data);
            param.data = param.list;
            param.listStart = 0;
            param.listEnd = param.list.size();
        }
        param.listSize = param.list.size();
        param.index = param.list.size() - 1;
        param.curr = param.list.get(param.index);
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
        calc.params(param.list);
        draw.start(canvas);
    }

}
