import ChartBase from "./ChartBase.js";

export default class KlineChart extends ChartBase {
  constructor(canvas, config) {
    super(canvas, config);
    this.config = {
      spacing: 20,
      scale: 1,
      transX: 0,
      ...config,
      colors: {
        rising: "#ff0000",
        falling: "#00ff00",
        share: "#999999",
        amount: "#0000ff",
        axis: "#333",
        grid: "#eee",
        hidden: "#999",
      },
    };
    config.maList.forEach((i) => {
      this.config.colors[i.key] = i.color;
    });
  }

  // 初始化图表
  init() {
    this.calcPositions();
    this.draw();
    this.setupEventListeners();
  }

  calcPositions() {
    const { width, height } = this.canvas;
    const ys = (this.config.title ? 40 * this.dpr : 0) + 30 * this.dpr; // 标题+图例高度
    const ye = 50 * this.dpr; // X轴标签高度
    const xs = 80 * this.dpr; // Y轴宽度
    const xe = 80 * this.dpr;

    // 计算上下两个图表区域
    const chartHeight = height - ye - ys;
    const klineHeight = chartHeight * 0.7;
    const shareHeight = chartHeight * 0.3;
    const gap = 20 * this.dpr;

    this.chartAreas = {
      x: xs,
      ky: ys,
      vy: ys + klineHeight + gap,
      width: width - xe - xs,
      kHeight: klineHeight,
      vHeight: shareHeight,
    };

    // 为基类提供chartArea属性以确保兼容性
    this.chartArea = {
      x: xs,
      y: ys,
      width: width - xe - xs,
      height: chartHeight,
    };
  }

