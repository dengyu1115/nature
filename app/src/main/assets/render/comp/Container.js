import Showable from "./Showable.js";
import Base from "./Base.js";

export default class Container extends Showable(Base) {
  render() {
    const element = this.createElement("div");
    this.element = element;
    this.refreshShow();
    this.renderChildren(element);
    return element;
  }
}