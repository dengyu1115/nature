import Optionable from "./Optionable.js";
import Base from "./Base.js";

export default class Radio extends Optionable(Base) {
  _inputType = "radio";
  _defaultValue = null;

  _getValue() {
    this.value = this.inputs.find((i) => i.checked)?.value;
  }

  _isChecked(input) {
    return this.value === input.value;
  }

  render() {
    const container = this.createElement("div");
    this.container = container;
    this.refreshOptions();
    return container;
  }
}