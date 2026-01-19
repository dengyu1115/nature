/**
 * SQL 构建器类
 */
export default class SqlBuilder {
  /**
   * 解析对象字段构建SQL
   * @param {string} template sql模板
   * @param {Object} obj 数据集合
   * @returns {string} sql
   */
  static one(template, obj) {
    // 模板可能是这种结构"insert into user(id,name) values({id},{name})"，对象可能是这种结构{id:1,name:"张三"}
    // 得到的sql是"insert into user(id,name) values(1,'张三')"
    // 可以根据字段的类型的自动进行拼接处理
    if (!template || !obj) return "";
    // 使用正则表达式匹配所有 {字段名} 的占位符
    const regex = /\{(\w+)\}/g;
    let sql = template;
    let match;

    while ((match = regex.exec(template)) !== null) {
      const placeholder = match[0]; // 完整的占位符，如 {id}
      const fieldName = match[1]; // 占位符中的字段名，如 id
      const value = obj[fieldName];
      if (value !== undefined) {
        const formattedValue = SqlBuilder.formatValue(value);
        sql = sql.replace(new RegExp(placeholder, "g"), formattedValue);
      } else {
        // 如果对象中没有这个字段，保留原占位符或替换为NULL
        sql = sql.replace(new RegExp(placeholder, "g"), "NULL");
      }
    }
    return sql;
  }

  /**
   * 遍历数组构建SQL
   * @param {string} template sql模板
   * @param {Array} items 数据集合
   * @returns {string} sql
   */
  static each(template, items) {
    // 模板可能是这种结构"insert into user(id,name) values[({id},{name})]"，对象可能是这种结构[{id:1,name:"张三"},{id:2,name:"张三"}]
    // 得到的sql是"insert into user(id,name) values(1,'张三'),(2,'张三')"
    // 可以遍历数组，根据字段的类型自动进行拼接处理
    if (!template || !items || !Array.isArray(items) || items.length === 0)
      return "";

    // 查找循环部分：用方括号 [] 括起来的部分，里面包含占位符
    const loopPattern = /\[([^\[\]]*?\{.*?\}.*?)\]/;
    const match = template.match(loopPattern);

    if (match) {
      // 提取循环部分（去掉方括号）
      const loopPart = match[1]; // match[1] 是第一个捕获组，即方括号内的内容
      // 获取循环部分前和后的固定部分
      const beforeLoop = template.substring(0, match.index);
      const afterLoop = template.substring(match.index + match[0].length);
      const sqlParts = [];
      for (const item of items) {
        const part = SqlBuilder.one(loopPart, item);
        sqlParts.push(part);
      }
      // 将前缀 + 循环部分处理结果 + 后缀
      return beforeLoop + sqlParts.join(",") + afterLoop;
    } else {
      throw new Error("模板格式错误");
    }
  }

  /**
   * 根据值类型格式化值
   * @param {*} value 要格式化的值
   * @returns {string} 格式化后的值
   */
  static formatValue(value) {
    if (value === null || value === undefined) {
      return "NULL";
    }
    if (typeof value === "string") {
      // 转义字符串中的单引号
      return `'${value.replace(/'/g, "''")}'`;
    }
    if (typeof value === "number" || typeof value === "boolean") {
      return String(value);
    }
    if (value instanceof Date) {
      return `'${value.toISOString().slice(0, 19).replace("T", " ")}'`;
    }
    // 对于其他类型，转换为字符串并加上引号
    return `'${String(value)}'`;
  }
}
