package model;
public class Attendance {
    private final String date;
    private final String status;

    public Attendance(String date, String status) {
        this.date = date;
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }
}
