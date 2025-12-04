export default class ChartBase {
  constructor(canvas, config) {
    this.canvas = canvas;
    this.ctx = canvas.getContext("2d");
    // 获取设备像素比，默认为1
    this.dpr = window.devicePixelRatio || 1;
    // 调整canvas尺寸以适应DPR
    canvas.width = canvas.width * this.dpr;
    canvas.height = canvas.height * this.dpr;
    this.config = {
      spacing: 20 * this.dpr,
      scale: 1,
      transX: 0,
      ...config,
    };
    this.isDragging = false;
    this.lastMouseX = 0;
    this.hoverData = null;
    this.mouseX = 0;
    this.mouseY = 0;
    // Pointer事件相关属性
    this.pointers = new Map(); // 存储所有活动的指针
    this.initialPinchDistance = 0;
    this.initialScale = 1;
    this.initialTransX = 0;
    canvas.style.touchAction = "none";
    // 初始化隐藏的图例项集合（只记录隐藏的项）
    this.hiddenLegendItems = new Set();
  }

  initTransX() {
    // 设置默认显示末端数据
    const width = this.chartArea.width || 0;
    const maxIndex = (this.config.data?.length || 0) - 1;
    const spacing = this.config.spacing * this.config.scale;
    this.config.transX = width - maxIndex * spacing;
  }

  // 计算可见数据下标范围的通用方法
  calcDataRangeIndices(length) {
    const { spacing, transX, scale } = this.config;
    const width = this.chartArea.width;
    const density = spacing * scale;
    const max = length - 1;
    const start = Math.max(0, Math.floor(-transX / density));
    const end = Math.min(max, Math.ceil((width - transX) / density));
    return { start, end };
  }

  // 通用方法：计算指定字段在数据范围内的最大值和最小值
  calcDataRange(data, fields, dataRangeIndices) {
    const { start, end } = dataRangeIndices;
    let min = Infinity;
    let max = -Infinity;
    for (let i = start; i <= end; i++) {
      fields.forEach((field) => {
        if (data[i]?.[field] !== undefined && data[i]?.[field] !== null) {
          min = Math.min(min, data[i][field]);
          max = Math.max(max, data[i][field]);
        }
      });
    }
    // 如果没有找到有效数据，返回默认范围
    if (min === Infinity || max === -Infinity) {
      return { min: 0, max: 1 };
    }
    // 添加边距使图表更美观
    const padding = (max - min) * 0.1 * this.dpr;
    return {
      min: min - padding,
      max: max + padding,
    };
  }

  // 通用方法：获取图例数据（需要子类实现）
  getLegendItems() {
    return [];
  }

  // 通用方法：绘制图例
  drawLegend() {
    // 获取子类提供的图例项数据
    const legendItems = this.getLegendItems();
    if (!legendItems || legendItems.length === 0) return;
    this.legendPositions = [];
    let currentIndex = 0;
    legendItems.forEach((item) => {
      const itemX = item.x || currentIndex * 80 * this.dpr;
      const itemY = item.y || 0;
      // 绘制图标
      if (item.drawIcon) {
        item.drawIcon(itemX, itemY);
      }
      // 绘制文本
      this.ctx.fillStyle = item.visible !== false ? "#000" : "#999";
      this.ctx.textAlign = "left";
      this.ctx.textBaseline = "middle";
      this.ctx.fillText(item.name, itemX + 20 * this.dpr, itemY);
      // 记录图例位置信息
      this.legendPositions = this.legendPositions || [];
      this.legendPositions.push({
        key: item.key,
        x: itemX,
        y: itemY,
        width: 80 * this.dpr,
        height: 20 * this.dpr,
        handler: () => {
          this.toggleLegendVisibility(item.key);
        },
      });
      currentIndex++;
    });
  }

  // 通用方法：切换图例可见性
  toggleLegendVisibility(key) {
    if (this.hiddenLegendItems.has(key)) {
      this.hiddenLegendItems.delete(key);
    } else {
      this.hiddenLegendItems.add(key);
    }
    this.draw();
  }

  // 通用方法：检查图例项是否可见
  isLegendItemVisible(key) {
    return !this.hiddenLegendItems.has(key);
  }

  // 通用方法：绘制水平网格线
  drawHorizontalGridlines(y, height, count, strokeStyle = "#eee") {
    const { x, width } = this.chartArea;
    this.ctx.beginPath();
    this.ctx.strokeStyle = strokeStyle;
    this.ctx.lineWidth = 1 * this.dpr;
    for (let i = 0; i <= count; i++) {
      const yi = y + height - (i * height) / count;
      this.ctx.moveTo(x - 25 * this.dpr, yi);
      this.ctx.lineTo(x + width + 25 * this.dpr, yi);
    }
    this.ctx.stroke();
  }

  // 通用方法：绘制垂直网格线
  drawVerticalGridlines(getXi, getY, strokeStyle = "#eee") {
    const { scale } = this.config;
    const { start, end } = this.dataRangeIndices;
    this.ctx.beginPath();
    this.ctx.strokeStyle = strokeStyle;
    this.ctx.lineWidth = 1 * this.dpr;
    for (let i = start; i <= end; i++) {
      // 只绘制每隔一定间隔的网格线以避免过于密集
      if (i % Math.max(1, Math.floor(10 / scale)) === 0) {
        const xi = getXi(i);
        const yi = getY(i);
        // 网格线
        this.ctx.moveTo(xi, yi.start);
        this.ctx.lineTo(xi, yi.end);
      }
    }
    this.ctx.stroke();
  }

  // 通用方法：绘制坐标轴
  drawAxisLine(x1, y1, x2, y2, strokeStyle = "#333") {
    this.ctx.beginPath();
    this.ctx.strokeStyle = strokeStyle;
    this.ctx.lineWidth = 1 * this.dpr;
    this.ctx.moveTo(x1, y1);
    this.ctx.lineTo(x2, y2);
    this.ctx.stroke();
  }

  // 通用方法：绘制坐标轴标签
  drawAxisLabel(x, y, text, textAlign = "center", fillStyle = "#333") {
    this.ctx.fillStyle = fillStyle;
    this.ctx.textAlign = textAlign;
    this.ctx.fillText(text, x, y);
  }

  // 限制平移范围，防止移出数据区域
  constrainTranslation() {
    const { transX, spacing, scale } = this.config;
    const { width } = this.chartArea;
    const maxIndex = (this.config.data?.length || 0) - 1;
    const min = width - maxIndex * spacing * scale;
    const max = 0;
    this.config.transX = Math.max(min, Math.min(max, transX));
  }

  // 通用方法：计算数据点的X坐标
  calcDatumIndex(count) {
    const { transX, spacing, scale } = this.config;
    const { x } = this.chartArea;
    return x + transX + count * spacing * scale;
  }

  // 计算两点间距离
  getDistance(pointer1, pointer2) {
    const dx = pointer1.clientX - pointer2.clientX;
    const dy = pointer1.clientY - pointer2.clientY;
    return Math.sqrt(dx * dx + dy * dy);
  }

  // 设置通用事件监听器
  setupEventListeners() {
    // 鼠标滚轮缩放
    this.canvas.addEventListener("wheel", (event) => {
      event.preventDefault();
      if (event.deltaY === 0) return;
      const zoom = event.deltaY < 0 ? 1.03 : 0.97;
      const rect = this.canvas.getBoundingClientRect();
      const xi = (event.clientX - rect.left) * this.dpr;
      this.config.transX = xi - (xi - this.config.transX) * zoom;
      this.config.scale *= zoom;
      this.constrainTranslation();
      this.draw();
    });

    // Pointer事件处理
    const handlePointerDown = (event) => {
      this.pointers.set(event.pointerId, {
        clientX: event.clientX * this.dpr,
        clientY: event.clientY * this.dpr,
      });
      // 初始化长按检测
      this.longPressTimer = null;
      this.hasLongPressed = false;
      if (this.pointers.size === 1) {
        // 单指触摸，用于拖拽或长按
        this.lastMouseX = event.clientX * this.dpr;
        // 设置长按定时器（500ms）
        this.longPressTimer = setTimeout(() => {
          this.hasLongPressed = true;
          this.isDragging = false;
          // 触发 hover 处理逻辑
          const rect = this.canvas.getBoundingClientRect();
          this.mouseX = (event.clientX - rect.left) * this.dpr;
          this.mouseY = (event.clientY - rect.top) * this.dpr;
          this.handleHover();
        }, 500);
      } else if (this.pointers.size === 2) {
        // 双指触摸开始，准备进行 pinch 操作
        const pointersArray = Array.from(this.pointers.values());
        this.initialPinchDistance = this.getDistance(
          pointersArray[0],
          pointersArray[1]
        );
        this.initialScale = this.config.scale;
        this.initialTransX = this.config.transX;
        // 禁止拖拽
        this.isDragging = false;
        // 清除长按定时器
        if (this.longPressTimer) {
          clearTimeout(this.longPressTimer);
          this.longPressTimer = null;
        }
      }
      this.hoverData = null;
    };

    const handlePointerUp = (event) => {
      this.pointers.delete(event.pointerId);
      // 清除长按定时器
      if (this.longPressTimer) {
        clearTimeout(this.longPressTimer);
        this.longPressTimer = null;
      }
      this.isDragging = false;
      this.canvas.style.cursor = "default";
      this.hasLongPressed = false;
    };

    const handlePointerMove = (event) => {
      // 更新指针位置
      if (this.pointers.has(event.pointerId)) {
        this.pointers.set(event.pointerId, {
          clientX: event.clientX * this.dpr,
          clientY: event.clientY * this.dpr,
        });
      }
      // 清除长按定时器，因为用户开始移动了
      if (this.longPressTimer && this.pointers.size === 1) {
        const pointer = Array.from(this.pointers.values())[0];
        const threshold = 5 * this.dpr; // 移动阈值
        const deltaX = Math.abs(pointer.clientX - this.lastMouseX);
        if (deltaX > threshold) {
          clearTimeout(this.longPressTimer);
          this.longPressTimer = null;
        }
      }
      if (this.pointers.size === 1 && !this.hasLongPressed) {
        // 单指拖拽
        this.isDragging = true;
        const pointer = Array.from(this.pointers.values())[0];
        const deltaX = pointer.clientX - this.lastMouseX;
        this.lastMouseX = pointer.clientX;
        this.config.transX += deltaX;
        this.constrainTranslation();
        this.hoverData = null;
        this.draw();
      } else if (this.pointers.size === 2) {
        // 双指 pinch 缩放
        const pointers = Array.from(this.pointers.values());
        const currentDistance = this.getDistance(pointers[0], pointers[1]);
        const scale = currentDistance / this.initialPinchDistance;
        // 以双指中心点为中心进行缩放
        const centerX = (pointers[0].clientX + pointers[1].clientX) / 2;
        const rect = this.canvas.getBoundingClientRect();
        const canvasCenterX = centerX - rect.left * this.dpr;
        this.config.scale = this.initialScale * scale;
        this.config.transX =
          canvasCenterX - (canvasCenterX - this.initialTransX) * scale;
        this.constrainTranslation();
        this.hoverData = null; // 缩放时清除hover数据
        this.draw();
      }
    };

    const handleHoverMove = (event) => {
      if (this.isDragging || !this.hasLongPressed || this.pointers.size > 1) {
        return;
      }
      // 处理hover
      const rect = this.canvas.getBoundingClientRect();
      this.mouseX = (event.clientX - rect.left) * this.dpr;
      this.mouseY = (event.clientY - rect.top) * this.dpr;
      this.handleHover();
    };

    this.canvas.addEventListener("pointerdown", handlePointerDown);
    window.addEventListener("pointerup", handlePointerUp);
    window.addEventListener("pointermove", handlePointerMove);
    this.canvas.addEventListener("pointermove", handleHoverMove);
    this.canvas.addEventListener("click", (event) => {
      if (!this.legendPositions) return;
      const rect = this.canvas.getBoundingClientRect();
      const x = (event.clientX - rect.left) * this.dpr;
      const y = (event.clientY - rect.top) * this.dpr;
      // 检查是否点击了图例
      for (const legend of this.legendPositions) {
        if (
          x >= legend.x &&
          x <= legend.x + legend.width &&
          y >= legend.y - 10 * this.dpr &&
          y <= legend.y + 10 * this.dpr
        ) {
          // 执行图例点击处理器
          if (legend.handler) {
            legend.handler(legend.key);
          }
          break;
        }
      }
    });
  }

  // 处理hover事件的通用方法
  handleHover() {
    this.hoverData = null;
    // 计算当前鼠标所在的X位置对应的数据索引
    const { start, end } = this.dataRangeIndices;
    let closestIndex = null;
    let minDistance = Infinity;
    // 查找最接近鼠标X位置的数据点索引
    for (let i = start; i <= end; i++) {
      const pointX = this.calcDatumIndex(i);
      const distance = Math.abs(this.mouseX - pointX);
      if (distance < minDistance && distance < 20 * this.dpr) {
        minDistance = distance;
        closestIndex = i;
      }
    }
    // 如果找到对应的索引，设置hover数据
    if (closestIndex !== null) {
      this.hoverData = {
        dataIndex: closestIndex,
      };
    }
    this.draw();
  }

  // 通用方法：绘制hover数据提示的通用方法
  drawHoverData(collectHoverDataPoints, labels) {
    if (!this.hoverData) return;
    const { dataIndex } = this.hoverData;
    const label = labels[dataIndex] || `数据${dataIndex + 1}`;
    const points = collectHoverDataPoints(dataIndex);
    if (points.length === 0) return;
    // 计算提示框位置 - 跟随鼠标位置
    const x = this.mouseX;
    const y = this.mouseY - 10 * this.dpr;
    // 绘制提示框背景
    this.ctx.fillStyle = "rgba(255, 255, 255, 0.9)";
    this.ctx.strokeStyle = "#ccc";
    this.ctx.lineWidth = 1;
    const labelWidth = this.ctx.measureText(label).width;
    let maxSeriesWidth = 0;
    let maxValueWidth = 0;
    points.forEach((point) => {
      const seriesWidth = this.ctx.measureText(point.seriesName).width;
      const valueWidth = this.ctx.measureText(point.value).width;
      maxSeriesWidth = Math.max(maxSeriesWidth, seriesWidth);
      maxValueWidth = Math.max(maxValueWidth, valueWidth);
    });
    const padding = 8 * this.dpr;
    const boxWidth =
      Math.max(labelWidth, maxSeriesWidth + maxValueWidth + 20 * this.dpr) +
      padding * 2;
    const boxHeight =
      padding + 20 * this.dpr + points.length * 20 * this.dpr + padding;
    // 确保提示框不会超出画布边界
    let tooltipX = x;
    let tooltipY = y - boxHeight;
    // 防止提示框超出边界
    if (tooltipX - boxWidth / 2 < 0) {
      tooltipX = boxWidth / 2;
    } else if (tooltipX + boxWidth / 2 > this.canvas.width) {
      tooltipX = this.canvas.width - boxWidth / 2;
    }
    if (tooltipY < 0) {
      tooltipY = y + 20 * this.dpr;
    }
    // 绘制背景矩形
    this.ctx.fillRect(tooltipX - boxWidth / 2, tooltipY, boxWidth, boxHeight);
    this.ctx.strokeRect(tooltipX - boxWidth / 2, tooltipY, boxWidth, boxHeight);
    // 绘制标签
    this.ctx.fillStyle = "#333";
    this.ctx.textAlign = "center";
    this.ctx.textBaseline = "middle";
    this.ctx.fillText(label, tooltipX, tooltipY + padding + 10 * this.dpr);
    // 绘制每个数据点
    points.forEach((point, index) => {
      const yPos =
        tooltipY +
        padding +
        20 * this.dpr +
        index * 20 * this.dpr +
        10 * this.dpr;
      this.ctx.fillStyle = point.color;
      this.ctx.fillRect(
        tooltipX - boxWidth / 2 + padding,
        yPos - 6 * this.dpr,
        12 * this.dpr,
        12 * this.dpr
      );
      this.ctx.fillStyle = "#333";
      this.ctx.textAlign = "left";
      this.ctx.fillText(
        point.seriesName,
        tooltipX - boxWidth / 2 + padding + 18 * this.dpr,
        yPos
      );
      this.ctx.textAlign = "right";
      this.ctx.fillText(point.value, tooltipX + boxWidth / 2 - padding, yPos);
    });
  }

  // 通用方法：绘制图例颜色框
  drawLegendColorBox(x, y, width, height, fillStyle) {
    this.ctx.fillStyle = fillStyle;
    this.ctx.fillRect(x, y, width, height);
  }

  // 通用方法：绘制图例外框（隐藏状态）
  drawLegendBorder(x, y, width, height, strokeStyle = "#999") {
    this.ctx.strokeStyle = strokeStyle;
    this.ctx.strokeRect(x, y, width, height);
  }

  // 通用方法：绘制图例线条
  drawLegendLine(x1, y1, x2, y2, strokeStyle, lineWidth = 1) {
    this.ctx.strokeStyle = strokeStyle;
    this.ctx.lineWidth = lineWidth * this.dpr;
    this.ctx.beginPath();
    this.ctx.moveTo(x1, y1);
    this.ctx.lineTo(x2, y2);
    this.ctx.stroke();
  }

  // 通用方法：绘制图例圆点
  drawLegendCircle(x, y, radius, fillStyle, strokeStyle = null) {
    this.ctx.beginPath();
    if (fillStyle) {
      this.ctx.fillStyle = fillStyle;
      this.ctx.arc(x, y, radius, 0, Math.PI * 2);
      this.ctx.fill();
    }
    if (strokeStyle) {
      this.ctx.strokeStyle = strokeStyle;
      this.ctx.arc(x, y, radius, 0, Math.PI * 2);
      this.ctx.stroke();
    }
  }

  // 通用方法：绘制标题
  drawTitle(title, x, y, fillStyle = "#333") {
    if (!title) return;
    this.ctx.fillStyle = fillStyle;
    this.ctx.textAlign = "center";
    this.ctx.fillText(title, x, y);
  }

  // 通用方法：设置裁剪区域
  setClipArea(x, y, width, height) {
    this.ctx.save();
    this.ctx.beginPath();
    this.ctx.rect(x - 25 * this.dpr, y, width + 50 * this.dpr, height);
    this.ctx.clip();
  }

  // 通用方法：恢复裁剪区域
  restoreClipArea() {
    this.ctx.restore();
  }
}
