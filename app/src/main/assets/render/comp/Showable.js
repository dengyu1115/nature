import Reactive from "../../util/Reactive.js";

export default (BaseClass) => class extends BaseClass {
  refreshShow() {
    const showConfig = this.data.show;
    if (!showConfig) return;
    const path = showConfig.path;
    const show = path ? Reactive.get(data, path) : showConfig.data;
    if (show !== undefined) {
      this.element.style.display = show ? "grid" : "none";
    }
  }
};