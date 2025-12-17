import Reactive from "../../util/Reactive.js";
import Val from "../../util/Val.js";
import Base from "./Base.js";

/**
 * 表格组件
 * 用于渲染具有多级表头、固定行列、排序和虚拟滚动功能的表格
 */
export default class Table extends Base {
  /**
   * 渲染表格组件
   * @returns {HTMLElement} 表格容器元素
   */
  render() {
    const element = this.createElement("div");
    this.element = element;
    element.style.gridAutoFlow = "column";
    element.style.border = "none";
    // 添加滚动事件监听器
    element.addEventListener("scroll", this.onScroll.bind(this));
    // 构建表格结构
    this.buildTableStructure();
    element.appendChild(this.table);
    // 更新表内容
    this.refreshTable();
    return element;
  }

  /**
   * 构建表格结构
   */
  buildTableStructure() {
    this.table = document.createElement("table");
    this.table.style.display = "table";
    this.table.style.borderSpacing = "0";
    this.thead = document.createElement("thead");
    this.tbody = document.createElement("tbody");
    this.table.appendChild(this.thead);
    this.table.appendChild(this.tbody);
  }

  refreshTable() {
    this.updateColumns();
    this.updateThead();
    this.refreshTbody();
  }

  refreshTbody() {
    this.updateRows();
    this.element.scrollTop = 0;
    this.updateTbody();
  }

  /**
   * 更新列数据
   */
  updateColumns() {
    // 相关属性计算
    this.level = this.calcHeadLevel(this.props.columns, 0);
    this.bodyRows = parseInt(this.props.bodyRows);
    const rowCount = this.bodyRows + 1;
    const tableHeight = Val.extract(this.styles.height)[1];
    this.rowHeight = tableHeight / rowCount;
    this.headerRh = Val.extract(this.props.fontSize || "14px")[1];
    const headerHeight = this.headerRh * this.level + this.level + 1;
    if (headerHeight > this.rowHeight) {
      this.rowHeight = (tableHeight - headerHeight) / this.bodyRows;
    } else {
      this.headerRh = this.rowHeight / this.level;
    }
    this.buttonMap = this.buildButtonMap();
    this.bindingMap = this.buildBindingMap();
    this.calcColWidth(this.props.columns);
    this.columns = [];
    this.appendColumn(this.columns, this.props.columns, 0);
    if (!this.columns.length) {
      return;
    }
    // 处理点击和格式化函数
    const columns = this.columns[this.columns.length - 1];
    columns.forEach((col) => {
      if (col.click) {
        try {
          col.clickFunc = new Function(["value", "datum"], col.click);
        } catch (e) {
          this.message.error(col.label + "点击事件编译出错:" + e.message);
        }
      }
      if (col.format) {
        try {
          col.formatFunc = new Function(["value", "datum"], col.format);
        } catch (e) {
          this.message.error(col.label + "格式化编译出错:" + e.message);
        }
      }
    });
    this.calcSticky();
  }

  /**
   * 更新行数据
   */
  updateRows() {
    if (this.data.rows?.path) {
      const data = Reactive.get(window.data, this.data.rows?.path);
      this.rows = data || [];
    } else if (this.data.rows?.data) {
      this.rows = this.data.rows?.data;
    } else {
      this.rows = [];
    }
    // 虚拟滚动相关属性
    this.startIndex = 0;
    this.endIndex = Math.min(this.bodyRows, this.rows.length);
  }

  /**
   * 滚动事件处理
   * @param {Event} event - 滚动事件
   */
  onScroll(event) {
    const container = event.target;
    const scrollTop = container.scrollTop;
    const rowHeight = this.rowHeight;
    const newIndex = Math.floor(scrollTop / rowHeight);
    if (newIndex === this.startIndex) {
      return;
    }
    const dataLength = this.rows.length;
    const cacheRows = 5;
    this.startIndex = Math.max(newIndex - cacheRows, 0);
    this.endIndex = Math.min(
      this.startIndex + this.bodyRows + cacheRows * 2,
      dataLength
    );
    this.updateTbody();
  }

