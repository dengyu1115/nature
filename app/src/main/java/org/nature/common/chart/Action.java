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
    /**
     * view
     */
    private final View view;
    /**
     * 矩形框
     */
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
            int moveSize = this.moveSize(-diff);
            if (moveSize == 0) {
                return;
            }
            p.listSize -= moveSize;
            if (p.listSize < p.sizeMin) {
                moveSize -= p.sizeMin - p.listSize;
                p.listSize = p.sizeMin;
            }
            p.listStart += moveSize / 2;
            p.listEnd -= moveSize - moveSize / 2;
        } else {
            int size = p.data.size();
            int sizeMax = Math.min(p.sizeMax, size);
            if (p.listSize >= sizeMax) {
                return;
            }
            int moveSize = this.moveSize(diff);
            if (moveSize == 0) {
                return;
            }
            p.listSize += moveSize;
            if (p.listSize > sizeMax) {
                moveSize -= p.listSize - sizeMax;
                p.listSize = sizeMax;
            }
            p.listStart -= moveSize / 2;
            if (p.listStart < 0) {
                p.listEnd += moveSize - moveSize / 2 - p.listStart;
                p.listStart = 0;
            } else {
                p.listEnd += moveSize - moveSize / 2;
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
        //  判断是否在有效区域内
        if (x < rect.sx - p.unitX / 2f || x > rect.ex + p.unitX / 2f || y < rect.sy || y > rect.ey) {
            return;
        }
        //  计算下标
        int index = Math.round((x - rect.sx) / p.unitX);
        // 判断位置是否发生变化
        if (p.index == index) {
            return;
        }
        // 当前下标位置设置
        p.index = index;
        p.curr = p.list.get(p.index);
        // 操作绘制view
        view.invalidate();
    }

    /**
     * 数据移动操作
     * @param event 事件
     */
    private void doMoveList(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        // 判断是否在有效区域内
        if (x < rect.sx - p.unitX / 2f || x > rect.ex + p.unitX / 2f || y < rect.sy || y > rect.ey) {
            return;
        }
        float diff = x - p.lx;
        // 没有移动
        if (diff == 0) {
            return;
        }
        int size = p.data.size();
        if (diff < 0) {
            // 已经移动至末端
            if (p.listEnd == size) {
                return;
            }
            // 计算移动量
            int moveSize = this.moveSize(-diff);
            // 没有移动
            if (moveSize == 0) {
                return;
            }
            p.listEnd += moveSize;
            // 移动后超出范围处理
            if (p.listEnd > size) {
                moveSize -= (p.listEnd - size);
                p.listEnd = size;
            }
            p.listStart += moveSize;
        } else {
            // 已经移动至头部
            if (p.listStart == 0) {
                return;
            }
            // 计算移动量
            int moveSize = this.moveSize(diff);
            // 没有移动
            if (moveSize == 0) {
                return;
            }
            p.listStart -= moveSize;
            // 移动量超出部分重置为有效区域
            if (p.listStart < 0) {
                moveSize += p.listStart;
                p.listStart = 0;
            }
            p.listEnd -= moveSize;
        }
        // 上一个位置设置
        p.lx = x;
        // 展示数据设置
        p.list = p.data.subList(p.listStart, p.listEnd);
        p.index = p.list.size() - 1;
        p.curr = p.list.get(p.index);
        // 操作绘制view
        view.invalidate();
    }

    /**
     * 移动大小计算
     * @param diff 差值
     * @return int
     */
    private int moveSize(float diff) {
        int moveSize = (int) ((float) p.listSize / (float) (this.rect.ex - this.rect.sx) * diff + 0.5f);
        if (moveSize == 0 && diff > 20) {
            return 1;
        }
        return moveSize;
    }

}
