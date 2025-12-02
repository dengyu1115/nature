import Reactive from "../../util/Reactive.js";
import Base from "./Base.js";

// 模态框组件
export default class Modal extends Base {
  render() {
    // 创建包装容器
    const wrapper = document.createElement("div");
    wrapper.style.cssText = "display: contents;";
    // 创建遮罩层
    const overlay = document.createElement("div");
    overlay.style.cssText = `
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0, 0, 0, 0.3);
      z-index: 999;
      display: 'block'};
    `;
    // 创建模态框容器
    const element = this.createElement("div");
    // 设置模态框默认样式
    Object.assign(element.style, {
      position: "fixed",
      top: "50%",
      left: "50%",
      transform: "translate(-50%, -50%)",
      zIndex: "1000",
    });
    // 点击遮罩层关闭模态框
    const path = this.data.show?.path;
    overlay.addEventListener("click", (e) => {
      if (e.target === overlay) {
        element.style.display = "none";
        overlay.style.display = "none";
        if (path) {
          Reactive.set(data, path, false, this);
        }
      }
    });
    this.overlay = overlay;
    this.element = element;
    // 组装最终结构
    wrapper.appendChild(overlay);
    wrapper.appendChild(element);
    // 添加子组件到内容区域
    this.renderChildren(element);
    this.refreshShow();
    return wrapper;
  }

  refreshShow() {
    const path = this.data.show?.path;
    const show = path ? Reactive.get(data, path) : this.data.show?.data;
    if (show) {
      this.element.style.display = "grid";
      this.overlay.style.display = "block";
    } else {
      this.element.style.display = "none";
      this.overlay.style.display = "none";
    }
  }
}
