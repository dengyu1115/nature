import Base from "./Base.js";
import Chartable from "./Chartable.js";

import Chart from "../../chart/KlineChart.js";
import Val from "../../util/Val.js";

export default class KlineChart extends Chartable(Base) {
  render() {
    const element = this.createElement("canvas");
    this.element = element;
    this.config = {
      title: this.props.title,
      spacing: 20,
      scale: 1,
      transX: 0,
      formatter: {
        kline: this.buildFormatter(this.props.klineFormatter),
        share: this.buildFormatter(this.props.shareFormatter),
        amount: this.buildFormatter(this.props.amountFormatter),
      },
    };
    this.updateConfig();
    this.updateMaList();
    this.updateData();
    this.initChart();
    return element;
  }

  initChart() {
    this.element.width = Val.extract(this.styles.width)[1];
    this.element.height = Val.extract(this.styles.height)[1];
    new Chart(this.element, this.config).init();
  }
}