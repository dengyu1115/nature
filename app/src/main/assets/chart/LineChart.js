import ChartBase from "./ChartBase.js";

export default class LineChart extends ChartBase {
  constructor(canvas, config) {
    super(canvas, config);
    this.hoverData = null;
    this.mouseX = 0;
    this.mouseY = 0;
    this.formatters = Object.fromEntries(
      Object.entries(this.config.yAxis).flatMap(([key, arr]) =>
        arr
          .filter((i) => i.formatter)
          .map((i, idx) => [key + "_" + idx, i.formatter])
      )
    );
  }

  init() {
    this.calcPositions();
    this.initTransX();
    this.draw();
    this.setupEventListeners();
  }

  calcPositions() {
    const { width, height } = this.canvas;
    let xs = 0;
    let xe = 0;
    let ys = 0;
    let ye = 0;
    if (this.config.title) {
      ys += 50 * this.dpr;
    }
    ys += 20 * this.dpr;
    ye += 40 * this.dpr;
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
    if (xs == 0) {
      xs += 20 * this.dpr;
    }
    if (xe == 0) {
      xe += 20 * this.dpr;
    }
    this.chartArea = {
      x: xs,
      y: ys,
      width: width - xe - xs,
      height: height - ye - ys,
    };
  }

  draw() {
    const dataLength = Math.max(this.config.data?.length, 0);
    this.dataRangeIndices = this.calcDataRangeIndices(dataLength);
    this.ctx.font = 12 * this.dpr + "px Arial";
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
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
    this.drawGridlines();
    this.drawAxes();
    this.drawAxisLabels(yAxisRanges);

    const { x, y, width, height } = this.chartArea;
    super.setClipArea(x, y, width, height);

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
      } else if (series.type === "scatter") {
        this.drawScatterSeries(series, ranges);
      }
    });

    super.restoreClipArea();

    this.drawLegend();
    this.drawTitle();
    if (this.hoverData) {
      this.drawHoverData(
        this.collectHoverDataPoints.bind(this),
        this.config.labels || []
      );
    }
  }

  calcAxisDataRangeIndices(side, index) {
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
    if (min === Infinity || max === -Infinity) {
      return { min: 0, max: 1 };
    }
    const padding = (max - min) * 0.1;
    return { min: min - padding, max: max + padding };
  }

  drawGridlines() {
    const count = 5;
    const { y, height } = this.chartArea;
    super.drawHorizontalGridlines(y, height, count);
    super.drawVerticalGridlines(
      (i) => this.calcDatumIndex(i),
      () => ({
        start: this.chartArea.y,
        end: this.chartArea.y + this.chartArea.height,
      })
    );
  }

  drawAxes() {
    const { x, y, width, height } = this.chartArea;
    super.drawAxisLine(
      x - 25 * this.dpr,
      y + height,
      x + width + 25 * this.dpr,
      y + height
    );
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

  drawAxisLabels(yAxisRanges) {
    const { labels, scale } = this.config;
    const { x, y, width, height } = this.chartArea;
    const { start, end } = this.dataRangeIndices;
    for (let i = start; i <= end; i++) {
      if (i % Math.max(1, Math.floor(5 / scale)) === 0) {
        const xi = this.calcDatumIndex(i);
        const yi = y + height + 20 * this.dpr;
        super.drawAxisLabel(xi, yi, labels[i] || "");
      }
    }
    Object.entries(this.config.yAxis).forEach(([key, arr]) => {
      arr
        .filter((axis) => axis.visible)
        .forEach((axis, index) => {
          const axisKey = key + "_" + index;
          const { max, min } = yAxisRanges[axisKey];
          const step = (max - min) / 5;
          const unit = axis.unit ? axis.unit : "";
          const aw = axis.width || 60;
          const formatter = axis.formatter;
          for (let i = 0; i <= 5; i++) {
            const text =
              (formatter
                ? formatter(min + i * step)
                : (min + i * step).toFixed(1)) + unit;
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

  drawLineChart(series, yAxisRange) {
    const data = this.config.data || [];
    const { min, max } = yAxisRange;
    const { start, end } = this.dataRangeIndices;
    const { y, height } = this.chartArea;
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

  drawAreaChart(series, yAxisRange) {
    const data = this.config.data || [];
    const { min, max } = yAxisRange;
    const { start, end } = this.dataRangeIndices;
    const { y, height } = this.chartArea;
    this.ctx.beginPath();
    this.ctx.fillStyle = series.color + "80";
    let firstPoint = true;
    for (let i = start; i <= end; i++) {
      const datum = data[i];
      if (datum === undefined || datum === null) continue;
      const value = datum[series.prop];
      if (value === undefined) continue;
      const xi = this.calcDatumIndex(i);
      const yi = y + height - ((value - min) / (max - min)) * height;
      if (firstPoint) {
        this.ctx.moveTo(xi, y + height);
        this.ctx.lineTo(xi, yi);
        firstPoint = false;
      } else {
        this.ctx.lineTo(xi, yi);
      }
    }
    if (!firstPoint) {
      const lastX = this.calcDatumIndex(end);
      this.ctx.lineTo(lastX, y + height);
      this.ctx.closePath();
      this.ctx.fill();
    }
    this.drawLineChart(series, yAxisRange);
  }

  drawScatterSeries(series, yAxisRange) {
    const data = this.config.data || [];
    const { min, max } = yAxisRange;
    const { start, end } = this.dataRangeIndices;
    const { y, height } = this.chartArea;
    const markerSize = (series.markerSize || 12) * this.dpr;
    this.ctx.fillStyle = series.color;
    this.ctx.strokeStyle = "rgba(255,255,255,0.9)";
    this.ctx.lineWidth = 1.5 * this.dpr;

    for (let i = start; i <= end; i++) {
      const datum = data[i];
      if (datum === undefined || datum === null) continue;
      const value = datum[series.prop];
      if (value === undefined || value === null) continue;
      const xi = this.calcDatumIndex(i);
      const yi = y + height - ((value - min) / (max - min)) * height;
      this.drawMarker(xi, yi, series.marker || "circle", markerSize);
    }
  }

  drawMarker(x, y, type, size) {
    this.ctx.beginPath();
    if (type === "triangleUp") {
      this.ctx.moveTo(x, y - size * 0.6);
      this.ctx.lineTo(x - size * 0.7, y + size * 0.4);
      this.ctx.lineTo(x + size * 0.7, y + size * 0.4);
      this.ctx.closePath();
    } else if (type === "triangleDown") {
      this.ctx.moveTo(x, y + size * 0.6);
      this.ctx.lineTo(x - size * 0.7, y - size * 0.4);
      this.ctx.lineTo(x + size * 0.7, y - size * 0.4);
      this.ctx.closePath();
    } else if (type === "square") {
      this.ctx.rect(x - size * 0.5, y - size * 0.5, size, size);
    } else {
      this.ctx.arc(x, y, size * 0.3, 0, Math.PI * 2);
    }
    this.ctx.fill();
    this.ctx.stroke();
  }

  drawTitle() {
    const { title } = this.config;
    super.drawTitle(
      title || "",
      this.canvas.width / 2,
      (this.config.paddingTop || 0) * this.dpr + 20 * this.dpr
    );
  }

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
            if (super.isLegendItemVisible(index)) {
              this.drawLegendColorBox(
                itemX,
                itemY - 6 * this.dpr,
                10 * this.dpr,
                10 * this.dpr,
                series.color
              );
            } else {
              this.drawLegendBorder(
                itemX,
                itemY - 6 * this.dpr,
                10 * this.dpr,
                10 * this.dpr
              );
            }
          } else if (series.type === "line") {
            this.drawLegendLine(
              itemX,
              itemY,
              itemX + 10 * this.dpr,
              itemY,
              super.isLegendItemVisible(index) ? series.color : "#999"
            );
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
              this.drawLegendBorder(
                itemX,
                itemY - 6 * this.dpr,
                10 * this.dpr,
                10 * this.dpr
              );
            }
          } else if (series.type === "scatter") {
            this.ctx.fillStyle = super.isLegendItemVisible(index) ? series.color : "#999";
            this.ctx.strokeStyle = super.isLegendItemVisible(index) ? "#fff" : "#999";
            this.ctx.lineWidth = 1 * this.dpr;
            const cx = itemX + 5 * this.dpr;
            const cy = itemY;
            const ms = 6 * this.dpr;
            this.ctx.beginPath();
            if (series.marker === "triangleDown") {
              this.ctx.moveTo(cx, cy + ms * 0.6);
              this.ctx.lineTo(cx - ms * 0.7, cy - ms * 0.4);
              this.ctx.lineTo(cx + ms * 0.7, cy - ms * 0.4);
              this.ctx.closePath();
            } else if (series.marker === "triangleUp") {
              this.ctx.moveTo(cx, cy - ms * 0.6);
              this.ctx.lineTo(cx - ms * 0.7, cy + ms * 0.4);
              this.ctx.lineTo(cx + ms * 0.7, cy + ms * 0.4);
              this.ctx.closePath();
            } else if (series.marker === "square") {
              this.ctx.rect(cx - ms * 0.5, cy - ms * 0.5, ms, ms);
            } else {
              this.ctx.arc(cx, cy, ms * 0.5, 0, Math.PI * 2);
            }
            this.ctx.fill();
            this.ctx.stroke();
          }
        },
      });
    });
    return legendItems;
  }

  collectHoverDataPoints(index) {
    const points = [];
    const data = this.config.data || [];
    const datum = data[index];
    if (!datum) return points;
    this.config.series.forEach((series, seriesIndex) => {
      if (!super.isLegendItemVisible(seriesIndex)) return;
      const value = datum[series.prop];
      if (value === undefined) return;
      const axisKey = `${series.yAxis}_${series.yAxisIndex || 0}`;
      const formatter = this.formatters[axisKey];
      points.push({
        seriesName: series.name,
        value: formatter ? formatter(value) : value,
        color: series.color,
      });
    });
    return points;
  }
}