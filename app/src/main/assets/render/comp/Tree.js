import Reactive from "../../util/Reactive.js";
import Base from "./Base.js";

export default class Tree extends Base {
  render() {
    // 创建容器元素
    const element = this.createElement("div");
    this.element = element;
    const tree = document.createElement("ul");
    element.appendChild(tree);
    this.tree = tree;
    tree.style.paddingLeft = "0";
    tree.style.margin = "0";
    return element;
  }

  refreshTree() {
    this.updateNodes();
    // 渲染树形结构
    this.buildTree();
  }

  updateNodes() {
    const path = this.data.nodes?.path;
    if (path) {
      this.nodes = Reactive.get(data, path) || [];
    } else {
      this.nodes = this.props.nodes || [];
    }
  }

  buildTree() {
    this.tree.innerHTML = "";
    this.nodes.forEach((node) => {
      this.tree.appendChild(this.renderNode(node));
    });
  }

  renderNode(node) {
    const li = document.createElement("li");
    li.style.listStyle = "none";
    li.style.margin = "4px 0";
    // 创建节点元素
    const item = document.createElement("div");
    li.appendChild(item);
    item.style.display = "flex";
    item.style.alignItems = "center";
    item.style.padding = "4px 8px";
    item.style.cursor = "pointer";
    // 创建图标元素
    const icon = document.createElement("span");
    icon.style.marginRight = "8px";
    icon.style.fontSize = "12px";
    icon.style.transition = "transform 0.2s";
    item.appendChild(icon);
    // 如果有子节点，添加事件
    if (node.children && node.children.length > 0) {
      icon.textContent = "▶";
      // 创建子节点列表
      const element = document.createElement("div");
      icon.style.transform = "rotate(90deg)";
      element.style.paddingLeft = "20px";
      const ul = this.buildTree(node.children);
      element.appendChild(ul);
      li.appendChild(element);
      // 添加展开/收起功能
      item.addEventListener("click", (e) => {
        if (e.target !== icon) return;
        if (ul.style.display === "none") {
          ul.style.display = "";
          icon.style.transform = "rotate(90deg)";
        } else {
          ul.style.display = "none";
          icon.style.transform = "rotate(0deg)";
        }
      });
    } else {
      // 没有子节点时添加占位符
      icon.innerHTML = "&nbsp;&nbsp;&nbsp;";
    }
    // 添加节点标签
    const label = document.createElement("span");
    label.textContent = node.label;
    item.appendChild(label);
    return li;
  }
}
