package rabin_karp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RabinKarpExtended {

  private final String text;
  private final Map<Integer, List<Integer>> number2position = new HashMap<>();

  public static void main(String[] args) {
    RabinKarpExtended rabinKarpExtended = new RabinKarpExtended("abcdefg sfhg "
        + "sfhfgbn sfbnsfnb "
        + "dsrb");
    rabinKarpExtended.search("a").forEach(System.out::println);
    System.out.println("-------------");
    rabinKarpExtended.search("fh").forEach(System.out::println);
    System.out.println("------------");
    rabinKarpExtended.search("agfdg").forEach(System.out::println);
  }

  public RabinKarpExtended(String text) {
    this.text = text;
    createIndex();
  }

  public List<Integer> search(String query) {
    int queryLenght = Optional.ofNullable(query)
        .filter(q -> !q.isEmpty())
        .map(String::length)
        .orElseThrow(IllegalArgumentException::new);
    return Optional.ofNullable(number2position.get(query.hashCode()))
        .map(list -> list.stream()
            .filter(
                ind -> text.substring(ind, ind + queryLenght).compareTo(query)
                    == 0)
            .collect(Collectors.toList())).orElseGet(ArrayList::new);
  }

  private void createIndex() {
    for (int textCharIndx = 0; textCharIndx < text.length() - 1;
        textCharIndx++) {
      int hashIndxInText = textCharIndx;
      for (int patternSize = 1; patternSize < text.length() - textCharIndx;
          patternSize++) {
        number2position.compute(
            text.substring(textCharIndx, textCharIndx + patternSize).hashCode(),
            (hash, indList) -> {
              if (indList == null) {
                return Stream.of(hashIndxInText).collect(Collectors.toList());
              }
              indList.add(hashIndxInText);
              return indList;
            });
      }
    }
  }
}