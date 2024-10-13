package view;
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import controller.AttendanceController;

public class AttendanceTrackerGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Attendance Tracker");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 300);
            frame.setLayout(new BorderLayout());

            AttendanceController controller = new AttendanceController();
            frame.add(controller.getMainPanel(), BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }
}
