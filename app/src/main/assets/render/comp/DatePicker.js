import Reactive from "../../util/Reactive.js";
import Base from "./Base.js";

export default class DatePicker extends Base {
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
    // 初始化当前月份
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
    const today = this.value ? this.parseDate(this.value) : new Date();
    this.year = today.getFullYear();
    this.month = today.getMonth();
  }

  buildStructure() {
    // 创建日期输入框
    this.input = document.createElement("input");
    this.input.type = "text";
    this.input.placeholder = this.props.placeholder || "请选择日期";
    // 创建清除按钮
    this.clearBtn = document.createElement("span");
    this.clearBtn.innerHTML = "×"; // ×符号
    Object.assign(this.clearBtn.style, {
      height: "100%",
      display: "none", // 默认隐藏
      alignItems: "center",
      justifyContent: "center",
      cursor: "pointer",
      color: "#999",
    });
    // 创建日历图标
    this.icon = document.createElement("span");
    this.icon.innerHTML = "📅"; // 日历emoji图标
    Object.assign(this.input.style, {
      flex: "1",
      width: "calc(100% - 24px)",
      height: "100%",
      border: "none",
      outline: "none",
      padding: "0 5px",
      boxSizing: "border-box",
    });
    this.input.readOnly = true;
    Object.assign(this.icon.style, {
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
      this.clearDate();
    });
    this.icon.addEventListener("click", (e) => {
      e.stopPropagation();
      this.showPicker();
    });
    // 点击其他地方关闭日期选择器
    document.addEventListener("click", (e) => {
      if (this.picker && this.picker.parentNode) {
        this.hidePicker();
      }
    });
  }

  // 清除日期
  clearDate() {
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
    // 定位日期选择器
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
    // 创建日期选择器容器
    this.picker = document.createElement("div");
    this.picker.classList.add("component");
    Object.assign(this.picker.style, {
      position: "absolute",
      width: "230px",
      backgroundColor: "white",
      border: "1px solid #ccc",
      borderRadius: "4px",
      boxShadow: "0 2px 10px rgba(0,0,0,0.1)",
      zIndex: "1000",
      display: "none",
      padding: "5px",
    });
    // 创建年月导航
    const header = document.createElement("div");
    Object.assign(header.style, {
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center",
      marginBottom: "10px",
    });
    this.monthText = document.createElement("span");
    this.monthText.style.fontWeight = "bold";
    header.append(
      this.createBtn("&lt;&lt;", () => this.year--),
      this.createBtn("&lt;", () => {
        this.month--;
        if (this.month < 0) {
          this.month = 11;
          this.year--;
        }
      }),
      this.monthText,
      this.createBtn("&gt;", () => {
        this.month++;
        if (this.month > 11) {
          this.month = 0;
          this.year++;
        }
      }),
      this.createBtn("&gt;&gt;", () => this.year++)
    );
    // 创建星期标题
    const weekdays = document.createElement("div");
    weekdays.style.display = "grid";
    weekdays.style.gridTemplateColumns = "repeat(7, 1fr)";
    weekdays.style.textAlign = "center";
    weekdays.style.fontWeight = "bold";
    weekdays.style.marginBottom = "5px";
    ["日", "一", "二", "三", "四", "五", "六"].forEach((day) => {
      const dayEl = document.createElement("div");
      dayEl.textContent = day;
      weekdays.appendChild(dayEl);
    });
    // 创建日期网格
    this.dateGrid = document.createElement("div");
    Object.assign(this.dateGrid.style, {
      display: "grid",
      gridTemplateColumns: "repeat(7, 1fr)",
      gap: "2px",
    });
    this.picker.append(header, weekdays, this.dateGrid);
    this.renderCalendar();
  }

  createBtn(name, handle) {
    const btn = document.createElement("button");
    btn.innerHTML = name;
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      handle();
      this.renderCalendar();
    });
    return btn;
  }

  renderCalendar() {
    // 更新年月显示
    this.monthText.textContent = `${this.year}年${String(
      this.month + 1
    ).padStart(2, "0")}月`;
    // 清空日期网格
    this.dateGrid.innerHTML = "";
    // 获取当月第一天和最后一天
    const firstDay = new Date(this.year, this.month, 1);
    const lastDay = new Date(this.year, this.month + 1, 0);
    // 获取第一天是星期几（0-6，0表示周日）
    const firstDayOfWeek = firstDay.getDay();
    // 获取上个月的最后一天
    const prevMonthLastDay = new Date(this.year, this.month, 0).getDate();
    // 创建上个月的日期
    for (let i = firstDayOfWeek - 1; i >= 0; i--) {
      const dateEl = document.createElement("div");
      dateEl.textContent = prevMonthLastDay - i;
      Object.assign(dateEl.style, {
        padding: "5px",
        textAlign: "center",
        color: "#ccc",
        cursor: "default",
      });
      this.dateGrid.appendChild(dateEl);
    }
    // 创建当月的日期
    const sldDate = this.value ? this.parseDate(this.value) : new Date();
    for (let i = 1; i <= lastDay.getDate(); i++) {
      const dateEl = document.createElement("div");
      dateEl.textContent = i;
      Object.assign(dateEl.style, {
        padding: "5px",
        textAlign: "center",
        cursor: "pointer",
        borderRadius: "4px",
      });
      // 高亮今天
      const isSelected =
        this.year === sldDate.getFullYear() &&
        this.month === sldDate.getMonth() &&
        i === sldDate.getDate();
      if (isSelected) {
        Object.assign(dateEl.style, {
          backgroundColor: "#1890ff",
          color: "white",
          fontWeight: "bold",
        });
      }
      // 选择日期事件
      dateEl.addEventListener("click", (e) => {
        e.stopPropagation();
        this.selectDate(this.year, this.month, i);
      });
      this.dateGrid.appendChild(dateEl);
    }
    // 计算还需要多少天来填满网格
    const totalCells = 42; // 6行7列
    const remainingCells = totalCells - (firstDayOfWeek + lastDay.getDate());
    // 创建下个月的日期
    for (let i = 1; i <= remainingCells; i++) {
      const dateEl = document.createElement("div");
      dateEl.textContent = i;
      Object.assign(dateEl.style, {
        padding: "5px",
        textAlign: "center",
        color: "#ccc",
        cursor: "default",
      });
      this.dateGrid.appendChild(dateEl);
    }
  }

  selectDate(year, month, day) {
    const date = new Date(year, month, day);
    const value = this.formatDate(date);
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
    // 重新渲染日历以更新高亮效果
    this.renderCalendar();
    // 隐藏日期选择器
    this.hidePicker();
  }

  formatDate(date) {
    // 获取用户配置的日期格式，默认为 yyyy-MM-dd
    let format = this.props.dateFormat || "yyyy-MM-dd";
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    // 替换年份
    format = format.replace(/yyyy/g, year);
    format = format.replace(/yy/g, String(year).slice(-2));
    // 替换月份
    format = format.replace(/MM/g, String(month).padStart(2, "0"));
    format = format.replace(/M/g, month);
    // 替换日期
    format = format.replace(/dd/g, String(day).padStart(2, "0"));
    format = format.replace(/d/g, day);
    return format;
  }

  parseDate(dateString) {
    // 移除所有非数字字符，提取纯数字
    const digits = dateString.replace(/[^0-9]/g, "");
    const year = parseInt(digits.substring(0, 4), 10);
    const month = parseInt(digits.substring(4, 6), 10) - 1;
    const day = parseInt(digits.substring(6, 8), 10);
    return new Date(year, month, day);
  }
}
