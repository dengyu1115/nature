import Reactive from "../../util/Reactive.js";

export default (BaseClass) => class extends BaseClass {
  refreshProp(propName, domProp, defaultValue) {
    const path = this.data[propName]?.path;
    if (path) {
      this[propName] = Reactive.get(data, path);
    } else {
      this[propName] = this.props[propName] != null ? this.props[propName] : defaultValue;
    }
    this.element[domProp || propName] = this[propName];
  }

  refreshBoolProp(propName) {
    const path = this.data[propName]?.path;
    if (path) {
      this.element[propName] = Reactive.get(data, path);
    } else {
      this.element[propName] = this.props[propName] === "true";
    }
  }
};