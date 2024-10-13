package model;
import java.util.List;

public class Subject {
    private final String name;
    private final List<String> daysOfWeek;

    public Subject(String name, List<String> daysOfWeek) {
        this.name = name;
        this.daysOfWeek = daysOfWeek;
    }

    public String getName() {
        return name;
    }

    public List<String> getDaysOfWeek() {
        return daysOfWeek;
    }
}
