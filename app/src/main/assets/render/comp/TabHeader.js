import Reactive from "../../util/Reactive.js";
import Val from "../../util/Val.js";
import Base from "./Base.js";

/**
 * Tab页头组件
 */
export default class TabHeader extends Base {
  render() {
    const element = this.createElement("div");
    this.element = element;
    // 根据风格类型添加对应样式
    const styleType = this.props.styleType || "classic";
    element.classList.add(styleType);
    // 创建tab按钮
    this.tabs = this.props.tabs || [];
    this.tabs.forEach((tab, index) => {
      const btn = document.createElement("button");
      btn.textContent = tab.label || "";
      if (tab.disable === "true") {
        btn.disable = true;
      }
      this.setWidthHeight(btn);
      // 添加点击事件
      btn.addEventListener("click", () => {
        // 如果按钮被禁用，不执行任何操作
        if (tab.disable === "true") {
          return;
        }
        if (this.active == index) {
          return;
        }
        this.active = index;
        this.activeTab();
      });
      element.appendChild(btn);
    });
    this.refreshActive();
    return element;
  }

  refreshActive() {
    this.updateActive();
    this.activeTab();
  }

  /**
   * 获取激活tab下标
   * @returns
   */
  updateActive() {
    const path = this.data.active?.path;
    const active = path ? Reactive.get(data, path) : this.data.active?.data;
    this.active = active || 0;
  }

  /**
   * 激活TAB
   * @param {*} index
   * @returns
   */
  activeTab() {
    // 更新按钮状态
    Array.from(this.element.children).forEach((btn, idx) => {
      btn.classList.remove("active");
      if (idx == this.active) {
        btn.classList.add("active");
      }
    });
    // 如果有数据属性绑定input事件
    const path = this.data.active?.path;
    if (path) {
      Reactive.set(data, path, this.active, this);
    }
  }
  /**
   * 设置宽度高度
   * @param {*} element
   */
  setWidthHeight(element) {
    if (this.styles.gridAutoFlow == "column") {
      element.style.height = this.styles.height;
      element.style.width = Val.extract(this.styles.width)[1] * 0.1 + "px";
    } else {
      element.style.height = Val.extract(this.styles.height)[1] * 0.1 + "px";
      element.style.width = this.styles.width;
    }
  }
}
