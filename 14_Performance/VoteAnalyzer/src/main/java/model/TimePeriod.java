package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class TimePeriod implements Comparable<TimePeriod> {

  private LocalDateTime from;
  private LocalDateTime to;

  public TimePeriod(LocalDateTime from, LocalDateTime to) {
    this.from = from;
    this.to = to;
    if (!from.toLocalDate().equals(to.toLocalDate())) {
      throw new IllegalArgumentException(
          "Dates 'from' and 'to' must be within ONE day!");
    }
  }

  public void appendTime(LocalDateTime visitTime) {
    if (!from.toLocalDate().equals(visitTime.toLocalDate())) {
      throw new IllegalArgumentException(
          "Visit time must be within the same day as the current model.TimePeriod!");
    }
    if (visitTime.isBefore(from)) {
      from = visitTime;
    }
    if (visitTime.isAfter(to)) {
      to = visitTime;
    }
  }

  public String toString() {
    DateTimeFormatter dateFormat = DateTimeFormatter
        .ofPattern("yyyy.MM.dd HH:mm");
    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
    String fromStr = dateFormat.format(from);
    String toStr = timeFormat.format(to);
    return fromStr + "-" + toStr;
  }

  @Override
  public int compareTo(TimePeriod period) {
    return from.toLocalDate().compareTo(period.from.toLocalDate());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TimePeriod that = (TimePeriod) o;
    return (Objects.equals(from, that.from))
        && (Objects.equals(to, that.to));
  }

  @Override
  public int hashCode() {
    return Objects.hash(from, to);
  }
}
