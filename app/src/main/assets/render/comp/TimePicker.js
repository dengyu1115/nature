import Reactive from "../../util/Reactive.js";
import Base from "./Base.js";

export default class TimePicker extends Base {
  render() {
    // 创建容器
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
    // 初始化时间选择器结构
    this.buildStructure();
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
      // 显示清除按钮
      this.clearBtn.style.display = "flex";
    } else {
      this.clearBtn.style.display = "none";
    }
  }

  buildStructure() {
    // 创建时间输入框
    this.input = document.createElement("input");
    this.input.type = "text";
    this.input.placeholder = this.props.placeholder || "请选择时间";
    // 创建清除按钮
    this.clearBtn = document.createElement("span");
    this.clearBtn.innerHTML = "×"; // ×符号
    Object.assign(this.clearBtn.style, {
      width: "12px",
      height: "100%",
      display: "none", // 默认隐藏
      alignItems: "center",
      justifyContent: "center",
      cursor: "pointer",
      color: "#999",
    });
    // 创建时钟图标
    this.icon = document.createElement("span");
    this.icon.innerHTML = "&#128336;"; // 时钟emoji图标
    Object.assign(this.input.style, {
      flex: "1",
      width: "calc(100% - 28px)",
      height: "100%",
      border: "none",
      outline: "none",
      padding: "0 5px",
      boxSizing: "border-box",
    });
    this.input.readOnly = true;
    Object.assign(this.icon.style, {
      width: "16px",
      height: "100%",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      cursor: "pointer",
    });
    // 绑定事件
    this.input.addEventListener("click", (e) => {
      e.stopPropagation();
      this.showPicker();
    });
    this.clearBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      this.clearTime();
    });
    this.icon.addEventListener("click", (e) => {
      e.stopPropagation();
      this.showPicker();
    });
    // 点击其他地方关闭时间选择器
    document.addEventListener("click", (e) => {
      if (this.picker && this.picker.parentNode) {
        this.hidePicker();
      }
    });
  }

  // 清除时间
  clearTime() {
    const changed = this.value !== null;
    this.value = null;
    this.input.value = "";
    this.clearBtn.style.display = "none";

    // 触发change事件
    if (changed) {
      this.changeHandle?.call(this);
      const path = this.data.value?.path;
      if (path) {
        Reactive.set(data, path, this.value, this);
      }
    }
  }

  showPicker() {
    if (!this.picker) {
      this.createPicker();
    }
    // 定位时间选择器
    const rect = this.input.getBoundingClientRect();
    Object.assign(this.picker.style, {
      position: "fixed",
      top: rect.bottom + "px",
      left: rect.left + "px",
      display: "block",
    });
    // 添加到body中
    document.body.appendChild(this.picker);
  }

  hidePicker() {
    if (this.picker && this.picker.parentNode) {
      this.picker.parentNode.removeChild(this.picker);
    }
  }

  createPicker() {
    // 创建时间选择器容器
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

    // 创建时间选择区域
    const timeContainer = document.createElement("div");
    Object.assign(timeContainer.style, {
      display: "flex",
      justifyContent: "space-around",
      alignItems: "center",
      height: "120px",
    });

    // 创建小时选择器
    this.hourPicker = this.createNumberSelector(0, 23, "时");
    // 创建分钟选择器
    this.minutePicker = this.createNumberSelector(0, 59, "分");
    // 创建秒选择器
    this.secondPicker = this.createNumberSelector(0, 59, "秒");

    timeContainer.append(
      this.hourPicker.container,
      this.minutePicker.container,
      this.secondPicker.container
    );

    // 创建操作按钮区域
    const buttonContainer = document.createElement("div");
    Object.assign(buttonContainer.style, {
      display: "flex",
      justifyContent: "flex-end",
      marginTop: "10px",
      paddingTop: "10px",
      borderTop: "1px solid #eee",
    });

    // 创建确定按钮
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

    confirmBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      this.selectTime();
    });

    buttonContainer.appendChild(confirmBtn);

    this.picker.append(timeContainer, buttonContainer);

    // 如果有当前值，则设置初始选择
    if (this.value) {
      const parts = this.parseTimeString(this.value);
      if (parts) {
        this.hourPicker.setValue(parts.hours);
        this.minutePicker.setValue(parts.minutes);
        this.secondPicker.setValue(parts.seconds);
      }
    }
  }

  createNumberSelector(min, max, label) {
    const container = document.createElement("div");
    Object.assign(container.style, {
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
    });

    const labelElement = document.createElement("div");
    labelElement.textContent = label;
    Object.assign(labelElement.style, {
      marginBottom: "5px",
      color: "#666",
    });

    const valueDisplay = document.createElement("div");
    valueDisplay.textContent = min.toString().padStart(2, "0");
    Object.assign(valueDisplay.style, {
      fontWeight: "bold",
      margin: "5px 0",
      minWidth: "40px",
      textAlign: "center",
    });
    const upBtn = document.createElement("button");
    upBtn.innerHTML = "&#9650;";
    Object.assign(upBtn.style, {
      width: "24px",
      height: "24px",
      border: "1px solid #ccc",
      backgroundColor: "#f5f5f5",
      cursor: "pointer",
    });

    const downBtn = document.createElement("button");
    downBtn.innerHTML = "&#9660;";
    Object.assign(downBtn.style, {
      width: "24px",
      height: "24px",
      border: "1px solid #ccc",
      backgroundColor: "#f5f5f5",
      cursor: "pointer",
    });

    let currentValue = min;

    const setValue = (value) => {
      currentValue = Math.max(min, Math.min(max, value));
      valueDisplay.textContent = currentValue.toString().padStart(2, "0");
    };

    const getValue = () => currentValue;

    upBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      setValue(currentValue + 1);
    });

    downBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      setValue(currentValue - 1);
    });

    container.append(labelElement, upBtn, valueDisplay, downBtn);

    return {
      container,
      setValue,
      getValue,
    };
  }

  selectTime() {
    const hours = this.hourPicker.getValue();
    const minutes = this.minutePicker.getValue();
    const seconds = this.secondPicker.getValue();

    const timeObj = { hours, minutes, seconds };
    const value = this.formatTime(timeObj);

    // 更新输入框的值
    const changed = this.value !== value;
    // 触发change事件
    if (changed) {
      this.value = value;
      this.input.value = value;
      this.clearBtn.style.display = "flex";
      this.changeHandle?.call(this);
      const path = this.data.value?.path;
      if (path) {
        Reactive.set(data, path, this.value, this);
      }
    }
    // 隐藏时间选择器
    this.hidePicker();
  }

  formatTime(timeObj) {
    // 获取用户配置的时间格式，默认为 HH:mm:ss
    let format = this.props.timeFormat || "HH:mm:ss";
    const hours = timeObj.hours;
    const minutes = timeObj.minutes;
    const seconds = timeObj.seconds;

    // 替换小时
    format = format.replace(/HH/g, String(hours).padStart(2, "0"));
    format = format.replace(/H/g, hours);

    // 替换分钟
    format = format.replace(/mm/g, String(minutes).padStart(2, "0"));
    format = format.replace(/m/g, minutes);

    // 替换秒
    format = format.replace(/ss/g, String(seconds).padStart(2, "0"));
    format = format.replace(/s/g, seconds);

    return format;
  }

  parseTimeString(timeString) {
    // 简单解析时间字符串，支持 HH:mm:ss 格式
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
