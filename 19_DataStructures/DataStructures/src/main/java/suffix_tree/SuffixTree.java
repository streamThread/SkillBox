package suffix_tree;

import java.util.ArrayList;
import java.util.List;

public class SuffixTree {

  private final String text;
  private final ArrayList<Node> nodes;
  private Node root;

  public SuffixTree(String text) {
    this.text = text;
    nodes = new ArrayList<>();
  }

  private void build() {
    //TODO
  }

  private List<Integer> search(String query) {
    ArrayList<Integer> positions = new ArrayList<>();
    //TODO
    return positions;
  }
}