import Id from "./Id.js";
import message from "./Message.js";

export default class Invoker {
  constructor() {
    this.map = new Map();
  }

  page(pageId) {
    return new Promise((resolve, reject) => {
      const id = Id.random();
      this.map.set(id, { resolve, reject });
      native.page(id, pageId);
    });
  }

  asyncInvoke(param) {
    return new Promise((resolve, reject) => {
      const id = Id.random();
      this.map.set(id, { resolve, reject });
      native.asyncInvoke(id, JSON.stringify(param));
    });
  }

  asyncCallback(id, param) {
    const res = JSON.parse(param);
    const { resolve, reject } = this.map.get(id);
    if (res.code === "success") {
      resolve(res.data);
    } else {
      reject(res);
    }
  }
}
