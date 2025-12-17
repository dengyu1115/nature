import Reactive from "../../util/Reactive.js";
import Base from "./Base.js";

export default class Input extends Base {
  render() {
    const element = this.createElement("input");
    this.element = element;
    element.type = this.props.type;
    element.placeholder = this.props.placeholder;
    // 如果有数据属性绑定input事件
    element.addEventListener("input", (e) => {
      this.setValue();
    });
    this.refreshReadOnly();
    this.refreshDisabled();
    this.refreshShow();
    this.refreshValue();
    return element;
  }

  refreshReadOnly() {
    const path = this.data.readOnly?.path;
    if (path) {
      this.element.readOnly = Reactive.get(data, path);
    } else {
      this.element.readOnly = this.props.readOnly === "true";
    }
  }

  refreshDisabled() {
    const path = this.data.disabled?.path;
    if (path) {
      this.element.disabled = Reactive.get(data, path);
    } else {
      this.element.disabled = this.props.disabled === "true";
    }
  }

  refreshValue() {
    const path = this.data.value?.path;
    if (path) {
      this.value = Reactive.get(data, path);
    } else {
      this.value = this.props.value;
    }
    this.element.value = this.value;
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

  setValue() {
    this.value = this.element.value;
    const path = this.data.value?.path;
    if (path) {
      Reactive.set(data, path, this.value, this);
    }
  }
}
