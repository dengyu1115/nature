import Bindable from "./Bindable.js";
import Showable from "./Showable.js";
import Base from "./Base.js";

export default class Text extends Bindable(Showable(Base)) {
  render() {
    const element = this.createElement(this.props.tag);
    this.element = element;
    this.refreshShow();
    this.refreshText();
    return element;
  }

  refreshText() {
    this.refreshProp("text", "textContent");
  }
}