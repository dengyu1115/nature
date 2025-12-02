import message from "../../util/Message.js";

/**
 * 组件基类
 */
export default class Base {
  constructor(props, styles, events, data) {
    this.props = props || {};
    this.styles = styles || {};
    this.events = events || {};
    this.data = data || {};
    this.children = [];
    // 数据回调方法
    this.callbackMap = Object.values(this.data).reduce((map, i) => {
      if (i.path && i.callback) {
        map[i.path] = i.callback;
      }
      return map;
    }, {});
    this.message = message;
  }

  // 通用的元素创建方法
  createElement(tag, defaultTag = "div") {
    const element = document.createElement(tag || defaultTag);
    this.setProps(element, this.props);
    this.setStyles(element, this.styles);
    this.setEvents(element, this.events);
    this.renderred = true;
    return element;
  }

  // 应用通用属性
  setProps(element, props) {
    // 处理className
    if (props.className) {
      element.className = props.className;
    }
    // 处理ID
    if (props.id) {
      element.id = props.id;
    }
  }

  // 设置元素样式属性的通用方法
  setStyles(element, styles) {
    element.style.display = "grid";

    // 直接设置的样式属性
    const directStyleProps = [
      "width",
      "height",
      "borderWidth",
      "borderColor",
      "borderStyle",
      "borderRadius",
      "overflow",
      "gridAutoFlow",
      "fontSize",
      "color",
      "backgroundColor",
      "fontWeight",
    ];

    directStyleProps.forEach((prop) => {
      if (styles[prop] !== undefined) {
        element.style[prop] = styles[prop];
      }
    });

    // 处理内容排布属性
    const alignH = styles.alignH || "start";
    const alignV = styles.alignV || "start";
    element.style.placeContent = alignV + " " + alignH;

    // 处理内外边距属性
    const spacingProps = [
      "margin",
      "marginTop",
      "marginRight",
      "marginBottom",
      "marginLeft",
      "padding",
      "paddingTop",
      "paddingRight",
      "paddingBottom",
      "paddingLeft",
    ];

    spacingProps.forEach((prop) => {
      if (styles[prop] !== undefined) {
        element.style[prop] = styles[prop];
      }
    });
  }

  setEvents(element, events) {
    // 添加点击事件处理
    if (events) {
      for (const [k, v] of Object.entries(events)) {
        if (!k || !v) {
          continue;
        }
        try {
          const func = new Function(v);
          element.addEventListener(k, (e) => {
            try {
              func.call(this);
            } catch (err) {
              this.message.error(err.message);
            }
          });
        } catch (e) {
          this.message.error("事件代码执行出错:" + e.message);
        }
      }
    }
  }

  // 通用的渲染子组件方法
  renderChildren(element) {
    this.children.forEach((child) => {
      element.appendChild(child.render());
    });
  }

  // 添加子组件
  addChild(child) {
    this.children.push(child);
    return this;
  }

  // 渲染方法（子类需要重写）
  render() {
    throw new Error("Render method must be implemented");
  }

  /**
   * 刷新方法，用于数据刷新时候回调
   * @param {*} path 数据路径
   */
  refresh(path) {
    if (!this.renderred) {
      return;
    }
    const callback = this.callbackMap[path];
    if (callback && this[callback] && typeof this[callback] === "function") {
      this[callback]();
    }
  }

  // 挂载到DOM元素
  mount(element) {
    element.innerHTML = "";
    element.appendChild(this.render());
    return this;
  }
}
