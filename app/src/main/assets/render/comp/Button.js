import EventUtil from "../../util/EventUtil.js";
import Base from "./Base.js";
import Bindable from "./Bindable.js";
import Showable from "./Showable.js";

export default class Button extends Bindable(Showable(Base)) {
  render() {
    const element = this.createElement("button");
    this.element = element;
    this.setClickEvent();
    this.refreshDisabled();
    this.refreshShow();
    this.refreshText();
    return element;
  }

  refreshDisabled() {
    this.refreshBoolProp("disabled");
  }

  refreshText() {
    this.refreshProp("text", "textContent", "按钮");
  }

  setClickEvent() {
    const func = EventUtil.compileAsync(this.events.click);
    if (!func) {
      return;
    }
    this.element.addEventListener("click", (e) => {
      func.call(this, e, this.element);
    });
  }
}
