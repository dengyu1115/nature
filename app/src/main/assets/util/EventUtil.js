export default class EventUtil {
  static compileAsync(script, args = [], name = "") {
    if (!script) {
      return null;
    }
    script = `
if (element) {
  element.disabled = true;
}
(async () => {
  try {
    ${script}
  } catch (err) {
    if (err && err.code === "warning") {
      message.warning(err.message);
    } else {
      message.error(err.message);
    }
  } finally {
    if (element) {
      element.disabled = false;
    }
  }
})();
`;
    try {
      args.unshift("event", "element");
      return new Function(args, script);
    } catch (err) {
      message.error(name + "事件编译出错:" + err.message);
      return null;
    }
  }

  static compileNormal(script, args = [], name = "") {
    if (!script) {
      return null;
    }
    script = `
try {
  ${script}
} catch (err) {
  if (err && err.code === "warning") {
    message.warning(err.message);
  } else {
    message.error(err.message);
  }
} 
`;
    try {
      return new Function(args, script);
    } catch (err) {
      message.error(name + "事件编译出错:" + err.message);
      return null;
    }
  }
}
