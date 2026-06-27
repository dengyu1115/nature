export default class Id {
  static count = 0;

  static random() {
    if (Id.count === 99999) {
      Id.count = 0;
    }
    return `I${String(Id.count++).padStart(5, "0")}`;
  }
}