  /**
   * 设置单元格边框样式
   * @param {HTMLElement} cell - 单元格元素
   * @param {Object} styles - 样式对象
   * @param {boolean} firstRow - 是否为第一行
   * @param {boolean} firstCol - 是否为第一列
   */
  setBorderStyle(cell, styles, firstRow, firstCol) {
    ["Width", "Color", "Style"].forEach((prop) => {
      this.setCellBorderStyle(cell, styles, firstRow, firstCol, prop);
    });
  }

  /**
   * 设置单元格特定边框属性
   * @param {HTMLElement} cell - 单元格元素
   * @param {Object} styles - 样式对象
   * @param {boolean} firstRow - 是否为第一行
   * @param {boolean} firstCol - 是否为第一列
   * @param {string} prop - 边框属性名 (Width/Color/Style)
   */
  setCellBorderStyle(cell, styles, firstRow, firstCol, prop) {
    const style = styles["border" + prop];
    if (style) {
      if (firstRow) {
        cell.style["borderTop" + prop] = style;
      }
      if (firstCol) {
        cell.style["borderLeft" + prop] = style;
      }
      cell.style["borderBottom" + prop] = style;
      cell.style["borderRight" + prop] = style;
    }
  }

  /**
   * 设置单元格通用样式
   * @param {HTMLElement} cell - 单元格元素
   * @param {Object} styles - 样式配置对象
   * @param {number} height - 元素高度
   */
  setCellCommonStyle(cell, styles, height) {
    if (styles.width) {
      cell.style.width = styles.width;
      cell.style.maxWidth = styles.width;
      cell.style.minWidth = styles.width;
      cell.style.height = height + "px";
      cell.style.lineHeight = 1;
      cell.style.whiteSpace = "nowrap";
    }
  }

  setCellContent(td, column, datum) {
    const prop = column.prop;
    if (Object.keys(this.bindingMap).includes(prop)) {
      const buttons =
        (Object.keys(datum).includes(prop)
          ? (datum[prop] || "").split(",")
          : this.bindingMap[prop]) || [];
      buttons.forEach((i) => {
        const button = this.buttonMap[i];
        if (!button) {
          return;
        }
        td.appendChild(this.createButton(button, datum));
      });
    } else {
      const func = column.formatFunc;
      if (func) {
        try {
          td.textContent = func.call(this, datum[prop], datum);
        } catch (err) {
          this.message.error(err.message);
        }
      } else {
        td.textContent = datum[prop];
      }
    }
  }

  setCellClick(td, column, datum) {
    const prop = column.prop;
    const func = column.clickFunc;
    if (func) {
      td.style.cursor = "pointer";
      td.addEventListener("click", (e) => {
        try {
          func.call(this, datum[prop], datum);
        } catch (err) {
          this.message.error(err.message);
        }
      });
    }
  }

  /**
   * 设置单元格粘性定位
   * @param {HTMLElement} cell - 单元格元素
   * @param {string} prop - 定位属性 (top/bottom/left/right)
   * @param {string} value - 定位值
   */
  setCellSticky(cell, prop, value, zIndex) {
    if (prop) {
      cell.style.position = "sticky";
      cell.style.zIndex = zIndex;
      cell.style[prop] = value;
    }
  }

  /**
   * 计算表头最大层级数
   * @param {Array} columns - 列配置数组
   * @param {number} level - 当前层级
   * @returns {number} 最大层级数
   */
  calcHeadLevel(columns, level) {
    if (columns && columns.length > 0) {
      level++;
      for (let i = 0; i < columns.length; i++) {
        const num = this.calcHeadLevel(columns[i].children, level);
        if (num > level) {
          return num;
        }
      }
    }
    return level;
  }

  /**
   * 递归添加列到指定层级
   * @param {Array} list - 列集合
   * @param {Array} columns - 当前列配置
   * @param {number} curr - 当前层级
   */
  appendColumn(list, columns, curr) {
    if (!(columns && columns.length > 0)) {
      return;
    }
    columns.forEach((col) => {
      const level = this.level - this.calcHeadLevel(col.children, 0) - 1;
      for (let i = curr; i < level + 1; i++) {
        if (!list[i]) {
          list[i] = [];
        }
        list[i].push(col);
      }
      this.appendColumn(list, col.children, curr + 1);
    });
  }

