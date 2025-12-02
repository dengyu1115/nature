import Base from "./Base.js";

import Chart from "../../chart/LineChart.js";
import Reactive from "../../util/Reactive.js";
import Val from "../../util/Val.js";

/**
 * 图形
 */
export default class LineChart extends Base {
  render() {
    const element = this.createElement("canvas");
    this.element = element;
    element.width = Val.extract(this.styles.width)[1];
    element.height = Val.extract(this.styles.height)[1];
    this.config = {
      // 图表标题
      title: this.props.title,
      // 数据点间距
      spacing: 40,
      // 缩放
      scale: 1,
      // 平移
      transX: 0,
    };
    this.config.yAxis = this.props.yIndices.reduce((map, i) => {
      if (map[i.position] == null) {
        map[i.position] = [];
      }
      map[i.position].push({
        id: i.prop,
        visible: true,
        label: i.title,
        unit: i.unit,
        width: this.getVal(i.width, 80),
      });
      return map;
    }, {});
    this.config.series = this.props.legends.map((i) => {
      const yIndex = this.getYAxisIndex(this.config.yAxis, i.yIndex);
      return {
        prop: i.prop,
        name: i.label,
        type: i.type,
        yAxis: yIndex.position,
        yAxisIndex: yIndex.index,
        color: i.color,
        visible: true,
      };
    });
    this.updateConfig();
    this.updateData();
    new Chart(element, this.config).init();
    return element;
  }

  refreshConfig() {
    this.updateConfig();
    this.updateData();
    new Chart(this.element, this.config).init();
  }

  refreshData() {
    this.updateData();
    new Chart(this.element, this.config).init();
  }

  updateConfig() {
    const path = this.data.config?.path;
    const config = path ? Reactive.get(data, path) : this.data.config?.data;
    if (config) {
      this.config = { ...this.config, ...config };
    }
  }

  updateData() {
    const path = this.data.data?.path;
    const chart = path ? Reactive.get(data, path) : this.data.data?.data;
    if (chart) {
      this.config.data = chart.data || [];
      this.config.labels = chart.labels || [];
    } else {
      this.config.data = [];
      this.config.labels = [];
    }
  }

  /**
   * 获取Y轴下标
   * @param {*} yAxis
   * @param {*} prop
   * @returns
   */
  getYAxisIndex(yAxis, prop) {
    for (const [k, v] of Object.entries(yAxis)) {
      const idx = v.findIndex((i) => i.id === prop);
      if (idx !== -1) {
        return {
          position: k,
          index: idx,
        };
      }
    }
    return {
      position: "left",
      index: 0,
    };
  }
  getVal(val, defVal) {
    return val ? parseFloat(Val.extract(val)[1]) : defVal;
  }
}
