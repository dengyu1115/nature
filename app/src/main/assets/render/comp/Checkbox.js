import Optionable from "./Optionable.js";
import Base from "./Base.js";

export default class Checkbox extends Optionable(Base) {
  _inputType = "checkbox";
  _defaultValue = [];

  _getValue() {
    this.value = this.inputs.filter((i) => i.checked).map((i) => i.value);
  }

  _isChecked(input) {
    return this.value.includes(input.value);
  }

  render() {
    const container = this.createElement("div");
    this.container = container;
    this.refreshOptions();
    return container;
  }
}