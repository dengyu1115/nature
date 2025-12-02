import Reactive from "../../util/Reactive.js";
import Base from "./Base.js";

export default class Radio extends Base {
  render() {
    const container = this.createElement("div");
    this.container = container;
    this.refreshOptions();
    return container;
  }

  refreshOptions() {
    this.updateOptions();
    this.buildOptions();
    this.refreshValue();
  }

  refreshValue() {
    this.updateValue();
    this.updateUI();
  }

  buildOptions() {
    this.container.innerHTML = "";
    this.inputs = [];
    this.options.forEach((option) => {
      const label = document.createElement("label");
      const input = document.createElement("input");
      input.type = "radio";
      input.value = option.value;
      input.disabled = option.disabled === "true";
      input.checked = option.checked === "true";
      if (this.props.name) {
        input.name = this.props.name;
      }
      input.addEventListener("input", (e) => {
        this.value = this.inputs.find((i) => i.checked)?.value;
        const path = this.data.value?.path;
        if (path) {
          Reactive.set(data, path, this.value, this);
        }
      });
      const text = document.createTextNode(option.label);
      label.appendChild(input);
      label.appendChild(text);
      this.container.appendChild(label);
      this.inputs.push(input);
    });
  }

  updateOptions() {
    const path = this.data.options?.path;
    // 如果有数据属性绑定input事件
    if (path) {
      const value = Reactive.get(data, path);
      this.options = value || [];
    } else {
      this.options = this.data.options?.data || this.props.options || [];
    }
  }

  updateValue() {
    const path = this.data.value?.path;
    if (path) {
      const value = Reactive.get(data, path);
      this.value = value || null;
    } else {
      this.value = this.data.value?.data || null;
    }
  }

  updateUI() {
    this.inputs.forEach((input) => {
      input.checked = this.value === input.value;
    });
  }
}
