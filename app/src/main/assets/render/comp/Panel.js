import Base from "./Base.js";

// 面板组件
export default class Panel extends Base {
  render() {
    const element = this.createElement("div");
    this.element = element;
    if (this.props.title) {
      const title = document.createElement("h3");
      title.textContent = this.props.title;
      title.className = "header";
      element.appendChild(title);
    }
    // 添加子组件
    this.renderChildren(element);
    this.refreshShow();
    return element;
  }

  refreshShow() {
    const path = this.data.show?.path;
    const show = path ? Reactive.get(data, path) : this.data.show?.data;
    if (show) {
      this.element.style.display = "grid";
    } else {
      this.element.style.display = "none";
    }
  }
}
