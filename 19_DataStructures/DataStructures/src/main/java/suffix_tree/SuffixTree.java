package suffix_tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SuffixTree {

  private static final String WORD_TERMINATION = "$";
  private static final int POSITION_UNDEFINED = -1;
  private final String fullText;
  private final Node root;


  public SuffixTree(String text) {
    this.fullText = text;
    root = new Node("", POSITION_UNDEFINED);
    build();
  }

  private void build() {
    for (int i = 0; i < fullText.length(); i++) {
      addSuffix(fullText.substring(i) + WORD_TERMINATION, i);
    }
  }

  public List<String> search(String pattern) {
    List<Node> nodes = getAllNodesInTraversePath(pattern, root, false);
    if (nodes.isEmpty()) {
      return Collections.emptyList();
    }
    Node lastNode = nodes.get(nodes.size() - 1);
    if (lastNode == null) {
      return Collections.emptyList();
    }
    return getPositions(lastNode).stream()
        .sorted()
        .map(position -> markPatternInText(position, pattern))
        .collect(Collectors.toList());
  }

  private void addChildNode(Node parentNode, String text, int index) {
    parentNode.getNextNodes().add(new Node(text, index));
  }

  private String getLongestCommonPrefix(String str1, String str2) {
    int compareLength = Math.min(str1.length(), str2.length());
    for (int i = 0; i < compareLength; i++) {
      if (str1.charAt(i) != str2.charAt(i)) {
        return str1.substring(0, i);
      }
    }
    return str1.substring(0, compareLength);
  }

  private void splitNodeToParentAndChild(Node parentNode, String parentNewText,
      String childNewText) {
    Node childNode = new Node(childNewText, parentNode.getPosition());
    if (!parentNode.getNextNodes().isEmpty()) {
      while (!parentNode.getNextNodes().isEmpty()) {
        childNode.getNextNodes()
            .add(parentNode.getNextNodes().remove(0));
      }
    }
    parentNode.getNextNodes().add(childNode);
    parentNode.setFragment(parentNewText);
    parentNode.setPosition(POSITION_UNDEFINED);
  }

  private List<Node> getAllNodesInTraversePath(String pattern, Node startNode,
      boolean isAllowPartialMatch) {
    List<Node> nodes = new ArrayList<>();
    for (int i = 0; i < startNode.getNextNodes().size(); i++) {
      Node currentNode = startNode.getNextNodes().get(i);
      String nodeText = currentNode.getFragment();
      if (pattern.charAt(0) != nodeText.charAt(0)) {
        continue;
      }
      if (isAllowPartialMatch && pattern.length() <= nodeText.length()) {
        nodes.add(currentNode);
        return nodes;
      }

      int compareLength = Math.min(nodeText.length(), pattern.length());
      for (int j = 1; j < compareLength; j++) {
        if (pattern.charAt(j) == nodeText.charAt(j)) {
          continue;
        }
        if (isAllowPartialMatch) {
          nodes.add(currentNode);
        }
        return nodes;
      }

      nodes.add(currentNode);
      if (pattern.length() <= compareLength) {
        return nodes;
      }
      List<Node> nodes2 = getAllNodesInTraversePath(
          pattern.substring(compareLength),
          currentNode, isAllowPartialMatch);
      if (!nodes2.isEmpty()) {
        nodes.addAll(nodes2);
      } else if (!isAllowPartialMatch) {
        nodes.add(null);
      }
      return nodes;
    }
    return nodes;
  }

  private void extendNode(Node node, String newText, int position) {
    String currentText = node.getFragment();
    String commonPrefix = getLongestCommonPrefix(currentText, newText);

    if (!commonPrefix.equals(currentText)) {
      String parentText = currentText.substring(0, commonPrefix.length());
      String childText = currentText.substring(commonPrefix.length());
      splitNodeToParentAndChild(node, parentText, childText);
    }

    String remainingText = newText.substring(commonPrefix.length());
    addChildNode(node, remainingText, position);
  }

  private void addSuffix(String suffix, int position) {
    List<Node> nodes = getAllNodesInTraversePath(suffix, root, true);
    if (nodes.isEmpty()) {
      addChildNode(root, suffix, position);
    } else {
      Node lastNode = nodes.remove(nodes.size() - 1);
      String newText = suffix;
      if (!nodes.isEmpty()) {
        String existingSuffixUptoLastNode = nodes.stream()
            .map(Node::getFragment)
            .reduce("", String::concat);
        newText = newText.substring(existingSuffixUptoLastNode.length());
      }
      extendNode(lastNode, newText, position);
    }
  }

  private List<Integer> getPositions(Node node) {
    List<Integer> positions = new ArrayList<>();
    if (node.getFragment().endsWith(WORD_TERMINATION)) {
      positions.add(node.getPosition());
    }
    for (int i = 0; i < node.getNextNodes().size(); i++) {
      positions.addAll(getPositions(node.getNextNodes().get(i)));
    }
    return positions;
  }

  private String markPatternInText(Integer startPosition, String pattern) {
    String matchingTextLHS = fullText.substring(0, startPosition);
    String matchingText = fullText
        .substring(startPosition, startPosition + pattern.length());
    String matchingTextRHS = fullText
        .substring(startPosition + pattern.length());
    return matchingTextLHS + "[" + matchingText + "]" + matchingTextRHS;
  }
}