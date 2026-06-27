import Id from "./Id.js";
import message from "./Message.js";

export default class Invoker {
  constructor() {
    this.map = new Map();
  }

  invoke(name, param) {
    const res = JSON.parse(native.invoke(name, JSON.stringify(param)));
    if (res.code === "success") {
      return res.data;
    }
    throw new Error(res.message);
  }

  asyncInvoke(name, param, success) {
    const id = Id.random();
    this.map.set(id, success);
    setTimeout(() => {
      native.asyncInvoke(name, id, JSON.stringify(param));
    }, 0);
  }

  asyncCallback(id, param) {
    const res = JSON.parse(param);
    const success = this.map.get(id);
    if (res.code === "success") {
      success(res.data);
    } else if (res.code === "warn") {
      message.warning(res.message);
    } else {
      message.error(res.message);
    }
  }
}
