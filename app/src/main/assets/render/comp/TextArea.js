import Reactive from "../../util/Reactive.js";
import Bindable from "./Bindable.js";
import Showable from "./Showable.js";
import Base from "./Base.js";

export default class TextArea extends Bindable(Showable(Base)) {
  render() {
    const element = this.createElement("textarea");
    this.element = element;
    element.placeholder = this.props.placeholder;
    element.rows = this.props.rows;
    element.cols = this.props.cols;

    if (this.props.maxlength !== undefined && this.props.maxlength !== "") {
      element.maxLength = this.props.maxlength;
    }

    if (this.props.minlength !== undefined && this.props.minlength !== "") {
      element.minLength = this.props.minlength;
    }

    element.addEventListener("input", (e) => {
      this.setValue();
    });

    element.addEventListener("change", (e) => {
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
    this.value = value;
    const path = this.data.value?.path;
    if (path) {
      Reactive.set(data, path, this.value, this);
    }
  }
}