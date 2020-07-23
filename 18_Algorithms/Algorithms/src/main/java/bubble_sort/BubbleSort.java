package bubble_sort;

import java.util.Optional;

public class BubbleSort {

  public static void sort(int[] array) {
    int n = Optional.ofNullable(array)
        .filter(arr -> arr.length > 0)
        .map(arr -> arr.length)
        .orElseThrow(IllegalArgumentException::new);
    int temp;
    for (int i = 0; i < n; i++) {
      for (int j = 1; j < n - i; j++) {
        if (array[j - 1] > array[j]) {
          temp = array[j - 1];
          array[j - 1] = array[j];
          array[j] = temp;
        }
      }
    }
  }
}
