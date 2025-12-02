import ChartBase from "./ChartBase.js";

export default class LineChart extends ChartBase {
  constructor(canvas, config) {
    super(canvas, config);
    this.config = config;
    this.hoverData = null;
    this.mouseX = 0;
    this.mouseY = 0;
  }

  // 初始化图表
  init() {
    this.calcPositions();
    this.draw();
    this.setupEventListeners();
  }

  calcPositions() {
    const { width, height } = this.canvas;
    // 初始化 横纵 起止值
    let xs = (this.config.paddingLeft || 0) * this.dpr;
    let xe = (this.config.paddingRight || 0) * this.dpr;
    let ys = (this.config.paddingTop || 0) * this.dpr;
    let ye = (this.config.paddingBottom || 0) * this.dpr;
    if (this.config.title) {
      ys += 50 * this.dpr;
    }
    // 图例高度
    ys += 20 * this.dpr;
    // X轴高度
    ye += 40 * this.dpr;
    // Y轴宽度
    Object.entries(this.config.yAxis).forEach(([key, arr]) => {
      arr
        .filter((axis) => axis.visible)
        .forEach((axis, index) => {
          if (key === "left") {
            xs += (axis.width || 60) * this.dpr;
          } else {
            xe += (axis.width || 60) * this.dpr;
          }
        });
    });
    this.chartArea = {
      x: xs,
      y: ys,
      width: width - xe - xs,
      height: height - ye - ys,
    };
  }

  // 主绘制函数
  draw() {
    const dataLength = Math.max(this.config.data?.length, 0);
    this.dataRangeIndices = this.calcDataRangeIndices(dataLength);
    this.ctx.font = 12 * this.dpr + "px Arial";
    // 清空画布
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    // 为每个Y轴计算可见数据范围
    const yAxisRanges = Object.fromEntries(
      Object.entries(this.config.yAxis).flatMap(([key, arr]) =>
        arr
          .filter((i) => i.visible)
          .map((i, idx) => [
            key + "_" + idx,
            this.calcAxisDataRangeIndices(key, idx),
          ])
      )
    );
    // 绘制各种图表元素
    this.drawGridlines();
    this.drawAxes();
    this.drawAxisLabels(yAxisRanges);

    // 设置裁剪区域，确保图形不超出坐标轴范围
    const { x, y, width, height } = this.chartArea;
    super.setClipArea(x, y, width, height);

    // 绘制数据系列，参考KlineChart的处理方式
    this.config.series.forEach((series, index) => {
      if (!super.isLegendItemVisible(index)) {
        return;
      }
      const axisKey = `${series.yAxis}_${series.yAxisIndex || 0}`;
      const ranges = yAxisRanges[axisKey];
      if (!ranges) {
        return;
      }
      if (series.type === "bar") {
        this.drawBarChart(series, ranges);
      } else if (series.type === "line") {
        this.drawLineChart(series, ranges);
      } else if (series.type === "area") {
        this.drawAreaChart(series, ranges);
      }
    });

    // 恢复裁剪区域
    super.restoreClipArea();

    // 绘制图例和标题
    this.drawLegend();
    this.drawTitle();
    // 绘制hover数据提示
    if (this.hoverData) {
      this.drawHoverData(
        this.collectHoverDataPoints.bind(this),
        this.config.labels || []
      );
    }
  }

  // 获取指定轴的可见数据范围
  calcAxisDataRangeIndices(side, index) {
    // 获取绑定到该轴的所有可见数据系列
    const seriesList = this.config.series.filter(
      (series) =>
        series.yAxis === side &&
        (series.yAxisIndex || 0) === index &&
        series.visible
    );
    if (seriesList.length === 0) {
      return { min: 0, max: 1 };
    }
    const { start, end } = this.dataRangeIndices;
    let min = Infinity;
    let max = -Infinity;
    const data = this.config.data || [];
    // 在可见范围内查找最小值和最大值
    for (let i = start; i <= end; i++) {
      const datum = data[i];
      if (datum === undefined || datum === null) continue;
      seriesList.forEach((series) => {
        const value = datum[series.prop];
        if (value !== undefined) {
          min = Math.min(min, value);
          max = Math.max(max, value);
        }
      });
    }
    // 添加一些边距使图表更美观
    const padding = (max - min) * 0.1;
    return {
      min: min - padding,
      max: max + padding,
    };
  }