  /**
   * 构建表头元素
   */
  updateThead() {
    this.thead.innerHTML = "";
    for (let i = 0; i < this.level; i++) {
      const tr = document.createElement("tr");
      this.thead.appendChild(tr);
    }
    if (this.props.stickyHeader === "true") {
      this.thead.style.position = "sticky";
      this.thead.style.top = "0px";
      this.thead.style.zIndex = "101";
    }
    this.buildThs(this.props.columns, 0, 0);
  }

  /**
   * 构建表头单元格
   * @param {Array} columns - 列配置数组
   * @param {number} level - 当前层级
   * @param {number} index - 列索引
   */
  buildThs(columns, level, index) {
    // 遍历所有列
    columns.forEach((column) => {
      const th = this.buildTh(column, level, index);
      th.style.backgroundColor = this.props.headerColor;
      // 有下级则递归处理
      if (column.children && column.children.length > 0) {
        this.buildThs(column.children, level + 1, index);
      } else {
        const rowspan = this.level - level;
        // 层级不是最下层说明需要合并行
        if (rowspan > 1) {
          th.setAttribute("rowspan", rowspan);
        }
      }
      this.setCellSticky(th, column.sticky, column[column.sticky], "101");
      this.thead.children[level].appendChild(th);
      index++;
    });
  }

  /**
   * 构建数据单元格
   */
  updateTbody() {
    // 清空并添加新内容
    this.tbody.innerHTML = "";
    if (!this.rows || this.rows.length === 0) {
      return;
    }
    const rowHeight = this.rowHeight;
    const startIndex = this.startIndex;
    const endIndex = this.endIndex;
    const dataLength = this.rows.length;
    if (startIndex > 0) {
      const tr = document.createElement("tr");
      tr.style.height = rowHeight * startIndex + "px";
      this.tbody.appendChild(tr);
    }
    const columns = this.columns[this.columns.length - 1];
    // 使用虚拟滚动，只渲染可见行
    for (let i = startIndex; i < endIndex; i++) {
      const datum = this.rows[i];
      const tr = document.createElement("tr");
      columns.forEach((column, idxCol) => {
        const td = document.createElement("td");
        this.setBorderStyle(td, this.styles, false, idxCol === 0);
        this.setCellCommonStyle(td, column, this.rowHeight);
        td.style.textAlign = column.alignTd;
        td.style.backgroundColor = this.props.bodyColor;
        this.setCellContent(td, column, datum);
        this.setCellSticky(td, column.sticky, column[column.sticky], "100");
        this.setCellClick(td, column, datum);
        tr.appendChild(td);
      });
      this.tbody.appendChild(tr);
    }
    if (endIndex < dataLength) {
      const tr = document.createElement("tr");
      tr.style.height = rowHeight * (dataLength - endIndex) + "px";
      this.tbody.appendChild(tr);
    }
  }

  /**
   * 构建表头单元格
   * @param {Object} column - 列配置
   * @param {number} level - 当前层级
   * @param {number} index - 列索引
   * @returns {HTMLTableCellElement} 表头单元格元素
   */
  buildTh(column, level, index) {
    const th = document.createElement("th");
    this.setBorderStyle(th, this.styles, level === 0, index === 0);
    const colspan = this.calcColSpan(column);
    // 要合并列说明不是最底层表头
    if (colspan > 1) {
      th.setAttribute("colspan", colspan);
    }
    this.setCellCommonStyle(th, column, this.headerRh);
    th.textContent = column.label;
    th.style.textAlign = column.alignTh;
    this.addSort(column, th);
    return th;
  }

  /**
   * 计算行合并数
   * @param {number} level - 当前层级
   * @returns {number} 需要合并的行数
   */
  calcRowSpan(level) {
    return this.level - level;
  }

  /**
   * 计算列合并数
   * @param {Object} column - 列配置
   * @returns {number} 需要合并的列数
   */
  calcColSpan(column) {
    if (!(column.children && column.children.length > 0)) {
      return 1;
    }
    let total = 0;
    column.children.forEach((col) => {
      total += this.calcColSpan(col);
    });
    return total;
  }

