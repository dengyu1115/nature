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
    private final Param<T> param;
    /**
     * view
     */
    private final View view;
    /**
     * 矩形框
     */
    private final XyIndex rect;

    public Action(Param<T> param, View view, XyIndex rect) {
        this.param = param;
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
            if (!param.moving) {
                float mx = Math.abs(event.getX() - param.lx);
                float my = Math.abs(event.getY() - param.ly);
                if (mx < 10 && my < 10) {
                    param.longPressed = (event.getEventTime() - event.getDownTime()) > 500L;
                } else {
                    param.moving = true;
                    param.ly = event.getY();
                    param.lx = event.getX();
                }
            }
            if (param.longPressed) {
                // 长按移动下标
                this.doMoveIndex(event);
            } else if (param.moving) {
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
        param.longPressed = false;
        param.moving = false;
        param.dx = 0;
    }

    /**
     * 按下操作
     * @param event 事件
     */
    public void down(MotionEvent event) {
        param.ly = event.getY();
        param.lx = event.getX();
    }

    /**
     * 放大缩小操作
     * @param event 事件
     */
    private void doScaleList(MotionEvent event) {
        float dx = Math.abs(event.getX(0) - event.getX(1));
        if (param.dx == 0) {
            param.dx = dx;
            return;
        }
        float diff = param.dx - dx;
        if (diff == 0) {
            return;
        }
        param.dx = dx;
        if (Math.abs(diff) > 200) {
            return;
        }
        if (diff < 0) { // 缩小
            if (param.listSize <= param.sizeMin) {
                return;
            }
            int moveSize = this.moveSize(-diff);
            if (moveSize == 0) {
                return;
            }
            param.listSize -= moveSize;
            if (param.listSize < param.sizeMin) {
                moveSize -= param.sizeMin - param.listSize;
                param.listSize = param.sizeMin;
            }
            param.listStart += moveSize / 2;
            param.listEnd -= moveSize - moveSize / 2;
        } else {
            int size = param.data.size();
            int sizeMax = Math.min(param.sizeMax, size);
            if (param.listSize >= sizeMax) {
                return;
            }
            int moveSize = this.moveSize(diff);
            if (moveSize == 0) {
                return;
            }
            param.listSize += moveSize;
            if (param.listSize > sizeMax) {
                moveSize -= param.listSize - sizeMax;
                param.listSize = sizeMax;
            }
            param.listStart -= moveSize / 2;
            if (param.listStart < 0) {
                param.listEnd += moveSize - moveSize / 2 - param.listStart;
                param.listStart = 0;
            } else {
                param.listEnd += moveSize - moveSize / 2;
                if (param.listEnd > size) {
                    param.listStart -= param.listEnd - size;
                    param.listEnd = size;
                }
            }
        }
        param.list = param.data.subList(param.listStart, param.listEnd);
        param.index = param.list.size() - 1;
        param.curr = param.list.get(param.index);
        view.invalidate();
    }

    /**
     * 移动下标操作
     * @param event 事件
     */
    private void doMoveIndex(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        //  判断是否在有效区域内
        if (x < rect.sx - param.unitX / 2f || x > rect.ex + param.unitX / 2f || y < rect.sy || y > rect.ey) {
            return;
        }
        //  计算下标
        int index = Math.round((x - rect.sx) / param.unitX);
        // 判断位置是否发生变化
        if (param.index == index) {
            return;
        }
        // 当前下标位置设置
        param.index = index;
        param.curr = param.list.get(param.index);
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
        if (x < rect.sx - param.unitX / 2f || x > rect.ex + param.unitX / 2f || y < rect.sy || y > rect.ey) {
            return;
        }
        float diff = x - param.lx;
        // 没有移动
        if (diff == 0) {
            return;
        }
        int size = param.data.size();
        if (diff < 0) {
            // 已经移动至末端
            if (param.listEnd == size) {
                return;
            }
            // 计算移动量
            int moveSize = this.moveSize(-diff);
            // 没有移动
            if (moveSize == 0) {
                return;
            }
            param.listEnd += moveSize;
            // 移动后超出范围处理
            if (param.listEnd > size) {
                moveSize -= (param.listEnd - size);
                param.listEnd = size;
            }
            param.listStart += moveSize;
        } else {
            // 已经移动至头部
            if (param.listStart == 0) {
                return;
            }
            // 计算移动量
            int moveSize = this.moveSize(diff);
            // 没有移动
            if (moveSize == 0) {
                return;
            }
            param.listStart -= moveSize;
            // 移动量超出部分重置为有效区域
            if (param.listStart < 0) {
                moveSize += param.listStart;
                param.listStart = 0;
            }
            param.listEnd -= moveSize;
        }
        // 上一个位置设置
        param.lx = x;
        // 展示数据设置
        param.list = param.data.subList(param.listStart, param.listEnd);
        param.index = param.list.size() - 1;
        param.curr = param.list.get(param.index);
        // 操作绘制view
        view.invalidate();
    }

    /**
     * 移动大小计算
     * @param diff 差值
     * @return int
     */
    private int moveSize(float diff) {
        int moveSize = (int) ((float) param.listSize / (float) (this.rect.ex - this.rect.sx) * diff + 0.5f);
        if (moveSize == 0 && diff > 20) {
            return 1;
        }
        return moveSize;
    }

}
