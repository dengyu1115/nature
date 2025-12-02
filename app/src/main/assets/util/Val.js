export default class Val {
  /**
   * 提取数字和单位
   * @param {string} value
   * @return {[string, string]} 数字和单位
   */
  static extract(value) {
    return value.match(/^(\d+\.?\d*)\s*([a-zA-z%]+)$/);
  }

  /**
   * 颜色转换为rgba值
   * @param {*} value
   */
  static color(value, a) {
    // 把#000000格式转换为rgba(0,0,0,1)
    if (value.startsWith("#")) {
      const [r, g, b] = value
        .substring(1)
        .match(/../g)
        .map((i) => parseInt(i, 16));
      return `rgba(${r},${g},${b},${a})`;
    }
    return value;
  }
}
