import Pickerable from "./Pickerable.js";
import Base from "./Base.js";

export default class TimePicker extends Pickerable(Base) {
  get _placeholder() { return "请选择时间"; }
  get _iconHtml() { return "\u{1F556}"; }
  get _inputWidth() { return "calc(100% - 28px)"; }
  get _clearBtnExtra() { return { width: "12px" }; }
  get _iconExtra() { return { width: "16px" }; }

  selectTime() {
    const hours = this.hourPicker.getValue();
    const minutes = this.minutePicker.getValue();
    const seconds = this.secondPicker.getValue();
    this._select(this.formatTime({ hours, minutes, seconds }));
  }

  createPicker() {
    this.picker = document.createElement("div");
    this.picker.classList.add("component");
    Object.assign(this.picker.style, {
      position: "absolute",
      width: "250px",
      backgroundColor: "white",
      border: "1px solid #ccc",
      borderRadius: "4px",
      boxShadow: "0 2px 10px rgba(0,0,0,0.1)",
      zIndex: "1000",
      display: "none",
      padding: "10px",
    });

    const timeContainer = document.createElement("div");
    Object.assign(timeContainer.style, {
      display: "flex",
      justifyContent: "space-around",
      alignItems: "center",
      height: "120px",
    });

    this.hourPicker = this._createNumberSelector(0, 23, "时");
    this.minutePicker = this._createNumberSelector(0, 59, "分");
    this.secondPicker = this._createNumberSelector(0, 59, "秒");
    timeContainer.append(this.hourPicker.container, this.minutePicker.container, this.secondPicker.container);

    const buttonContainer = document.createElement("div");
    Object.assign(buttonContainer.style, {
      display: "flex",
      justifyContent: "flex-end",
      marginTop: "10px",
      paddingTop: "10px",
      borderTop: "1px solid #eee",
    });

    const confirmBtn = document.createElement("button");
    confirmBtn.textContent = "确定";
    Object.assign(confirmBtn.style, {
      padding: "5px 15px",
      backgroundColor: "#1890ff",
      color: "white",
      border: "none",
      borderRadius: "4px",
      cursor: "pointer",
    });
    confirmBtn.addEventListener("click", (e) => { e.stopPropagation(); this.selectTime(); });
    buttonContainer.appendChild(confirmBtn);
    this.picker.append(timeContainer, buttonContainer);

    if (this.value) {
      const parts = this.parseTimeString(this.value);
      if (parts) {
        this.hourPicker.setValue(parts.hours);
        this.minutePicker.setValue(parts.minutes);
        this.secondPicker.setValue(parts.seconds);
      }
    }
  }

  _createNumberSelector(min, max, label) {
    const container = document.createElement("div");
    Object.assign(container.style, { display: "flex", flexDirection: "column", alignItems: "center" });

    const labelElement = document.createElement("div");
    labelElement.textContent = label;
    Object.assign(labelElement.style, { marginBottom: "5px", color: "#666" });

    const valueDisplay = document.createElement("div");
    valueDisplay.textContent = min.toString().padStart(2, "0");
    Object.assign(valueDisplay.style, { fontWeight: "bold", margin: "5px 0", minWidth: "40px", textAlign: "center" });

    const upBtn = document.createElement("button");
    upBtn.innerHTML = "&#9650;";
    Object.assign(upBtn.style, { width: "24px", height: "24px", border: "1px solid #ccc", backgroundColor: "#f5f5f5", cursor: "pointer" });

    const downBtn = document.createElement("button");
    downBtn.innerHTML = "&#9660;";
    Object.assign(downBtn.style, { width: "24px", height: "24px", border: "1px solid #ccc", backgroundColor: "#f5f5f5", cursor: "pointer" });

    let currentValue = min;

    const setValue = (value) => {
      currentValue = Math.max(min, Math.min(max, value));
      valueDisplay.textContent = currentValue.toString().padStart(2, "0");
    };

    const getValue = () => currentValue;

    upBtn.addEventListener("click", (e) => { e.stopPropagation(); setValue(currentValue + 1); });
    downBtn.addEventListener("click", (e) => { e.stopPropagation(); setValue(currentValue - 1); });

    container.append(labelElement, upBtn, valueDisplay, downBtn);
    return { container, setValue, getValue };
  }

  formatTime(timeObj) {
    let format = this.props.timeFormat || "HH:mm:ss";
    format = format.replace(/HH/g, String(timeObj.hours).padStart(2, "0")).replace(/H/g, timeObj.hours);
    format = format.replace(/mm/g, String(timeObj.minutes).padStart(2, "0")).replace(/m/g, timeObj.minutes);
    format = format.replace(/ss/g, String(timeObj.seconds).padStart(2, "0")).replace(/s/g, timeObj.seconds);
    return format;
  }

  parseTimeString(timeString) {
    const parts = timeString.split(":");
    if (parts.length >= 2) {
      return {
        hours: parseInt(parts[0], 10) || 0,
        minutes: parseInt(parts[1], 10) || 0,
        seconds: (parts[2] ? parseInt(parts[2], 10) : 0) || 0,
      };
    }
    return null;
  }
}