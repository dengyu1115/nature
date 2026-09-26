import Base from "./Base.js";
import Chartable from "./Chartable.js";

import Chart from "../../chart/LineChart.js";
import Val from "../../util/Val.js";

export default class LineChart extends Chartable(Base) {
  render() {
    const element = this.createElement("canvas");
    this.element = element;
    element.width = Val.extract(this.styles.width)[1];
    element.height = Val.extract(this.styles.height)[1];
    this.config = {
      title: this.props.title,
      spacing: 40,
      scale: 1,
      transX: 0,
    };
    this.updateConfig();
    this.updateYIndices();
    this.updateLegends();
    this.updateData();
    this.initChart();
    return element;
  }

  getYAxisIndex(yAxis, prop) {
    for (const [k, v] of Object.entries(yAxis)) {
      const idx = v.findIndex((i) => i.id === prop);
      if (idx !== -1) {
        return { position: k, index: idx };
      }
    }
    return { position: "left", index: 0 };
  }

  getVal(val, defVal) {
    return val ? parseFloat(Val.extract(val)[1]) : defVal;
  }

  initChart() {
    this.element.width = Val.extract(this.styles.width)[1];
    this.element.height = Val.extract(this.styles.height)[1];
    new Chart(this.element, this.config).init();
  }
}