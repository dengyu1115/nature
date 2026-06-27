import Reactive from "../../util/Reactive.js";

export default (BaseClass) => class extends BaseClass {
  get _placeholder() { return "请选择"; }
  get _iconHtml() { return "?"; }
  get _inputWidth() { return "calc(100% - 24px)"; }
  get _clearBtnExtra() { return {}; }
  get _iconExtra() { return {}; }

  render() {
    const element = this.createElement("div");
    this.element = element;
    Object.assign(element.style, {
      display: "flex",
      flexDirection: "row",
      overflow: "visible",
    });
    if (this.events.change) {
      try {
        this.changeHandle = new Function(this.events.change);
      } catch (error) {
        console.error(error);
      }
    }
    this._buildStructure();
    element.append(this.input, this.clearBtn, this.icon);
    this.refreshShow();
    this.refreshValue();
    return element;
  }

  refreshShow() {
    const path = this.data.show?.path;
    if (path) {
      this.element.style.display = Reactive.get(data, path) ? "flex" : "none";
    }
  }

  refreshValue() {
    const path = this.data.value?.path;
    if (path) {
      this.value = Reactive.get(data, path);
    } else {
      this.value = null;
    }
    if (this.value) {
      this.input.value = this.value;
      this.clearBtn.style.display = "flex";
    } else {
      this.input.value = "";
      this.clearBtn.style.display = "none";
    }
    this._afterRefreshValue();
  }

  _afterRefreshValue() {}

  _buildStructure() {
    this.input = document.createElement("input");
    this.input.type = "text";
    this.input.placeholder = this._placeholder;
    this.input.readOnly = true;
    Object.assign(this.input.style, {
      flex: "1",
      width: this._inputWidth,
      height: "100%",
      border: "none",
      outline: "none",
      padding: "0 5px",
      boxSizing: "border-box",
    });

    this.clearBtn = document.createElement("span");
    this.clearBtn.innerHTML = "×";
    Object.assign(this.clearBtn.style, {
      height: "100%",
      display: "none",
      alignItems: "center",
      justifyContent: "center",
      cursor: "pointer",
      color: "#999",
      ...this._clearBtnExtra,
    });

    this.icon = document.createElement("span");
    this.icon.innerHTML = this._iconHtml;
    Object.assign(this.icon.style, {
      height: "100%",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      cursor: "pointer",
      ...this._iconExtra,
    });

    this.input.addEventListener("click", (e) => { e.stopPropagation(); this.showPicker(); });
    this.clearBtn.addEventListener("click", (e) => { e.stopPropagation(); this._clear(); });
    this.icon.addEventListener("click", (e) => { e.stopPropagation(); this.showPicker(); });
    document.addEventListener("click", () => {
      if (this.picker && this.picker.parentNode) { this.hidePicker(); }
    });
  }

  _clear() {
    const changed = this.value !== null;
    this.value = null;
    this.input.value = "";
    this.clearBtn.style.display = "none";
    if (changed) {
      this.changeHandle?.call(this);
      const path = this.data.value?.path;
      if (path) { Reactive.set(data, path, this.value, this); }
    }
  }

  showPicker() {
    if (!this.picker) { this.createPicker(); }
    const rect = this.input.getBoundingClientRect();
    Object.assign(this.picker.style, {
      position: "fixed",
      top: rect.bottom + "px",
      left: rect.left + "px",
      display: "block",
    });
    document.body.appendChild(this.picker);
  }

  hidePicker() {
    if (this.picker && this.picker.parentNode) {
      this.picker.parentNode.removeChild(this.picker);
    }
  }

  _select(value) {
    const changed = this.value !== value;
    if (changed) {
      this.value = value;
      this.input.value = value;
      this.clearBtn.style.display = "flex";
      this.changeHandle?.call(this);
      const path = this.data.value?.path;
      if (path) { Reactive.set(data, path, this.value, this); }
    }
    this._afterSelect();
    this.hidePicker();
  }

  _afterSelect() {}
};