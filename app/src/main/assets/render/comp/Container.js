import Base from "./Base.js";

// 容器组件
export default class Container extends Base {
  render() {
    const element = this.createElement("div");
    this.element = element;
    this.refreshShow();
    // 添加子组件
    this.renderChildren(element);
    return element;
  }

  refreshShow() {
    const path = this.data.show?.path;
    if (path) {
      this.element.style.display = Reactive.get(data, path) ? "grid" : "none";
    }
  }
}
