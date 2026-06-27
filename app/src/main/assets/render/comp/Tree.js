import Reactive from "../../util/Reactive.js";
import Base from "./Base.js";

export default class Tree extends Base {
  render() {
    const element = this.createElement("div");
    this.element = element;
    const tree = document.createElement("ul");
    element.appendChild(tree);
    this.tree = tree;
    tree.style.paddingLeft = "0";
    tree.style.margin = "0";
    this.updateNodes();
    this.buildTree();
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

  buildTree(nodes) {
    // 未传参时使用顶层节点
    const targetNodes = nodes || this.nodes;
    if (!targetNodes) return;

    if (nodes) {
      // 子节点递归：创建子树 ul 并返回
      const ul = document.createElement("ul");
      ul.style.paddingLeft = "20px";
      targetNodes.forEach((node) => {
        ul.appendChild(this.renderNode(node));
      });
      return ul;
    }

    // 顶层渲染：清空并重建
    this.tree.innerHTML = "";
    targetNodes.forEach((node) => {
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
      icon.style.transform = "rotate(90deg)";
      // 递归创建子节点
      const childUl = this.buildTree(node.children);
      li.appendChild(childUl);
      // 添加展开/收起功能
      item.addEventListener("click", (e) => {
        if (e.target !== icon) return;
        if (childUl.style.display === "none") {
          childUl.style.display = "";
          icon.style.transform = "rotate(90deg)";
        } else {
          childUl.style.display = "none";
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