import Reactive from "../../util/Reactive.js";
import Base from "./Base.js";

/**
 * Page 页面组件
 */
export default class Page extends Base {
  constructor(props = {}, styles = {}, events = {}, data = null) {
    super(props, styles, events, data);
    const globalData = this.data.global?.data || {};
    // 数据绑定至window对象
    window.data = Reactive.proxy(globalData);
    if (this.events && this.events.load) {
      try {
        const func = new Function(this.events.load);
        try {
          func.call(this);
        } catch (err) {
          message.error(err.message);
        }
      } catch (e) {
        message.error("事件代码编译出错:" + e.message);
      }
    }
  }

  render() {
    const element = this.createElement("div");
    // 添加子组件
    this.renderChildren(element);
    return element;
  }
}
