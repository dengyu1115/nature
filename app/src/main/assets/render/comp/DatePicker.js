import Pickerable from "./Pickerable.js";
import Base from "./Base.js";

export default class DatePicker extends Pickerable(Base) {
  get _placeholder() { return "请选择日期"; }
  get _iconHtml() { return "📅"; }

  _afterRefreshValue() {
    const today = this.value ? this.parseDate(this.value) : new Date();
    this.year = today.getFullYear();
    this.month = today.getMonth();
  }

  _afterSelect() {
    this.renderCalendar();
  }

  selectDate(year, month, day) {
    this._select(this.formatDate(new Date(year, month, day)));
  }

  createPicker() {
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
      this.createBtn("&lt;", () => { this.month--; if (this.month < 0) { this.month = 11; this.year--; } }),
      this.monthText,
      this.createBtn("&gt;", () => { this.month++; if (this.month > 11) { this.month = 0; this.year++; } }),
      this.createBtn("&gt;&gt;", () => this.year++)
    );

    const weekdays = document.createElement("div");
    Object.assign(weekdays.style, {
      display: "grid",
      gridTemplateColumns: "repeat(7, 1fr)",
      textAlign: "center",
      fontWeight: "bold",
      marginBottom: "5px",
    });
    ["日", "一", "二", "三", "四", "五", "六"].forEach((day) => {
      const dayEl = document.createElement("div");
      dayEl.textContent = day;
      weekdays.appendChild(dayEl);
    });

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
    this.monthText.textContent = `${this.year}年${String(this.month + 1).padStart(2, "0")}月`;
    this.dateGrid.innerHTML = "";

    const firstDay = new Date(this.year, this.month, 1);
    const lastDay = new Date(this.year, this.month + 1, 0);
    const firstDayOfWeek = firstDay.getDay();
    const prevMonthLastDay = new Date(this.year, this.month, 0).getDate();

    for (let i = firstDayOfWeek - 1; i >= 0; i--) {
      const dateEl = document.createElement("div");
      dateEl.textContent = prevMonthLastDay - i;
      Object.assign(dateEl.style, { padding: "5px", textAlign: "center", color: "#ccc", cursor: "default" });
      this.dateGrid.appendChild(dateEl);
    }

    const sldDate = this.value ? this.parseDate(this.value) : new Date();
    for (let i = 1; i <= lastDay.getDate(); i++) {
      const dateEl = document.createElement("div");
      dateEl.textContent = i;
      Object.assign(dateEl.style, { padding: "5px", textAlign: "center", cursor: "pointer", borderRadius: "4px" });

      const isSelected = this.year === sldDate.getFullYear() && this.month === sldDate.getMonth() && i === sldDate.getDate();
      if (isSelected) {
        Object.assign(dateEl.style, { backgroundColor: "#1890ff", color: "white", fontWeight: "bold" });
      }

      dateEl.addEventListener("click", (e) => {
        e.stopPropagation();
        this.selectDate(this.year, this.month, i);
      });
      this.dateGrid.appendChild(dateEl);
    }

    const totalCells = 42;
    const remainingCells = totalCells - (firstDayOfWeek + lastDay.getDate());
    for (let i = 1; i <= remainingCells; i++) {
      const dateEl = document.createElement("div");
      dateEl.textContent = i;
      Object.assign(dateEl.style, { padding: "5px", textAlign: "center", color: "#ccc", cursor: "default" });
      this.dateGrid.appendChild(dateEl);
    }
  }

  formatDate(date) {
    let format = this.props.dateFormat || "yyyy-MM-dd";
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    format = format.replace(/yyyy/g, year).replace(/yy/g, String(year).slice(-2));
    format = format.replace(/MM/g, String(month).padStart(2, "0")).replace(/M/g, month);
    format = format.replace(/dd/g, String(day).padStart(2, "0")).replace(/d/g, day);
    return format;
  }

  parseDate(dateString) {
    const digits = dateString.replace(/[^0-9]/g, "");
    return new Date(parseInt(digits.substring(0, 4), 10), parseInt(digits.substring(4, 6), 10) - 1, parseInt(digits.substring(6, 8), 10));
  }
}