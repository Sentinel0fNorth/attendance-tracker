package model;
import java.io.*;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelHandler {
    private static final String FILE_NAME = "attendance.xlsx";

    public static void readExcel(List<Subject> subjects, Map<String, List<Attendance>> attendanceRecords) {
        try (FileInputStream fis = new FileInputStream(FILE_NAME);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet subjectSheet = workbook.getSheet("Subjects");
            for (Row row : subjectSheet) {
                String name = row.getCell(0).getStringCellValue();
                String days = row.getCell(1).getStringCellValue();
                List<String> daysOfWeek = Arrays.asList(days.split(","));
                subjects.add(new Subject(name, daysOfWeek));
            }

            Sheet attendanceSheet = workbook.getSheet("Attendance");
            for (Row row : attendanceSheet) {
                String subjectName = row.getCell(0).getStringCellValue();
                String date = row.getCell(1).getStringCellValue();
                String status = row.getCell(2).getStringCellValue();
                attendanceRecords.computeIfAbsent(subjectName, k -> new ArrayList<>())
                                 .add(new Attendance(date, status));
            }
        } catch (IOException e) {
            System.out.println("Error reading Excel file: " + e.getMessage());
        }
    }

    public static void writeExcel(List<Subject> subjects, Map<String, List<Attendance>> attendanceRecords) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet subjectSheet = workbook.createSheet("Subjects");
            int rowNum = 0;
            for (Subject subject : subjects) {
                Row row = subjectSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(subject.getName());
                row.createCell(1).setCellValue(String.join(",", subject.getDaysOfWeek()));
            }

            Sheet attendanceSheet = workbook.createSheet("Attendance");
            rowNum = 0;
            for (Map.Entry<String, List<Attendance>> entry : attendanceRecords.entrySet()) {
                for (Attendance attendance : entry.getValue()) {
                    Row row = attendanceSheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(entry.getKey());
                    row.createCell(1).setCellValue(attendance.getDate());
                    Cell statusCell = row.createCell(2);
                    statusCell.setCellValue(attendance.getStatus());

                    // Apply color coding
                    CellStyle style = workbook.createCellStyle();
                    if (attendance.getStatus().equalsIgnoreCase("Present")) {
                        style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
                    } else if (attendance.getStatus().equalsIgnoreCase("Absent")) {
                        style.setFillForegroundColor(IndexedColors.RED.getIndex());
                    } else {
                        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                    }
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    statusCell.setCellStyle(style);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(FILE_NAME)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            System.out.println("Error writing Excel file: " + e.getMessage());
        }
    }
}
