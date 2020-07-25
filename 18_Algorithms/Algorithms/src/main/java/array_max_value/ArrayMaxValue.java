package array_max_value;

import java.util.Optional;

public class ArrayMaxValue {

  public static int getMaxValue(int[] values) {
    Optional.ofNullable(values)
        .filter(val -> val.length != 0)
        .orElseThrow(() -> new IllegalArgumentException("the array can't be "
            + "empty or null"));
    int maxValue = Integer.MIN_VALUE;
    for (int value : values) {
      if (value > maxValue) {
        maxValue = value;
      }
    }
    return maxValue;
  }
}