  // 绘制网格线
  drawGridlines() {
    const count = 5;
    const { x, y, width, height } = this.chartArea;

    // 水平网格线
    super.drawHorizontalGridlines(y, height, count);

    // 垂直网格线
    super.drawVerticalGridlines(
      (i) => this.calcDatumIndex(i),
      () => ({
        start: this.chartArea.y,
        end: this.chartArea.y + this.chartArea.height,
      })
    );
  }

  // 绘制坐标轴
  drawAxes() {
    const { x, y, width, height } = this.chartArea;

    // X轴
    super.drawAxisLine(
      x - 25 * this.dpr,
      y + height,
      x + width + 25 * this.dpr,
      y + height
    );

    // Y轴 - 根据配置绘制
    Object.entries(this.config.yAxis).forEach(([key, arr]) => {
      arr
        .filter((axis) => axis.visible)
        .forEach((axis, index) => {
          const aw = axis.width || 60;
          const xi =
            x +
            index * aw +
            (key === "left" ? -25 * this.dpr : width + 25 * this.dpr);
          super.drawAxisLine(xi, y, xi, y + height);
        });
    });
  }

  // 绘制坐标轴标签
  drawAxisLabels(yAxisRanges) {
    const { labels, scale } = this.config;
    const { x, y, width, height } = this.chartArea;
    // X轴标签
    const { start, end } = this.dataRangeIndices;
    for (let i = start; i <= end; i++) {
      // 只绘制每隔一定间隔的标签以避免过于密集
      if (i % Math.max(1, Math.floor(5 / scale)) === 0) {
        const xi = this.calcDatumIndex(i);
        const yi = y + height + 20 * this.dpr;
        super.drawAxisLabel(xi, yi, labels[i] || `数据${i + 1}`);
      }
    }
    // Y轴标签 - 根据配置绘制
    Object.entries(this.config.yAxis).forEach(([key, arr]) => {
      arr
        .filter((axis) => axis.visible)
        .forEach((axis, index) => {
          const axisKey = key + "_" + index;
          const { max, min } = yAxisRanges[axisKey];
          const step = (max - min) / 5;
          const unit = axis.unit ? axis.unit : "";
          const aw = axis.width || 60;
          for (let i = 0; i <= 5; i++) {
            const text = (min + i * step).toFixed(1) + unit;
            const xi =
              x +
              index * aw +
              (key === "left" ? -30 * this.dpr : width + 30 * this.dpr);
            const yi = y + height - (i * height) / 5 + 4 * this.dpr;
            super.drawAxisLabel(
              xi,
              yi,
              text,
              key === "left" ? "right" : "left"
            );
          }
        });
    });
  }

  // 绘制柱状图
  drawBarChart(series, yAxisRange) {
    const { spacing, scale } = this.config;
    const data = this.config.data || [];
    const { min, max } = yAxisRange;
    const { start, end } = this.dataRangeIndices;
    const { y, height } = this.chartArea;
    const wi = Math.max(
      5 * this.dpr,
      Math.min(50 * this.dpr, spacing * scale * 0.6)
    );
    this.ctx.fillStyle = series.color;
    for (let i = start; i <= end; i++) {
      const datum = data[i];
      if (datum === undefined || datum === null) continue;
      const value = datum[series.prop];
      if (value === undefined) continue;
      const xi = this.calcDatumIndex(i) - wi / 2;
      const hi = ((value - min) / (max - min)) * height;
      const yi = y + height - hi;
      this.ctx.fillRect(xi, yi, wi, hi);
    }
  }

  // 绘制折线图
  drawLineChart(series, yAxisRange) {
    const data = this.config.data || [];
    const { min, max } = yAxisRange;
    const { start, end } = this.dataRangeIndices;
    const { y, height } = this.chartArea;
    // 绘制线条（仅绘制可视区域内的部分）
    this.ctx.beginPath();
    this.ctx.strokeStyle = series.color;
    this.ctx.lineWidth = 1 * this.dpr;
    let firstPoint = true;
    for (let i = start; i <= end; i++) {
      const datum = data[i];
      if (datum === undefined || datum === null) continue;
      const value = datum[series.prop];
      if (value === undefined) continue;
      const xi = this.calcDatumIndex(i);
      const yi = y + height - ((value - min) / (max - min)) * height;
      if (firstPoint) {
        this.ctx.moveTo(xi, yi);
        firstPoint = false;
      } else {
        this.ctx.lineTo(xi, yi);
      }
    }
    this.ctx.stroke();
  }

