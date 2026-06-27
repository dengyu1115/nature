import Bindable from "./Bindable.js";
import Showable from "./Showable.js";
import Base from "./Base.js";

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

  refreshDisabled() { this.refreshBoolProp("disabled"); }

  refreshText() { this.refreshProp("text", "textContent", "按钮"); }

  setClickEvent() {
    const click = this.events.click;
    if (!click) {
      return;
    }
    try {
      const func = new Function(click);
      this.element.addEventListener("click", (e) => {
        try {
          this.element.disabled = true;
          func.call(this);
        } catch (err) {
          message.error(err.message);
        } finally {
          this.element.disabled = false;
        }
      });
    } catch (err) {
      message.error("事件编译出错:" + err.message);
    }
  }
}