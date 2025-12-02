import Reactive from "../../util/Reactive.js";
import Base from "./Base.js";

/**
 * Tab内容区组件
 */
export default class TabBody extends Base {
  render() {
    const element = this.createElement("div");
    this.element = element;
    this.refreshActive();
    return element;
  }

  refreshActive() {
    this.updateActive();
    this.element.innerHTML = "";
    if (this.active === undefined || this.active === null) {
      return;
    }
    // 展示激活的tab
    this.children.forEach((child, index) => {
      if (this.active === index) {
        this.element.appendChild(child.render());
      }
    });
  }

  updateActive() {
    const path = this.data.active?.path;
    if (path) {
      this.active = Reactive.get(data, path);
    }
  }
}
