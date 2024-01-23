package org.nature.common.chart;

import android.view.MotionEvent;
import android.view.View;

/**
 * 动作处理
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/23
 */
public class Action<T> {

    /**
     * 参数
     */
    private final P<T> p;

    private final View view;

    private final XY rect;

    public Action(P<T> p, View view, XY rect) {
        this.p = p;
        this.view = view;
        this.rect = rect;
    }

    /**
     * 移动操作
     * @param event 事件对象
     */
    public void move(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            // 双指操作放大缩小
            this.doScaleList(event);
        } else {
            if (!p.moving) {
                float mx = Math.abs(event.getX() - p.lx);
                float my = Math.abs(event.getY() - p.ly);
                if (mx < 10 && my < 10) {
                    p.longPressed = (event.getEventTime() - event.getDownTime()) > 500L;
                } else {
                    p.moving = true;
                    p.ly = event.getY();
                    p.lx = event.getX();
                }
            }
            if (p.longPressed) {
                // 长按移动下标
                this.doMoveIndex(event);
            } else if (p.moving) {
                // 移动列表
                this.doMoveList(event);
            }
        }
    }

    /**
     * 抬起操作
     * @param event 事件
     */
    public void up(MotionEvent event) {
        p.longPressed = false;
        p.moving = false;
        p.dx = 0;
    }

    /**
     * 按下操作
     * @param event 事件
     */
    public void down(MotionEvent event) {
        p.ly = event.getY();
        p.lx = event.getX();
    }

    /**
     * 放大缩小操作
     * @param event 事件
     */
    private void doScaleList(MotionEvent event) {
        float dx = Math.abs(event.getX(0) - event.getX(1));
        if (p.dx == 0) {
            p.dx = dx;
            return;
        }
        float diff = p.dx - dx;
        if (diff == 0) {
            return;
        }
        p.dx = dx;
        if (Math.abs(diff) > 200) {
            return;
        }
        if (diff < 0) { // 缩小
            if (p.listSize <= p.sizeMin) {
                return;
            }
            int unit = this.moveUnit(-diff);
            if (unit == 0) {
                return;
            }
            p.listSize -= unit;
            if (p.listSize < p.sizeMin) {
                unit -= p.sizeMin - p.listSize;
                p.listSize = p.sizeMin;
            }
            p.listStart += unit / 2;
            p.listEnd -= unit - unit / 2;
        } else {
            int size = p.data.size();
            int sizeMax = Math.min(p.sizeMax, size);
            if (p.listSize >= sizeMax) {
                return;
            }
            int unit = this.moveUnit(diff);
            if (unit == 0) {
                return;
            }
            p.listSize += unit;
            if (p.listSize > sizeMax) {
                unit -= p.listSize - sizeMax;
                p.listSize = sizeMax;
            }
            p.listStart -= unit / 2;
            if (p.listStart < 0) {
                p.listEnd += unit - unit / 2 - p.listStart;
                p.listStart = 0;
            } else {
                p.listEnd += unit - unit / 2;
                if (p.listEnd > size) {
                    p.listStart -= p.listEnd - size;
                    p.listEnd = size;
                }
            }
        }
        p.list = p.data.subList(p.listStart, p.listEnd);
        p.index = p.list.size() - 1;
        p.curr = p.list.get(p.index);
        view.invalidate();
    }

    /**
     * 移动下标操作
     * @param event 事件
     */
    private void doMoveIndex(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        if (x < rect.sx - p.unitX / 2f || x > rect.ex + p.unitX / 2f || y < rect.sy || y > rect.ey) {
            return;
        }
        int index = Math.round((x - rect.sx) / p.unitX);
        if (p.index == index) {
            return;
        }
        p.index = index;
        p.curr = p.list.get(p.index);
        view.invalidate();
    }

    /**
     * 数据移动操作
     * @param event 事件
     */
    private void doMoveList(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        if (x < rect.sx - p.unitX / 2f || x > rect.ex + p.unitX / 2f || y < rect.sy || y > rect.ey) {
            return;
        }
        float diff = x - p.lx;
        if (diff == 0) {
            return;
        }
        p.lx = x;
        int size = p.data.size();
        if (diff < 0) {
            if (p.listEnd == size) {
                return;
            }
            int unit = this.moveUnit(-diff);
            if (unit == 0) {
                return;
            }
            p.listEnd += unit;
            if (p.listEnd > size) {
                unit -= (p.listEnd - size);
                p.listEnd = size;
            }
            p.listStart += unit;
        } else {
            if (p.listStart == 0) {
                return;
            }
            int unit = this.moveUnit(diff);
            if (unit == 0) {
                return;
            }
            p.listStart -= unit;
            if (p.listStart < 0) {
                unit += p.listStart;
                p.listStart = 0;
            }
            p.listEnd -= unit;
        }
        p.list = p.data.subList(p.listStart, p.listEnd);
        p.index = p.list.size() - 1;
        p.curr = p.list.get(p.index);
        view.invalidate();
    }

    /**
     * 移动单位大小计算
     * @param diff 差值
     * @return int
     */
    private int moveUnit(float diff) {
        return (int) (p.listSize / (float) (this.rect.ex - this.rect.sx) * diff + 0.5f);
    }

}
