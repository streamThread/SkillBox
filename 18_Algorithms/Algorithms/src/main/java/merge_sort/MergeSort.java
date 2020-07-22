package merge_sort;

public class MergeSort {

  public static void mergeSort(int[] array) {
    int n = array.length;
    if (n < 2) {
      return;
    }
    int middle = n / 2;
    int[] leftArray = new int[middle];
    int[] rightArray = new int[n - middle];
    System.arraycopy(array, 0, leftArray, 0, middle);
    System.arraycopy(array, middle, rightArray, 0, n - middle);
    mergeSort(leftArray);
    mergeSort(rightArray);
    merge(array, leftArray, rightArray);
  }

  private static void merge(int[] array, int[] leftArray, int[] rightArray) {
    int leftIndx = 0;
    int rightIndx = 0;
    int mainIndx = 0;
    int leftLenght = leftArray.length;
    int rightLengt = rightArray.length;
    while (leftIndx < leftLenght && rightIndx < rightLengt) {
      if (leftArray[leftIndx] <= rightArray[rightIndx]) {
        array[mainIndx++] = leftArray[leftIndx++];
      } else {
        array[mainIndx++] = rightArray[rightIndx++];
      }
    }
    while (leftIndx < leftLenght) {
      array[mainIndx++] = leftArray[leftIndx++];
    }
    while (rightIndx < rightLengt) {
      array[mainIndx++] = rightArray[rightIndx++];
    }
  }
}
