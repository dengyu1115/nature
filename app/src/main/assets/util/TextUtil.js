/**
 * 文本工具类 - JavaScript 实现版本
 */
export default class TextUtil {
  /**
   * 将对象转换为字符串表示
   * @param {*} o 需要转换的对象
   * @returns {string} 转换后的字符串
   */
  static text(o) {
    return TextUtil._text(o, (item) => String(item));
  }

  /**
   * 格式化金额数值为带单位的字符串（万/亿/万亿）
   * @param {number|null} o 金额数值
   * @returns {string} 格式化后的金额字符串
   */
  static amount(o) {
    return TextUtil._text(o, (i) => {
      const val = Math.abs(i);
      if (val < 10000) {
        return i.toFixed(2);
      } else if (val < 100000000) {
        return (i / 10000).toFixed(2) + "万";
      } else if (val < 1000000000000) {
        return (i / 10000 / 10000).toFixed(4) + "亿";
      } else {
        return (i / 10000 / 10000 / 10000).toFixed(4) + "万亿";
      }
    });
  }

  /**
   * 格式化份额数值为带单位的字符串（万/亿/万亿）
   * @param {number|null} o 份额数值
   * @returns {string} 格式化后的份额字符串
   */
  static share(o) {
    return TextUtil._text(o, (i) => {
      const val = Math.abs(i);
      if (val < 10000 * 100) {
        return (i / 100).toFixed(0) + "手";
      } else if (val < 100000000 * 100) {
        return (i / 10000 / 100).toFixed(2) + "万手";
      } else if (val < 1000000000000 * 100) {
        return (i / 10000 / 10000 / 100).toFixed(4) + "亿手";
      } else {
        return (i / 10000 / 10000 / 10000 / 100).toFixed(4) + "万亿手";
      }
    });
  }

  /**
   * 将小数转换为百分比格式的字符串
   * @param {number|null} o 小数值
   * @returns {string} 百分比格式的字符串
   */
  static hundred(o) {
    if (o === null || o === undefined) {
      return "";
    }
    return TextUtil._text(o, (i) => (i * 100).toFixed(2) + "%");
  }

  /**
   * 将数值转换为百分比格式的字符串
   * @param {number|null} o 数值
   * @returns {string} 百分比格式的字符串
   */
  static percent(o) {
    return TextUtil._text(o, (i) => i.toFixed(2) + "%");
  }

  /**
   * 格式化价格数值为三位小数字符串
   * @param {number|null} o 价格数值
   * @returns {string} 格式化后的价格字符串
   */
  static price(o) {
    return TextUtil._text(o, (i) => i.toFixed(3));
  }

  /**
   * 格式化网络数值为四位小数字符串
   * @param {number|null} o 网络数值
   * @returns {string} 格式化后的网络字符串
   */
  static net(o) {
    return TextUtil._text(o, (i) => i.toFixed(4));
  }

  /**
   * 解析字符串为 BigDecimal 对象（模拟 Java 中的 BigDecimal）
   * @param {string|null} s 字符串
   * @returns {number|null} 解析出的数字或 null
   */
  static decimal(s) {
    if (!s || s === "-" || s === "---") {
      return null;
    }
    if (s.endsWith("%")) {
      s = s.slice(0, -1); // 移除末尾的 %
    }
    const parsed = parseFloat(s);
    if (isNaN(parsed)) {
      throw new Error("decimal format error: " + s);
    }
    return parsed;
  }

  /**
   * 连接多个字符串为一个冒号分隔的字符串
   * @param  {...string} arr 字符串数组
   * @returns {string} 连接后的字符串
   */
  static join(...arr) {
    return arr.filter(Boolean).join(":"); // 过滤掉空值后连接
  }

  /**
   * 转化为文本的核心方法
   * @template T
   * @param {T} t 对象
   * @param {function(T): string} func 处理函数
   * @returns {string} 处理结果
   */
  static _text(t, func) {
    if (t === null || t === undefined) {
      return "";
    }
    return func(t);
  }
}
