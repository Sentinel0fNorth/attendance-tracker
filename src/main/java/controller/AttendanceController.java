package controller;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import model.Attendance;
import model.DateLabelFormatter;
import model.ExcelHandler;
import model.Subject;

public class AttendanceController {
    private final JPanel mainPanel;
    private final JButton recordAttendanceButton;
    private final JButton addSubjectButton;
    private final JButton showTotalAttendanceButton;
    private final JButton exitButton;
    private final JTextArea outputArea;

    private final List<Subject> subjects = new ArrayList<>();
    private final Map<String, List<Attendance>> attendanceRecords = new HashMap<>();

    public AttendanceController() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(5, 1));

        recordAttendanceButton = new JButton("Record Attendance");
        addSubjectButton = new JButton("Add Subject");
        showTotalAttendanceButton = new JButton("Show Total Attendance");
        exitButton = new JButton("Exit");
        outputArea = new JTextArea();

        mainPanel.add(recordAttendanceButton);
        mainPanel.add(addSubjectButton);
        mainPanel.add(showTotalAttendanceButton);
        mainPanel.add(exitButton);
        mainPanel.add(new JScrollPane(outputArea));

        recordAttendanceButton.addActionListener((ActionEvent e) -> {
            recordAttendance();
        });

        addSubjectButton.addActionListener((ActionEvent e) -> {
            addSubject();
        });

        showTotalAttendanceButton.addActionListener((ActionEvent e) -> {
            showTotalAttendance();
        });

        exitButton.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });

        loadData();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void recordAttendance() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Date Picker
        UtilDateModel model = new UtilDateModel();
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        panel.add(new JLabel("Select Date:"));
        panel.add(datePicker);

        int result = JOptionPane.showConfirmDialog(null, panel, "Select Date", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Date selectedDate = (Date) datePicker.getModel().getValue();
            if (selectedDate != null) {
                String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);
                for (Subject subject : subjects) {
                    if (subject.getDaysOfWeek().contains(getDayOfWeek(selectedDate))) {
                        if (attendanceRecords.containsKey(subject.getName())) {
                            List<Attendance> records = attendanceRecords.get(subject.getName());
                            Optional<Attendance> existingRecord = records.stream()
                                    .filter(a -> a.getDate().equals(dateStr))
                                    .findFirst();
                            if (existingRecord.isPresent()) {
                                int overwrite = JOptionPane.showConfirmDialog(null,
                                        "Attendance for " + subject.getName() + " on " + dateStr + " already exists. Overwrite?",
                                        "Overwrite Confirmation", JOptionPane.YES_NO_OPTION);
                                if (overwrite == JOptionPane.NO_OPTION) {
                                    continue;
                                } else {
                                    records.remove(existingRecord.get());
                                }
                            }
                        }

                        JPanel statusPanel = new JPanel();
                        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
                        JLabel label = new JLabel("Attendance for " + subject.getName() + ":");
                        JRadioButton presentButton = new JRadioButton("Present");
                        JRadioButton absentButton = new JRadioButton("Absent");
                        JRadioButton noClassButton = new JRadioButton("No Class");
                        ButtonGroup group = new ButtonGroup();
                        group.add(presentButton);
                        group.add(absentButton);
                        group.add(noClassButton);
                        statusPanel.add(label);
                        statusPanel.add(presentButton);
                        statusPanel.add(absentButton);
                        statusPanel.add(noClassButton);

                        int statusResult = JOptionPane.showConfirmDialog(null, statusPanel, "Record Attendance", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                        if (statusResult == JOptionPane.OK_OPTION) {
                            String status = presentButton.isSelected() ? "Present" : absentButton.isSelected() ? "Absent" : "No Class";
                            attendanceRecords.computeIfAbsent(subject.getName(), k -> new ArrayList<>())
                                             .add(new Attendance(dateStr, status));
                        }
                    }
                }
                saveData();
                outputArea.append("Attendance recorded for " + dateStr + ".\n");
            }
        }
    }


    private void addSubject() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JTextField nameField = new JTextField(10);
        panel.add(new JLabel("Enter subject name:"));
        panel.add(nameField);

        panel.add(new JLabel("Select days of the week:"));
        JCheckBox[] dayCheckBoxes = new JCheckBox[7];
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < days.length; i++) {
            dayCheckBoxes[i] = new JCheckBox(days[i]);
            panel.add(dayCheckBoxes[i]);
        }

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Subject", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText();
            List<String> daysOfWeek = new ArrayList<>();
            for (JCheckBox checkBox : dayCheckBoxes) {
                if (checkBox.isSelected()) {
                    daysOfWeek.add(checkBox.getText());
                }
            }
            subjects.add(new Subject(name, daysOfWeek));
            saveData();
            outputArea.append("Subject added: " + name + "\n");
        }
    }

    private void showTotalAttendance() {
        StringBuilder report = new StringBuilder();
        for (Map.Entry<String, List<Attendance>> entry : attendanceRecords.entrySet()) {
            String subjectName = entry.getKey();
            List<Attendance> records = entry.getValue();
            long presentCount = records.stream().filter(a -> "Present".equalsIgnoreCase(a.getStatus())).count();
            long totalCount = records.size();
            double percentage = (double) presentCount / totalCount * 100;
            report.append(subjectName).append(": ").append(String.format("%.2f", percentage)).append("% present\n");
        }
        outputArea.setText(report.toString());
    }

    private void loadData() {
        ExcelHandler.readExcel(subjects, attendanceRecords);
    }

    private void saveData() {
        ExcelHandler.writeExcel(subjects, attendanceRecords);
    }

    private String getDayOfWeek(Date selectedDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return switch (day) {
            case Calendar.SUNDAY -> "Sun";
            case Calendar.MONDAY -> "Mon";
            case Calendar.TUESDAY -> "Tue";
            case Calendar.WEDNESDAY -> "Wed";
            case Calendar.THURSDAY -> "Thu";
            case Calendar.FRIDAY -> "Fri";
            case Calendar.SATURDAY -> "Sat";
            default -> "";
        };
    }
}
