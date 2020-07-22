package bubble_sort;

import java.util.Optional;

public class BubbleSort {

  public static void sort(int[] array) {
    Optional.ofNullable(array).orElseThrow(IllegalArgumentException::new);
    int n = array.length;
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
