package model;

import java.time.LocalDateTime;
import java.util.TreeSet;

public class WorkTime {
    private final TreeSet<TimePeriod> periods;

    /**
     * Set of model.TimePeriod objects
     */
    public WorkTime() {
        periods = new TreeSet<>();
    }

    public WorkTime addVisitTime(LocalDateTime visitTime) {
        TimePeriod newPeriod = new TimePeriod(visitTime, visitTime);
        for (TimePeriod period : periods) {
            if (period.compareTo(newPeriod) == 0) {
                period.appendTime(visitTime);
                return this;
            }
        }
        periods.add(newPeriod);
        return this;
    }

    public String toString() {
        StringBuilder line = new StringBuilder();
        for (TimePeriod period : periods) {
            if (line.length() > 0) {
                line.append(", ");
            }
            line.append(period);
        }
        return line.toString();
    }
}
