// 全局类型配置：颜色与图标
const TYPE_CONFIG = {
  success: { color: "#52c41a", icon: "✓" },
  error: { color: "#ff4d4f", icon: "✕" },
  warning: { color: "#faad14", icon: "⚠" },
  info: { color: "#1890ff", icon: "ⓘ" },
};

class Message {
  constructor() {
    if (Message.instance) return Message.instance;
    Message.instance = this;
    this.messages = [];
    this.count = 0;
  }

  getContainer() {
    if (!this.container) {
      this.container = document.createElement("div");
      Object.assign(this.container.style, {
        position: "fixed",
        top: "20px",
        left: "50%",
        transform: "translateX(-50%)",
        zIndex: "999999",
        display: "flex",
        flexDirection: "column",
        gap: "10px",
      });
    }
    return this.container;
  }

  generateId() {
    this.count %= 100000;
    return `M${String(this.count++).padStart(5, "0")}`;
  }

  show(options) {
    const { text, type = "info", duration = 3000 } = options;
    const messageId = this.generateId();
    const div = document.createElement("div");
    div.id = messageId;
    Object.assign(div.style, {
      display: "flex",
      alignItems: "center",
      padding: "12px 20px",
      borderRadius: "6px",
      boxShadow: "0 4px 12px rgba(0, 0, 0, 0.15)",
      fontFamily:
        "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
      fontSize: "14px",
      opacity: "0",
      transform: "translateY(-20px)",
      transition: "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)",
      minWidth: "300px",
      maxWidth: "400px",
      cursor: "pointer",
      border: "1px solid",
      backgroundColor: "white",
    });

    const { color, icon } = TYPE_CONFIG[type] || TYPE_CONFIG.info;
    div.style.borderColor = color;
    div.style.color = color;

    const iconSpan = document.createElement("span");
    iconSpan.textContent = icon;
    Object.assign(iconSpan.style, {
      marginRight: "10px",
      fontSize: "1.2em",
      color,
    });
    div.append(iconSpan, text);

    div.addEventListener("click", () => this.removeMessage(messageId));

    const container = this.getContainer();
    container.appendChild(div);
    if (!container.parentElement) document.body.appendChild(container);
    this.messages.push(div);

    setTimeout(() => {
      div.style.opacity = "1";
      div.style.transform = "translateY(0)";
    }, 10);

    if (duration > 0) {
      div.timer = setTimeout(() => this.removeMessage(messageId), duration);
    }
    return this;
  }

  removeMessage(id) {
    const el = this.messages.find((m) => m.id === id);
    if (!el) return;

    if (el.timer) clearTimeout(el.timer);

    el.style.opacity = "0";
    el.style.transform = "translateY(-20px)";

    setTimeout(() => {
      el.remove();
      this.messages = this.messages.filter((m) => m !== el);
      this._cleanupContainer();
    }, 300);
  }

  closeAll() {
    this.messages.forEach((m) => {
      if (m.timer) clearTimeout(m.timer);
      m.remove();
    });
    this.messages = [];
    this._cleanupContainer();
  }

  _cleanupContainer() {
    if (this.messages.length === 0 && this.container?.parentElement) {
      this.container.remove();
      this.container = null;
    }
  }
}

Object.keys(TYPE_CONFIG).forEach((type) => {
  Message.prototype[type] = function (text, duration) {
    return this.show({ text, type, duration });
  };
});

const message = new Message();
export default message;
