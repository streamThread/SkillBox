package main.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public final class Convert {

  private Convert() {
  }

  public static String getDateAsString(LocalDateTime dateTime) {
    return dateTime
        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
  }
}
