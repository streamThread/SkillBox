package suffix_tree;

import static java.util.Collections.EMPTY_LIST;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SuffixTreeTest {

  SuffixTree suffixTree = new SuffixTree("havanabanana");

  @Test
  void searchOneLetter() {
    List<String> matches = suffixTree.search("a");
    Assertions.assertLinesMatch(List.of(
        "h[a]vanabanana",
        "hav[a]nabanana",
        "havan[a]banana",
        "havanab[a]nana",
        "havanaban[a]na",
        "havanabanan[a]"), matches);
  }

  @Test
  void searchValidPattern() {
    List<String> matches = suffixTree.search("nab");
    Assertions.assertLinesMatch(List.of("hava[nab]anana"), matches);
  }

  @Test
  void searchInvalidPattern() {
    List<String> matches = suffixTree.search("nag");
    Assertions.assertLinesMatch(EMPTY_LIST, matches);
  }
}