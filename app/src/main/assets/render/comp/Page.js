import EventUtil from "../../util/EventUtil.js";
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
    const func = EventUtil.compileAsync(this.events.load);
    if (func) {
      func.call(this);
    }
  }

  render() {
    const element = this.createElement("div");
    // 添加子组件
    this.renderChildren(element);
    return element;
  }
}
