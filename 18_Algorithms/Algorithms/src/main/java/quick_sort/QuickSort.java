package quick_sort;

public class QuickSort {

  public static void sort(int[] array) {
    if (array.length <= 1) {
      return;
    }
    sort(array, 0, array.length - 1);
  }

  private static void sort(int[] array, int from, int to) {
    if (from < to) {
      int pivot = partition(array, from, to);
      sort(array, from, pivot - 1);
      sort(array, pivot + 1, to);
    }
  }

  private static int partition(int[] array, int from, int to) {
    int partitionValue = array[to];
    int indexToSwap = from - 1;
    for (int mainArrIndx = from; mainArrIndx < to; mainArrIndx++) {
      if (array[mainArrIndx] <= partitionValue) {
        indexToSwap++;
        int swapTemp = array[indexToSwap];
        array[indexToSwap] = array[mainArrIndx];
        array[mainArrIndx] = swapTemp;
      }
    }
    int swapTemp = array[indexToSwap + 1];
    array[indexToSwap + 1] = array[to];
    array[to] = swapTemp;
    return indexToSwap + 1;
  }
}
