import Reactive from "../../util/Reactive.js";
import Base from "./Base.js";

// 文本组件
export default class Text extends Base {
  render() {
    const element = this.createElement(this.props.tag);
    this.element = element;
    this.refreshShow();
    this.refreshText();
    return element;
  }

  refreshText() {
    const path = this.data.text?.path;
    if (path) {
      const text = Reactive.get(data, path);
      this.element.textContent = text;
    } else {
      this.element.textContent = this.props.text;
    }
  }

  refreshShow() {
    const path = this.data.show?.path;
    if (path) {
      this.element.style.display = Reactive.get(data, path) ? "grid" : "none";
    }
  }
}
