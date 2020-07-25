package bubble_sort;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BubbleSortTest {

  @Test
  @DisplayName("Bubble-sort test")
  void sort() {
    int[] testArray = {10, 43, 23, 56, 7, -55, -66, 0};
    BubbleSort.sort(testArray);
    Assertions
        .assertArrayEquals(new int[]{-66, -55, 0, 7, 10, 23, 43, 56},
            testArray);
  }

  @Test
  @DisplayName("BubbleSort exception test")
  void sortExceptionTesting() {
    Assertions.assertThrows(IllegalArgumentException.class,
        () -> BubbleSort.sort(null));
  }
}