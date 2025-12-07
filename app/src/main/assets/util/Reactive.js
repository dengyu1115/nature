export default class Reactive {
  /**
   * 代理对象
   * @param {*} obj 目标对象
   * @param {*} pk 父级key
   * @returns
   */
  static proxy(obj, pk = "") {
    if (obj == null || typeof obj != "object") {
      return obj;
    }
    if (pk) {
      pk = pk + ".";
    }
    for (let k in obj) {
      if (obj[k] == null || typeof obj[k] == "object") {
        obj[k] = Reactive.proxy(obj[k], pk + k);
      }
    }
    return new Proxy(obj, {
      get(target, key, receiver) {
        return Reflect.get(target, key, receiver);
      },
      set(target, key, value, receiver) {
        const path = pk + key;
        if (value != null && typeof value === "object") {
          value = Reactive.proxy(value, path);
        }
        const res = Reflect.set(target, key, value, receiver);
        Reactive.refresh(path);
        return res;
      },
      deleteProperty(target, key) {
        const res = Reflect.deleteProperty(target, key);
        const path = pk + key;
        Reactive.refresh(path);
        return res;
      },
    });
  }

  static refresh(path) {
    const components = window.mountedCompMap[path];
    if (components) {
      components.forEach((component) => {
        if (Reactive.trigger && Reactive.trigger === component) {
          return;
        }
        component.refresh(path);
      });
    }
    Reactive.trigger = null;
  }

  /**
   * 获取数据
   * @param {*} data 数据
   * @param {*} key 数据路径data.prop1.prop2
   * @returns
   */
  static get(data, key) {
    if (!key) {
      return null;
    }
    const keys = key.split(".");
    for (let i = 0; i < keys.length; i++) {
      if (data[keys[i]] != null && data[keys[i]] != undefined) {
        data = data[keys[i]];
      } else {
        return null;
      }
    }
    return data;
  }

  /**
   * 设置值
   * @param {*} data 数据对象
   * @param {*} key 数据路径data.prop1.prop2
   * @param {*} value 值
   * @returns
   */
  static set(data, key, value, comp) {
    if (!key) {
      return;
    }
    const keys = key.split(".");
    for (let i = 0; i < keys.length - 1; i++) {
      if (data[keys[i]]) {
        data = data[keys[i]];
      } else {
        return;
      }
    }
    if (comp) {
      Reactive.trigger = comp;
    }
    data[keys[keys.length - 1]] = value;
  }
}
