package binary_search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

public class BinarySearch {

  private final ArrayList<String> list;

  public BinarySearch(ArrayList<String> list) {
    Collections.sort(
        Optional.ofNullable(list)
            .filter(l -> l.size() > 1)
            .orElseThrow(IllegalArgumentException::new));
    this.list = list;
  }

  public int search(String query) {
    return Optional.ofNullable(query)
        .filter(q -> !q.isEmpty())
        .map(s -> search(s, 0, list.size() - 1))
        .orElseThrow(IllegalArgumentException::new);
  }

  private int search(String query, int from, int to) {
    if (from == to) {
      return -1;
    }
    int middle = (from + to) / 2;
    int comparison = query.compareTo(list.get(middle));
    if (comparison == 0) {
      return middle;
    }
    if (comparison > 0) {
      return search(query, middle, to);
    }
    return search(query, from, middle);
  }
}
