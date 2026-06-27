import Chartable from "./Chartable.js";
import Base from "./Base.js";

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
    this.config.maList = this.props.maList.map((item) => ({
      key: item.prop,
      name: item.title,
      color: item.color,
    }));
    this.updateConfig();
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