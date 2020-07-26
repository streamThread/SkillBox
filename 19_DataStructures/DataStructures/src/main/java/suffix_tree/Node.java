package suffix_tree;

import java.util.ArrayList;
import java.util.List;

public class Node {

  private final String fragment;
  private final ArrayList<Integer> nextNodes;
  private final int position;

  public Node(String fragment, int position) {
    this.fragment = fragment;
    nextNodes = new ArrayList<>();
    this.position = position;
  }

  public String getFragment() {
    return fragment;
  }

  public int getPosition() {
    return position;
  }

  public List<Integer> getNextNodes() {
    return nextNodes;
  }
}