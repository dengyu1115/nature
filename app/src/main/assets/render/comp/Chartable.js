import Reactive from "../../util/Reactive.js";

export default (BaseClass) => class extends BaseClass {
  refreshConfig() {
    this.updateConfig();
    this.updateData();
    this.initChart();
  }

  refreshData() {
    this.updateData();
    this.initChart();
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

  buildFormatter(formatter) {
    if (!formatter) return null;
    return new Function(["value", "datum"], formatter);
  }
};