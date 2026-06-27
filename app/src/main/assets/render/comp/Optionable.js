import Reactive from "../../util/Reactive.js";

export default (BaseClass) => class extends BaseClass {
  get _inputType() { throw new Error("Must implement _inputType"); }
  get _defaultValue() { throw new Error("Must implement _defaultValue"); }

  _getValue() { throw new Error("Must implement _getValue()"); }
  _isChecked(input) { throw new Error("Must implement _isChecked()"); }

  refreshOptions() {
    this.updateOptions();
    this.buildOptions();
    this.refreshValue();
  }

  refreshValue() {
    this.updateValue();
    this.updateUI();
  }

  updateOptions() {
    const path = this.data.options?.path;
    if (path) {
      this.options = Reactive.get(data, path) || [];
    } else {
      this.options = this.data.options?.data || this.props.options || [];
    }
  }

  updateValue() {
    const path = this.data.value?.path;
    if (path) {
      this.value = Reactive.get(data, path) || this._defaultValue;
    } else {
      this.value = this.data.value?.data || this._defaultValue;
    }
  }

  buildOptions() {
    this.container.innerHTML = "";
    this.inputs = [];
    this.options.forEach((option) => {
      const label = document.createElement("label");
      const input = document.createElement("input");
      input.type = this._inputType;
      input.value = option.value;
      input.disabled = option.disabled === "true";
      input.checked = option.checked === "true";
      if (this.props.name) {
        input.name = this.props.name;
      }
      input.addEventListener("input", () => {
        this._getValue();
        const path = this.data.value?.path;
        if (path) {
          Reactive.set(data, path, this.value, this);
        }
      });
      label.appendChild(input);
      label.appendChild(document.createTextNode(option.label));
      this.container.appendChild(label);
      this.inputs.push(input);
    });
  }

  updateUI() {
    this.inputs.forEach((input) => {
      input.checked = this._isChecked(input);
    });
  }
};