import Showable from "./Showable.js";
import Base from "./Base.js";

export default class Panel extends Showable(Base) {
  render() {
    const element = this.createElement("div");
    this.element = element;
    if (this.props.title) {
      const title = document.createElement("h3");
      title.textContent = this.props.title;
      title.className = "header";
      element.appendChild(title);
    }
    this.renderChildren(element);
    this.refreshShow();
    return element;
  }
}