  // 绘制面积图
  drawAreaChart(series, yAxisRange) {
    const data = this.config.data || [];
    const { min, max } = yAxisRange;
    const { start, end } = this.dataRangeIndices;
    const { y, height } = this.chartArea;
    // 绘制填充区域
    this.ctx.beginPath();
    this.ctx.fillStyle = series.color + "80"; // 添加透明度
    let firstPoint = true;
    for (let i = start; i <= end; i++) {
      const datum = data[i];
      if (datum === undefined || datum === null) continue;
      const value = datum[series.prop];
      if (value === undefined) continue;
      const xi = this.calcDatumIndex(i);
      const yi = y + height - ((value - min) / (max - min)) * height;
      if (firstPoint) {
        this.ctx.moveTo(xi, y + height); // 从底部开始
        this.ctx.lineTo(xi, yi);
        firstPoint = false;
      } else {
        this.ctx.lineTo(xi, yi);
      }
    }
    // 封闭区域
    if (!firstPoint) {
      const lastX = this.calcDatumIndex(end);
      this.ctx.lineTo(lastX, y + height);
      this.ctx.closePath();
      this.ctx.fill();
    }
    this.drawLineChart(series, yAxisRange);
  }

  // 绘制标题
  drawTitle() {
    const { title } = this.config;
    super.drawTitle(
      title || "",
      this.canvas.width / 2,
      (this.config.paddingTop || 0) * this.dpr + 20 * this.dpr
    );
  }

  // 获取图例项数据
  getLegendItems() {
    const legendX = (this.config.paddingLeft || 0) * this.dpr;
    const legendY = (this.config.title ? 50 : 10) * this.dpr;
    const legendItems = [];
    this.config.series?.forEach((series, index) => {
      legendItems.push({
        key: index,
        name: series.name,
        x: legendX + index * 80 * this.dpr,
        y: legendY,
        visible: super.isLegendItemVisible(index),
        series: series,
        drawIcon: (itemX, itemY) => {
          if (series.type === "bar") {
            // 图例颜色框
            if (super.isLegendItemVisible(index)) {
              this.drawLegendColorBox(
                itemX,
                itemY - 6 * this.dpr,
                10 * this.dpr,
                10 * this.dpr,
                series.color
              );
            } else {
              // 隐藏状态：绘制空心矩形
              this.drawLegendBorder(
                itemX,
                itemY - 6 * this.dpr,
                10 * this.dpr,
                10 * this.dpr
              );
            }
          } else if (series.type === "line") {
            // 图例颜色线
            this.drawLegendLine(
              itemX,
              itemY,
              itemX + 10 * this.dpr,
              itemY,
              super.isLegendItemVisible(index) ? series.color : "#999"
            );
            // 图例点
            if (super.isLegendItemVisible(index)) {
              this.drawLegendCircle(
                itemX + 5 * this.dpr,
                itemY,
                2 * this.dpr,
                series.color
              );
            } else {
              this.drawLegendCircle(
                itemX + 5 * this.dpr,
                itemY,
                2 * this.dpr,
                null,
                "#999"
              );
            }
          } else if (series.type === "area") {
            // 面积图图例
            if (super.isLegendItemVisible(index)) {
              this.drawLegendColorBox(
                itemX,
                itemY - 6 * this.dpr,
                10 * this.dpr,
                10 * this.dpr,
                series.color + "80"
              );
              this.drawLegendBorder(
                itemX,
                itemY - 6 * this.dpr,
                10 * this.dpr,
                10 * this.dpr,
                series.color
              );
            } else {
              // 隐藏状态：绘制空心矩形
              this.drawLegendBorder(
                itemX,
                itemY - 6 * this.dpr,
                10 * this.dpr,
                10 * this.dpr
              );
            }
          }
        },
      });
    });
    return legendItems;
  }

  // 收集hover数据点
  collectHoverDataPoints(index) {
    // 收集所有需要显示的数据点
    const points = [];
    const data = this.config.data || [];
    const datum = data[index];
    if (!datum) return points;
    this.config.series.forEach((series, seriesIndex) => {
      if (!super.isLegendItemVisible(seriesIndex)) return;
      const value = datum[series.prop];
      if (value === undefined) return;
      points.push({
        seriesName: series.name,
        value: value,
        color: series.color,
      });
    });
    return points;
  }
}
