import Val from "../util/Val.js";
import Button from "./comp/Button.js";
import Checkbox from "./comp/Checkbox.js";
import Container from "./comp/Container.js";
import Input from "./comp/Input.js";
import KlineChart from "./comp/KlineChart.js";
import LineChart from "./comp/LineChart.js";
import Page from "./comp/Page.js";
import Panel from "./comp/Panel.js";
import Radio from "./comp/Radio.js";
import Select from "./comp/Select.js";
import TabBody from "./comp/TabBody.js";
import TabHeader from "./comp/TabHeader.js";
import Table from "./comp/Table.js";
import Text from "./comp/Text.js";
import Tree from "./comp/Tree.js";
import Modal from "./comp/Modal.js";
import DatePicker from "./comp/DatePicker.js";
import TimePicker from "./comp/TimePicker.js";
import TextUtil from "../util/TextUtil.js";

// DSL解析器
export default class DslParser {
  constructor(dsl) {
    // 创建一个Designer实例
    this.dsl = dsl;
    window.mountedCompMap = {};
    window.TextUtil = TextUtil;
  }

  create(type, props, styles, events, data) {
    switch (type) {
      case "page":
        return new Page(props, styles, events, data);
      case "container":
        return new Container(props, styles, events, data);
      case "modal":
        return new Modal(props, styles, events, data);
      case "panel":
        return new Panel(props, styles, events, data);
      case "text":
        return new Text(props, styles, events, data);
      case "input":
        return new Input(props, styles, events, data);
      case "button":
        return new Button(props, styles, events, data);
      case "select":
        return new Select(props, styles, events, data);
      case "date":
        return new DatePicker(props, styles, events, data);
      case "time":
        return new TimePicker(props, styles, events, data);
      case "table":
        return new Table(props, styles, events, data);
      case "radio":
        return new Radio(props, styles, events, data);
      case "checkbox":
        return new Checkbox(props, styles, events, data);
      case "tree":
        return new Tree(props, styles, events, data);
      case "linechart":
        return new LineChart(props, styles, events, data);
      case "klinechart":
        return new KlineChart(props, styles, events, data);
      case "tabheader":
        return new TabHeader(props, styles, events, data);
      case "tabbody":
        return new TabBody(props, styles, events, data);
      default:
        throw new Error(`Unknown component type: ${type}`);
    }
  }

  // 递归处理DSL对象
  processDSL(dslObj, parent) {
    // 页面元素实际宽高计算，%单位转换为px
    this.convertUnit(dslObj, parent);
    // 创建组件元素
    const comp = this.create(
      dslObj.type,
      dslObj.props,
      dslObj.styles,
      dslObj.events,
      dslObj.data
    );
    this.setPathCompMap(dslObj.data, comp);
    if (dslObj.children) {
      dslObj.children.forEach((child) => {
        comp.addChild(this.processDSL(child, dslObj));
      });
    }
    return comp;
  }

  setPathCompMap(data, comp) {
    if (!data) {
      return;
    }
    Object.values(data).forEach((i) => {
      if (i.path) {
        if (!window.mountedCompMap[i.path]) {
          window.mountedCompMap[i.path] = [];
        }
        window.mountedCompMap[i.path].push(comp);
      }
    });
  }

  // 百分比转换为实际值
  convertUnit(child, parent) {
    if (!child || !parent || !child.styles || !parent.styles) {
      return;
    }
    // 宽度转换
    if (child.styles.width && parent.styles.width) {
      const cvu = Val.extract(child.styles.width);
      const pvu = Val.extract(parent.styles.width);
      if (cvu[2] == "%" && pvu[2] != "%") {
        child.styles.width =
          (parseFloat(pvu[1]) * parseFloat(cvu[1])) / 100 + pvu[2];
      }
    }
    // 高度转换
    if (child.styles.height && parent.styles.height) {
      const cvu = Val.extract(child.styles.height);
      const pvu = Val.extract(parent.styles.height);
      if (cvu[2] == "%" && pvu[2] != "%") {
        child.styles.height =
          (parseFloat(pvu[1]) * parseFloat(cvu[1])) / 100 + pvu[2];
      }
    }
  }

  processUnit(dslObj, parent, units) {
    this.convertUnit(dslObj, parent);
    const { width, height } = dslObj.styles;
    units[dslObj.id] = { width, height };
    if (dslObj.children) {
      dslObj.children.forEach((child) => {
        this.processUnit(child, dslObj, units);
      });
    }
  }

  transferUnit() {
    const units = {};
    this.processUnit(this.dsl, null, units);
    return units;
  }

  // 简单的DSL解析逻辑
  parse() {
    return this.processDSL(this.dsl, null);
  }
}