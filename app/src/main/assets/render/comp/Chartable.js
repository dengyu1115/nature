import Reactive from "../../util/Reactive.js";

export default (BaseClass) =>
  class extends BaseClass {
    refreshConfig() {
      this.updateConfig();
      this.updateYIndices();
      this.updateLegends();
      this.updateMaList();
      this.updateData();
      this.initChart();
    }

    refreshData() {
      this.updateData();
      this.initChart();
    }

    refreshSeries() {
      this.updateYIndices();
      this.updateLegends();
      this.initChart();
    }

    refreshYIndices() {
      this.updateYIndices();
      this.updateLegends();
      this.initChart();
    }

    refreshMaList() {
      this.updateMaList();
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

    updateYIndices() {
      let yIndices = this.props.yIndices;
      const path = this.data.yIndices?.path;
      if (path) {
        const v = Reactive.get(data, path);
        if (v) yIndices = v;
      } else if (this.data.yIndices?.data) {
        yIndices = this.data.yIndices.data;
      }
      if (!yIndices) return;
      this.config.yAxis = yIndices.reduce((map, i) => {
        if (map[i.position] == null) {
          map[i.position] = [];
        }
        map[i.position].push({
          id: i.prop,
          visible: true,
          label: i.title,
          unit: i.unit,
          width: this.getVal ? this.getVal(i.width, 80) : 80,
          formatter: this.buildFormatter(i.formatter),
        });
        return map;
      }, {});
    }

    updateLegends() {
      let legends = this.props.legends;
      const path = this.data.legends?.path;
      if (path) {
        const v = Reactive.get(data, path);
        if (v) legends = v;
      } else if (this.data.legends?.data) {
        legends = this.data.legends.data;
      }
      if (!legends || !this.config.yAxis) return;
      this.config.series = legends.map((i) => {
        const yIndex = this.getYAxisIndex ? this.getYAxisIndex(this.config.yAxis, i.yIndex) : { position: "left", index: 0 };
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
    }

    updateMaList() {
      let maList = this.props.maList;
      const path = this.data.maList?.path;
      if (path) {
        const v = Reactive.get(data, path);
        if (v) maList = v;
      } else if (this.data.maList?.data) {
        maList = this.data.maList.data;
      }
      if (!maList) return;
      this.config.maList = maList.map((item) => ({
        key: item.prop,
        name: item.title,
        color: item.color,
      }));
    }

    buildFormatter(formatter) {
      if (!formatter) return null;
      return new Function(["value", "datum"], formatter);
    }
  };