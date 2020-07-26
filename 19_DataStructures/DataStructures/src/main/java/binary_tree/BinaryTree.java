package binary_tree;

import java.util.ArrayList;
import java.util.List;

public class BinaryTree {

  private Node root;

  public void addNode(String data) {
    Node inputNode = new Node(data);
    if (root == null) {
      root = inputNode;
    } else {
      addInputNodeToTree(root, inputNode);
    }
  }

  private void addInputNodeToTree(Node treeNode, Node inputNode) {
    int comparison = treeNode.getData().compareTo(inputNode.getData());
    if (comparison >= 0) {
      if (treeNode.getRight() == null) {
        treeNode.setRight(inputNode);
        inputNode.setParent(treeNode);
      } else {
        addInputNodeToTree(treeNode.getRight(), inputNode);
      }
    } else {
      if (treeNode.getLeft() == null) {
        treeNode.setLeft(inputNode);
        inputNode.setParent(treeNode);
      } else {
        addInputNodeToTree(treeNode.getLeft(), inputNode);
      }
    }
  }

  public List<Node> searchNodes(String data) {
    List<Node> listOfFoundNodes = new ArrayList<>();
    if (root == null) {
      return listOfFoundNodes;
    }
    searchDataInTree(data, root, listOfFoundNodes);
    return listOfFoundNodes;
  }

  private void searchDataInTree(String data,
      Node currentNode, List<Node> listOfFoundNodes) {
    int comparison = currentNode.getData().compareTo(data);
    if (comparison == 0) {
      listOfFoundNodes.add(currentNode);
    }
    if (comparison >= 0) {
      if (currentNode.getRight() != null) {
        searchDataInTree(data, currentNode.getRight(), listOfFoundNodes);
      }
    } else {
      if (currentNode.getLeft() != null) {
        searchDataInTree(data, currentNode.getLeft(), listOfFoundNodes);
      }
    }
  }
}