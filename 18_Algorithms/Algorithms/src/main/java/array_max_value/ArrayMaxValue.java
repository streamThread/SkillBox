package array_max_value;

public class ArrayMaxValue {

  public static int getMaxValue(int[] values) {
    if (values == null || values.length == 0) {
      throw new IllegalArgumentException("the array can't be empty or null");
    }
    int maxValue = Integer.MIN_VALUE;
    for (int value : values) {
      if (value > maxValue) {
        maxValue = value;
      }
    }
    return maxValue;
  }
}