export default class Id {
  static count = 0;

  static random() {
    // 生成I00001格式的id
    return "I" + (Id.count++).toString().padStart(5, "0");
  }
}