  /**
   * 计算列宽度
   * @param {Array} columns - 列配置数组
   */
  calcColWidth(columns) {
    if (!(columns && columns.length > 0)) {
      return;
    }
    columns.forEach((col) => {
      if (col.children && col.children.length > 0) {
        this.calcColWidth(col.children);
        let width = 0;
        col.children.forEach((i) => {
          width += parseFloat(Val.extract(i.width)[1]);
        });
        col.width = width + "px";
      }
    });
  }

  /**
   * 计算粘性定位相关属性
   */
  calcSticky() {
    // 下级固定属性和上级的保持一致
    this.props.columns.forEach((col) => {
      this.calcColSticky(col.children, col.sticky);
    });
    // 分级计算表头定位位置
    this.columns.forEach((cols, idx) => {
      let left = 0;
      for (let i = 0; i < cols.length; i++) {
        const col = cols[i];
        if (col.sticky === "left") {
          col.left = left + "px";
          left += parseFloat(Val.extract(col.width)[1]);
        }
      }
      let right = 0;
      for (let i = cols.length - 1; i >= 0; i--) {
        const col = cols[i];
        if (col.sticky === "right") {
          col.right = right + "px";
          right += parseFloat(Val.extract(col.width)[1]);
        }
      }
    });
  }

  /**
   * 递归设置子列的粘性定位属性
   * @param {Array} columns - 子列配置数组
   * @param {string} sticky - 粘性定位方向
   */
  calcColSticky(columns, sticky) {
    if (!(columns && columns.length > 0)) {
      return;
    }
    columns.forEach((col) => {
      col.sticky = sticky;
      this.calcColSticky(col.children, sticky);
    });
  }

  /**
   * 为可排序列添加排序功能
   * @param {Object} col - 列配置
   * @param {HTMLTableCellElement} th - 表头单元格元素
   */
  addSort(col, th) {
    if (col.sort !== "true") {
      return;
    }
    th.style.cursor = "pointer";
    th.addEventListener("click", () => {
      this.sort(col);
    });
  }

  /**
   * 对表格数据执行排序操作
   * @param {Object} col - 列配置
   */
  sort(col) {
    // 找出列的排序字段
    const props = [];
    this.collectSort(props, col);
    if (props.length == 0) {
      return;
    }
    col.sortOrder = col.sortOrder === "asc" ? "desc" : "asc";
    this.rows.sort((a, b) => {
      for (let i = 0; i < props.length; i++) {
        const prop = props[i];
        const va = a[prop];
        const vb = b[prop];
        // 空值处理
        if (va == null) {
          return col.sortOrder == "asc" ? -1 : 1;
        }
        if (vb == null) {
          return col.sortOrder == "asc" ? 1 : -1;
        }
        if (va > vb) {
          return col.sortOrder == "asc" ? 1 : -1;
        }
        if (va < vb) {
          return col.sortOrder == "asc" ? -1 : 1;
        }
      }
      return 0;
    });
    this.updateTbody();
  }

  /**
   * 收集需要排序的字段
   * @param {Array} props - 字段名数组
   * @param {Object} col - 列配置
   */
  collectSort(props, col) {
    if (!(col.children && col.children.length > 0)) {
      props.push(col.prop);
    } else {
      col.children.forEach((child) => {
        this.collectSort(props, child);
      });
    }
  }

  buildBindingMap() {
    const bindings = this.props.bindings || [];
    const bindingMap = {};
    bindings.forEach((i) => {
      if (!i.prop || !i.buttons) {
        return;
      }
      bindingMap[i.prop] = i.buttons.split(",");
    });
    return bindingMap;
  }

  buildButtonMap() {
    const buttons = this.props.buttons || [];
    const buttonMap = {};
    buttons.forEach((i) => {
      if (!i.prop) {
        return;
      }
      buttonMap[i.prop] = i;
    });
    return buttonMap;
  }

  createButton(button, datum) {
    const btn = document.createElement("button");
    btn.classList.add("component", "table-btn", button.style);
    btn.textContent = button.label;
    if (button.click) {
      try {
        const func = new Function(["datum"], button.click);
        btn.addEventListener("click", (e) => {
          try {
            func.call(this, datum);
          } catch (err) {
            this.message.error(err.message);
          }
        });
      } catch (e) {
        this.message.error("事件代码执行出错:" + e.message);
      }
    }
    return btn;
  }
}
