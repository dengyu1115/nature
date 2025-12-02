import Base from "./Base.js";

import Chart from "../../chart/KlineChart.js";
import Reactive from "../../util/Reactive.js";
import Val from "../../util/Val.js";

/**
 * 图形
 */
export default class KlineChart extends Base {
  render() {
    const element = this.createElement("canvas");
    this.element = element;
    // 添加canvas到容器
    element.width = Val.extract(this.styles.width)[1];
    element.height = Val.extract(this.styles.height)[1];
    this.config = {
      title: this.props.title,
      spacing: 20,
      scale: 1,
      transX: 0,
    };
    this.config.maList = this.props.maList.map((item) => {
      return {
        key: item.prop,
        name: item.title,
        color: item.color,
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
      this.config.data = chart.data;
      this.config.labels = chart.labels;
    } else {
      this.config.data = [];
      this.config.labels = [];
    }
  }
}
