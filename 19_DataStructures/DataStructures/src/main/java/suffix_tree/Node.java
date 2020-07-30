package suffix_tree;

import java.util.ArrayList;
import java.util.List;

public class Node {

  private final ArrayList<Node> nextNodes;
  private String fragment;
  private int position;

  public Node(String fragment, int position) {
    this.fragment = fragment;
    this.position = position;
    nextNodes = new ArrayList<>();
  }

  public String getFragment() {
    return fragment;
  }

  public int getPosition() {
    return position;
  }

  public List<Node> getNextNodes() {
    return nextNodes;
  }

  public void setFragment(String fragment) {
    this.fragment = fragment;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  @Override
  public String toString() {
    return "Node{" +
        "fragment: " + fragment +
        ", position: " + position +
        '}';
  }
}