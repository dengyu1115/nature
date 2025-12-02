import Base from "./Base.js";

// 按钮组件
export default class Button extends Base {
  render() {
    const element = this.createElement("button");
    this.element = element;
    this.refreshDisabled();
    this.refreshShow();
    this.refreshText();
    return element;
  }

  refreshDisabled() {
    this.update("disabled");
  }

  refreshText() {
    const path = this.data["text"]?.path;
    if (path) {
      this["text"] = Reactive.get(data, path);
    } else {
      this["text"] = this.props["text"] || "按钮";
    }
    this.element["textContent"] = this["text"];
  }

  refreshShow() {
    const path = this.data.show?.path;
    if (path) {
      this.element.style.display = Reactive.get(data, path) ? "grid" : "none";
    }
  }

  update(prop) {
    const path = this.data[prop]?.path;
    if (path) {
      this[prop] = Reactive.get(data, path);
    } else {
      this[prop] = this.props[prop];
    }
    this.element[prop] = this[prop];
  }
}
