import java.util.Date;
import java.util.TreeSet;

public class WorkTime {
    private final TreeSet<TimePeriod> periods;

    /**
     * Set of TimePeriod objects
     */
    public WorkTime() {
        periods = new TreeSet<>();
    }

    public WorkTime addVisitTime(long visitTime) {
        Date visit = new Date(visitTime);
        TimePeriod newPeriod = new TimePeriod(visit, visit);
        for (TimePeriod period : periods) {
            if (period.compareTo(newPeriod) == 0) {
                period.appendTime(visit);
                return this;
            }
        }
        periods.add(new TimePeriod(visit, visit));
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
