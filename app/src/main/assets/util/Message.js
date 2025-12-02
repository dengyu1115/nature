class Message {
  constructor() {
    if (Message.instance) return Message.instance;
    Message.instance = this;
    this.messages = [];
  }

  // 获取或创建消息容器
  getContainer() {
    if (!this.container) {
      const container = document.createElement("div");
      Object.assign(container.style, {
        position: "fixed",
        top: "20px",
        left: "50%",
        transform: "translateX(-50%)",
        zIndex: "999999",
        display: "flex",
        flexDirection: "column",
        gap: "10px",
      });
      this.container = container;
    }
    return this.container;
  }

  // 生成唯一ID
  generateId() {
    return "message_" + Date.now() + "_" + Math.floor(Math.random() * 1000);
  }

  // 核心方法：显示消息
  show(options) {
    const { text, type = "info", duration = 3000 } = options;
    const messageId = this.generateId();
    // 创建消息元素
    const div = document.createElement("div");
    div.id = messageId;
    // 样式定义 - 使用边框样式替代背景色填充
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
    // 设置不同类型消息的边框颜色和文字颜色
    const typeStyles = {
      success: "#52c41a",
      error: "#ff4d4f",
      warning: "#faad14",
      info: "#1890ff",
    };
    const color = typeStyles[type] || typeStyles.info;
    div.style.borderColor = color;
    div.style.color = color;
    // 创建内容
    const iconSpan = document.createElement("span");
    iconSpan.textContent = {
      success: "✓",
      error: "✕",
      warning: "⚠",
      info: "ⓘ",
    }[type];
    Object.assign(iconSpan.style, {
      marginRight: "10px",
      fontSize: "1.2em",
      color: color,
    });
    div.append(iconSpan, document.createTextNode(text));
    // 点击关闭
    div.addEventListener("click", () => this.removeMessage(messageId));
    // 添加到容器
    const container = this.getContainer();
    container.appendChild(div);
    if (!container.parentElement) document.body.appendChild(container);
    this.messages.push(div);
    // 触发动画
    setTimeout(() => {
      div.style.opacity = "1";
      div.style.transform = "translateY(0)";
    }, 10);
    // 设置自动关闭计时器
    if (duration > 0) {
      div.timer = setTimeout(() => this.removeMessage(messageId), duration);
    }
    return this;
  }

  // 移除消息
  removeMessage(id) {
    const el = document.getElementById(id);
    if (!el) return;
    // 清除计时器
    if (el.timer) clearTimeout(el.timer);
    // 触发退出动画
    el.style.opacity = "0";
    el.style.transform = "translateY(-20px)";
    // 动画结束后移除
    setTimeout(() => {
      el.remove();
      this.messages = this.messages.filter((m) => m.id !== id);
      // 无消息时移除容器
      if (
        this.messages.length === 0 &&
        this.container &&
        this.container.parentElement
      ) {
        this.container.remove();
        this.container = null;
      }
    }, 300);
  }

  // 关闭所有消息
  closeAll() {
    if (!this.container) return;
    [...this.messages].forEach((m) => this.removeMessage(m.id));
  }
}

// 为不同类型消息提供便捷方法
["success", "error", "warning", "info"].forEach((type) => {
  Message.prototype[type] = function (text, duration) {
    return this.show({ text, type, duration });
  };
});

const message = new Message();
export default message;
