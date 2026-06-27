import Chartable from "./Chartable.js";
import Base from "./Base.js";

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
        formatter: this.buildFormatter(i.formatter),
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