  // 主绘制函数
  draw() {
    this.dataRangeIndices = this.calcDataRangeIndices(
      this.config.data?.length || 0
    );
    this.ctx.font = 12 * this.dpr + "px Arial";
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
    const yAxisRanges = this.calculateYAxisRanges();
    // 绘制各种图表元素
    this.drawGridlines();
    this.drawAxes();
    this.drawAxisLabels(yAxisRanges);
    // 设置裁剪区域，确保图形不超出坐标轴范围
    const { x, ky, width, kHeight } = this.chartAreas;
    const { vy, vHeight } = this.chartAreas;
    super.setClipArea(x, ky, width, kHeight);
    this.drawKLineArea(yAxisRanges);
    super.restoreClipArea();

    super.setClipArea(x, vy, width, vHeight);
    this.drawVolumeArea(yAxisRanges);
    super.restoreClipArea();

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

  // 绘制K线图区域
  drawKLineArea(yAxisRanges) {
    if (!(this.config.data?.length > 0)) return;
    if (super.isLegendItemVisible("kline")) {
      this.drawKlineChart(yAxisRanges["kline"]);
    }
    // 绘制均线
    this.config.maList?.forEach((config) => {
      if (super.isLegendItemVisible(config.key)) {
        const color = this.config.colors[config.key] || "#000000";
        this.drawMALine(yAxisRanges["kline"], color, config.key);
      }
    });
  }

  // 绘制交易量区域
  drawVolumeArea(yAxisRanges) {
    if (!(this.config.data?.length > 0)) return;
    if (super.isLegendItemVisible("share")) {
      this.drawVolumeChart(yAxisRanges, "share");
    }
    if (super.isLegendItemVisible("amount")) {
      this.drawVolumeChart(yAxisRanges, "amount");
    }
  }

  // 计算所有Y轴范围
  calculateYAxisRanges() {
    const prices = ["open", "high", "low", "latest"];
    if (this.config.maList) {
      prices.push(...this.config.maList.map((item) => item.key));
    }
    return {
      kline: this.calcDataRange(
        this.config.data || [],
        prices,
        this.dataRangeIndices
      ),
      share: this.calcDataRange(
        this.config.data || [],
        ["share"],
        this.dataRangeIndices
      ),
      amount: this.calcDataRange(
        this.config.data || [],
        ["amount"],
        this.dataRangeIndices
      ),
    };
  }

  // 绘制网格线
  drawGridlines() {
    const { ky, vy, kHeight, vHeight } = this.chartAreas;
    // 水平网格线
    super.drawHorizontalGridlines(ky, kHeight, 5, this.config.colors.grid);
    super.drawHorizontalGridlines(vy, vHeight, 3, this.config.colors.grid);
    // 垂直网格线
    super.drawVerticalGridlines(
      (i) => this.calcDatumIndex(i),
      () => ({
        start: this.chartAreas.ky,
        end: this.chartAreas.ky + this.chartAreas.kHeight,
      }),
      this.config.colors.grid
    );
    super.drawVerticalGridlines(
      (i) => this.calcDatumIndex(i),
      () => ({
        start: this.chartAreas.vy,
        end: this.chartAreas.vy + this.chartAreas.vHeight,
      }),
      this.config.colors.grid
    );
  }

  // 绘制坐标轴
  drawAxes() {
    // X轴和Y轴
    this.drawXAxis();
    this.drawYAxes();
  }

  // 绘制X轴
  drawXAxis() {
    const { x, vy, vHeight, width } = this.chartAreas;
    super.drawAxisLine(
      x - 25 * this.dpr,
      vy + vHeight,
      x + width + 25 * this.dpr,
      vy + vHeight
    );
  }

  // 绘制Y轴
  drawYAxes() {
    const { x, ky, vy, width, kHeight, vHeight } = this.chartAreas;
    const lx = x - 25 * this.dpr;
    const rx = x + width + 25 * this.dpr;
    // K线图区域Y轴
    super.drawAxisLine(lx, ky, lx, ky + kHeight);
    super.drawAxisLine(rx, ky, rx, ky + kHeight);
    // 交易量区域Y轴
    super.drawAxisLine(lx, vy, lx, vy + vHeight);
    super.drawAxisLine(rx, vy, rx, vy + vHeight);
  }

  // 绘制坐标轴标签
  drawAxisLabels(yAxisRanges) {
    // X轴标签
    this.drawXAxisLabels();
    // Y轴标签
    this.drawYAxisLabels(yAxisRanges);
  }

  // 绘制X轴标签
  drawXAxisLabels() {
    const { scale } = this.config;
    const { start, end } = this.dataRangeIndices;
    const { vy, vHeight } = this.chartAreas;
    const labels = this.config.labels || [];
    for (let i = start; i <= end; i++) {
      if (i % Math.max(1, Math.floor((5 * this.dpr) / scale)) === 0) {
        const xi = this.calcDatumIndex(i);
        const yi = vy + vHeight + 20 * this.dpr;
        super.drawAxisLabel(xi, yi, labels[i] || `数据${i + 1}`);
      }
    }
  }

  // 绘制Y轴标签
  drawYAxisLabels(yAxisRanges) {
    const { x, ky, vy, width, kHeight, vHeight } = this.chartAreas;
    // K线图区域Y轴标签
    const { max: kMax, min: kMin } = yAxisRanges["kline"];
    const kStep = (kMax - kMin) / 5;
    for (let i = 0; i <= 5; i++) {
      const text = (kMin + i * kStep).toFixed(3);
      const xi = x - 30 * this.dpr;
      const yi = ky + kHeight - (i * kHeight) / 5 + 4 * this.dpr;
      super.drawAxisLabel(xi, yi, text, "right", this.config.colors.axis);
    }
    // 右侧Y轴标签 (K线图区域)
    for (let i = 0; i <= 5; i++) {
      const text = (kMin + i * kStep).toFixed(3);
      const xi = x + width + 30 * this.dpr;
      const yi = ky + kHeight - (i * kHeight) / 5 + 4 * this.dpr;
      super.drawAxisLabel(xi, yi, text, "left", this.config.colors.axis);
    }
    // 交易量区域Y轴标签 (合并左右两侧)
    const shareData = yAxisRanges["share"];
    const amountData = yAxisRanges["amount"];
    const sStep = (shareData.max - shareData.min) / 3;
    const aStep = (amountData.max - amountData.min) / 3;
    for (let i = 0; i <= 3; i++) {
      // 左侧标签
      const sText = Math.round(shareData.min + i * sStep);
      const aText = Math.round(amountData.min + i * aStep);
      const xi = x - 30 * this.dpr;
      const yi = vy + vHeight - (i * vHeight) / 3 + 4 * this.dpr;
      super.drawAxisLabel(xi, yi, sText, "right", this.config.colors.axis);
      // 右侧标签
      super.drawAxisLabel(
        x + width + 30 * this.dpr,
        yi,
        aText,
        "left",
        this.config.colors.axis
      );
    }
  }

  // 绘制K线图
  drawKlineChart(yAxisRange) {
    const { spacing, scale } = this.config;
    const klineData = this.config.data;
    const { min, max } = yAxisRange;
    const { start, end } = this.dataRangeIndices;
    const { ky, kHeight } = this.chartAreas;
    const candleWidth = Math.max(
      1,
      Math.min(10 * this.dpr, spacing * scale * 0.8)
    );

    for (let i = start; i <= end; i++) {
      const datum = klineData[i];
      if (!datum) continue;
      const { open, high, low, latest } = datum;
      const xi = this.calcDatumIndex(i);
      const candleX = xi - candleWidth / 2;
      // 计算K线各值的Y坐标
      const openY = ky + kHeight - ((open - min) / (max - min)) * kHeight;
      const latestY = ky + kHeight - ((latest - min) / (max - min)) * kHeight;
      const highY = ky + kHeight - ((high - min) / (max - min)) * kHeight;
      const lowY = ky + kHeight - ((low - min) / (max - min)) * kHeight;
      // 绘制影线
      this.ctx.beginPath();
      this.ctx.strokeStyle =
        latest >= open ? this.config.colors.rising : this.config.colors.falling;
      this.ctx.lineWidth = 1 * this.dpr;
      this.ctx.moveTo(xi, highY);
      this.ctx.lineTo(xi, lowY);
      this.ctx.stroke();
      // 绘制实体
      const rectHeight = Math.abs(latestY - openY);
      const rectY = Math.min(openY, latestY);
      if (rectHeight < 1) {
        // 当实体高度小于1像素时，绘制一条线
        this.ctx.beginPath();
        this.ctx.strokeStyle =
          latest >= open
            ? this.config.colors.rising
            : this.config.colors.falling;
        this.ctx.lineWidth = 1 * this.dpr;
        this.ctx.moveTo(candleX, rectY);
        this.ctx.lineTo(candleX + candleWidth, rectY);
        this.ctx.stroke();
      } else {
        // 绘制实体矩形
        this.ctx.fillStyle =
          latest >= open
            ? this.config.colors.rising
            : this.config.colors.falling;
        this.ctx.fillRect(candleX, rectY, candleWidth, rectHeight);
      }
    }
  }

  // 绘制均线曲线
  drawMALine(yAxisRange, color, maType) {
    const klineData = this.config.data;
    const { min, max } = yAxisRange;
    const { start, end } = this.dataRangeIndices;
    const { ky, kHeight } = this.chartAreas;
    this.ctx.beginPath();
    this.ctx.strokeStyle = color;
    this.ctx.lineWidth = 1 * this.dpr;
    let firstPoint = true;
    for (let i = start; i <= end; i++) {
      const datum = klineData[i];
      if (!datum || datum[maType] === undefined || datum[maType] === null) {
        continue;
      }
      const value = datum[maType];
      const xi = this.calcDatumIndex(i);
      const yi = ky + kHeight - ((value - min) / (max - min)) * kHeight;
      if (firstPoint) {
        this.ctx.moveTo(xi, yi);
        firstPoint = false;
      } else {
        this.ctx.lineTo(xi, yi);
      }
    }
    this.ctx.stroke();
  }

  // 绘制交易量柱状图
  drawVolumeChart(yAxisRanges, type) {
    const { spacing, scale } = this.config;
    const klineData = this.config.data;
    const { min, max } = yAxisRanges[type];
    const { start, end } = this.dataRangeIndices;
    const { vy, vHeight } = this.chartAreas;
    const barWidth = Math.max(
      1 * this.dpr,
      Math.min(10 * this.dpr, spacing * scale * 0.4)
    );
    for (let i = start; i <= end; i++) {
      const datum = klineData[i];
      if (!datum) continue;
      const xi = this.calcDatumIndex(i);
      const value = datum[type];
      const barX =
        xi + (type === "share" ? -barWidth - 2 * this.dpr : 2 * this.dpr);
      const barHeight = ((value - min) / (max - min)) * vHeight;
      const barY = vy + vHeight - barHeight;
      if (type === "share") {
        // 根据K线涨跌确定交易量柱状图颜色
        this.ctx.fillStyle =
          this.config.colors[datum.latest >= datum.open ? "rising" : "falling"];
      } else {
        // 交易金额
        this.ctx.fillStyle = this.config.colors.amount;
      }
      this.ctx.fillRect(barX, barY, barWidth, barHeight);
    }
  }

  // 绘制标题
  drawTitle() {
    if (!this.config.title) return;
    super.drawTitle(
      this.config.title,
      this.canvas.width / 2,
      30 * this.dpr,
      this.config.colors.axis
    );
  }

  // 获取图例项数据
  getLegendItems() {
    if (!(this.config.data?.length > 0)) return [];
    const legendX = 60 * this.dpr;
    const legendY = (this.config.title ? 60 : 20) * this.dpr;
    const legendItems = [];
    // K线图例项
    legendItems.push({
      key: "kline",
      name: "K线图",
      x: legendX,
      y: legendY,
      visible: super.isLegendItemVisible("kline"),
      drawIcon: (itemX, itemY) => {
        if (super.isLegendItemVisible("kline")) {
          this.drawLegendColorBox(
            itemX,
            itemY - 6 * this.dpr,
            5 * this.dpr,
            10 * this.dpr,
            this.config.colors.rising
          );
          this.drawLegendColorBox(
            itemX + 5 * this.dpr,
            itemY - 6 * this.dpr,
            5 * this.dpr,
            10 * this.dpr,
            this.config.colors.falling
          );
        } else {
          this.drawLegendBorder(
            itemX,
            itemY - 6 * this.dpr,
            5 * this.dpr,
            10 * this.dpr,
            this.config.colors.hidden
          );
          this.drawLegendBorder(
            itemX + 5 * this.dpr,
            itemY - 6 * this.dpr,
            5 * this.dpr,
            10 * this.dpr,
            this.config.colors.hidden
          );
        }
      },
    });

    // 均线图例项
    this.config.maList?.forEach((config, index) => {
      legendItems.push({
        key: config.key,
        name: config.name,
        x: legendX + (index + 1) * 80 * this.dpr,
        y: legendY,
        visible: super.isLegendItemVisible(config.key),
        drawIcon: (itemX, itemY) => {
          const color = this.config.colors[config.key] || "#0000ff";
          this.drawLegendLine(
            itemX,
            itemY,
            itemX + 10 * this.dpr,
            itemY,
            color,
            2
          );
          this.drawLegendCircle(
            itemX + 5 * this.dpr,
            itemY,
            2 * this.dpr,
            color
          );
        },
      });
    });

    // 交易量图例项
    const maLength = this.config.maList?.length || 0;
    legendItems.push({
      key: "share",
      name: "交易量",
      x: legendX + (maLength + 1) * 80 * this.dpr,
      y: legendY,
      visible: super.isLegendItemVisible("share"),
      drawIcon: (itemX, itemY) => {
        this.drawLegendColorBox(
          itemX,
          itemY - 6 * this.dpr,
          10 * this.dpr,
          10 * this.dpr,
          this.config.colors.share
        );
      },
    });

    // 交易金额图例项
    legendItems.push({
      key: "amount",
      name: "交易金额",
      x: legendX + (maLength + 2) * 80 * this.dpr,
      y: legendY,
      visible: super.isLegendItemVisible("amount"),
      drawIcon: (itemX, itemY) => {
        this.drawLegendColorBox(
          itemX,
          itemY - 6 * this.dpr,
          10 * this.dpr,
          10 * this.dpr,
          this.config.colors.amount
        );
      },
    });

    return legendItems;
  }

  // 收集hover数据点
  collectHoverDataPoints(index) {
    const points = [];
    const data = this.config.data || [];
    const datum = data[index];

    if (!datum) return points;

    // 收集K线数据
    if (super.isLegendItemVisible("kline")) {
      points.push({
        seriesName: "开盘价",
        value: datum.open,
        color: this.config.colors.rising,
      });
      points.push({
        seriesName: "最高价",
        value: datum.high,
        color: this.config.colors.rising,
      });
      points.push({
        seriesName: "最低价",
        value: datum.low,
        color: this.config.colors.falling,
      });
      points.push({
        seriesName: "收盘价",
        value: datum.latest,
        color: this.config.colors.falling,
      });
    }
    // 收集均线数据
    this.config.maList?.forEach((config) => {
      if (
        super.isLegendItemVisible(config.key) &&
        datum[config.key] !== undefined
      ) {
        const color = this.config.colors[config.key] || "#0000ff";
        points.push({
          seriesName: config.name,
          value: datum[config.key].toFixed(3),
          color: color,
        });
      }
    });
    // 收集交易量数据
    if (super.isLegendItemVisible("share") && datum.share !== undefined) {
      points.push({
        seriesName: "交易量",
        value: datum.share,
        color: this.config.colors.share,
      });
    }
    // 收集交易金额数据
    if (super.isLegendItemVisible("amount") && datum.amount !== undefined) {
      points.push({
        seriesName: "交易金额",
        value: datum.amount,
        color: this.config.colors.amount,
      });
    }
    return points;
  }
}
