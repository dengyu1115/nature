import Reactive from "../../util/Reactive.js";
import Bindable from "./Bindable.js";
import Showable from "./Showable.js";
import Base from "./Base.js";

export default class Input extends Bindable(Showable(Base)) {
  render() {
    const element = this.createElement("input");
    this.element = element;
    element.type = this.props.type;
    element.placeholder = this.props.placeholder;
    element.addEventListener("input", (e) => {
      this.setValue();
    });
    this.refreshReadOnly();
    this.refreshDisabled();
    this.refreshShow();
    this.refreshValue();
    return element;
  }

  refreshReadOnly() { this.refreshBoolProp("readOnly"); }

  refreshDisabled() { this.refreshBoolProp("disabled"); }

  refreshValue() { this.refreshProp("value"); }

  setValue() {
    const value = this.element.value;
    if (this.props.type === "number") {
      this.value = value ? +value : null;
    } else {
      this.value = value;
    }
    const path = this.data.value?.path;
    if (path) {
      Reactive.set(data, path, this.value, this);
    }
  }
}