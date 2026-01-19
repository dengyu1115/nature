import Reactive from "../../util/Reactive.js";
import Base from "./Base.js";

export default class Select extends Base {
  render() {
    // 创建容器
    const container = this.createElement("div");
    Object.assign(container.style, {
      display: "block",
      overflow: "visible",
    });
    this.buildStructure();
    container.append(this.selector, this.dropdown);
    this.refreshOptions();
    return container;
  }

  buildStructure() {
    // 创建选择器显示区域
    this.selector = document.createElement("div");
    // 显示文本
    this.text = document.createElement("span");
    // 清空按钮
    this.clearBtn = document.createElement("span");
    this.clearBtn.innerHTML = "×";
    this.clearBtn.style.display = "none"; // 默认隐藏
    // 箭头图标
    this.arrow = document.createElement("span");
    // 使用回旋镖形状的 Unicode 字符
    this.arrow.innerHTML = "^";
    // 创建下拉列表
    this.dropdown = document.createElement("div");
    // 创建选项列表
    this.opList = document.createElement("ul");
    this.selector.append(this.text, this.clearBtn, this.arrow);
    this.dropdown.append(this.opList);
    this.text.textContent = this.opList.placeholder;
    Object.assign(this.selector.style, {
      display: "flex",
      alignItems: "center",
      height: "100%",
      justifyContent: "space-between",
      cursor: "pointer",
    });
    Object.assign(this.text.style, {
      overflow: "hidden",
      textOverflow: "ellipsis",
      whiteSpace: "nowrap",
      flex: "1",
      textAlign: "center",
    });
    Object.assign(this.clearBtn.style, {
      color: "#999",
      cursor: "pointer",
      fontWeight: "bold",
      display: "none",
    });
    Object.assign(this.arrow.style, {
      color: "#181717ff",
      transform: "rotate(180deg)",
    });
    Object.assign(this.dropdown.style, {
      position: "fixed",
      width: this.styles.width,
      transform: `translateX(-${this.styles.borderWidth})`,
      overflowY: "auto",
      border: `1px solid ${this.styles.borderColor}`,
      borderTop: "none",
      maxHeight: "100px",
      borderRadius: "0 0 4px 4px",
      zIndex: "1000",
      boxSizing: "border-box",
      display: "none",
    });
    Object.assign(this.opList.style, {
      listStyle: "none",
      margin: "0",
      padding: "0",
    });
    // 绑定事件
    this.selector.addEventListener("click", (e) => {
      e.stopPropagation();
      this.toggle();
    });
    // 清空按钮点击事件
    this.clearBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      this.clearValue();
    });
    document.addEventListener("click", (e) => {
      this.close();
    });
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
    this.opList.innerHTML = "";
    const last = this.options.length - 1;
    this.options.forEach((item, index) => {
      const op = document.createElement("li");
      const value = item.value;
      const label = item.label;
      op.textContent = label;
      Object.assign(op.style, {
        borderBottom: index < last ? "1px solid #f0f0f0" : "none",
        backgroundColor: "white",
      });
      if (item.disabled !== "true") {
        op.style.cursor = "pointer";
        // 点击选项
        op.addEventListener("click", (e) => {
          e.stopPropagation();
          this.toggleValue(value);
          this.updateUI();
          this.toggleOption();
        });
      }
      this.opList.appendChild(op);
    });
  }

  toggle() {
    this.isOpen ? this.close() : this.open();
  }

  open() {
    this.isOpen = true;
    this.dropdown.style.display = "block";
    this.arrow.style.transform = "rotate(0deg)";
  }

  close() {
    this.isOpen = false;
    this.dropdown.style.display = "none";
    this.arrow.style.transform = "rotate(180deg)";
  }

  updateOptions() {
    this.options = this.data.options?.data || this.props.options || [];
    const path = this.data.options?.path;
    // 如果有数据属性绑定input事件
    if (path) {
      const value = Reactive.get(data, path);
      if (!value) {
        return;
      }
      this.options = value;
    }
  }

  updateValue() {
    // 初始化为数组
    const multiple = this.props.multiple === "true";
    const path = this.data.value?.path;
    // 如果有数据属性绑定input事件
    if (path) {
      const value = Reactive.get(data, path);
      if (multiple) {
        this.value = value || [];
      } else {
        this.value = value ? [value] : [];
      }
    } else {
      const value = this.data.value?.data;
      if (multiple) {
        this.value = value || [];
      } else {
        this.value = value ? [value] : [];
      }
    }
  }

  toggleValue(value) {
    const val = this.value;
    const index = val.indexOf(value);
    const multiple = this.props.multiple === "true";
    if (index > -1) {
      val.splice(index, 1);
    } else {
      if (!multiple) {
        val.length = 0;
      }
      val.push(value);
    }
    const path = this.data.value?.path;
    if (path) {
      const v = multiple ? val : val.length > 0 ? val[0] : null;
      Reactive.set(data, path, v, this);
    }
  }

  toggleOption() {
    if (this.props.multiple !== "true") {
      this.close();
    }
    if (this.events.change) {
      try {
        new Function(this.events.change).call(this);
      } catch (error) {
        console.error(error);
      }
    }
  }

  clearValue() {
    const multiple = this.props.multiple === "true";
    const path = this.data.value?.path;
    
    if (multiple) {
      this.value = [];
    } else {
      this.value = [];
    }
    
    if (path) {
      const v = multiple ? [] : null;
      Reactive.set(data, path, v, this);
    }
    
    this.updateUI();
    
    if (this.events.change) {
      try {
        new Function(this.events.change).call(this);
      } catch (error) {
        console.error(error);
      }
    }
  }

  updateUI() {
    const options = this.options;
    const opList = this.opList.children;
    const values = new Set(this.value);
    for (let i = 0; i < options.length; i++) {
      const opt = options[i];
      const op = opList[i];
      if (values.has(opt.value)) {
        op.style.backgroundColor = "#e6f7ff";
      } else {
        op.style.backgroundColor = "white";
      }
    }
    const labels = options
      .filter((i) => values.has(i.value))
      .map((i) => i.label);
    this.text.textContent = labels.length
      ? labels.join(",")
      : this.props.placeholder;
    
    // 根据是否有选中值来控制清空按钮的显示/隐藏
    this.clearBtn.style.display = labels.length ? "block" : "none";
  }
}
