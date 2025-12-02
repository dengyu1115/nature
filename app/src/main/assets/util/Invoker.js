export default class Invoke {
  invoke(name, param) {
    const res = JSON.parse(native.invoke(name, JSON.stringify(param)));
    if (res.code === "success") {
      return res.data;
    }
    throw new Error(res.message);
  }
}
