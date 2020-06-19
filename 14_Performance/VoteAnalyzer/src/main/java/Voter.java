import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Objects;

public class Voter {

    private final String name;
    private final LocalDate birthDay;

    public Voter(String name, LocalDate birthDay) {
        this.name = name;
        this.birthDay = birthDay;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Voter voter = (Voter) obj;
        return (name == voter.name || (name != null && name.equals(voter.getName())))
                && (birthDay == voter.birthDay || (birthDay != null && birthDay.equals(voter.getBirthDay())));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, birthDay);
    }

    public String toString() {
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy.MM.dd");
        return name + " (" + dayFormat.format(birthDay) + ")";
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthDay() {
        return birthDay;
    }
}
