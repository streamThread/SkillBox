package jsonclasses;

import java.util.Objects;

public class Line implements Comparable<Line> {
    private String number;
    private String name;

    public Line(String number, String name) {
        this.number = number;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return number.equals(line.number) &&
                name.equals(line.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, name);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Line line) {
        if (getNumber().equals(line.getNumber())) {
            return getName().compareTo(line.getName());
        }
        if (getName().equals(line.getName())) {
            return getNumber().compareTo(line.getNumber());
        }
        return getNumber().compareTo(line.getNumber());
    }
}

