/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;

/**
 *
 * @author DANAH
 */
public class InsightsFrame extends JFrame {
    public InsightsFrame(String instructorId) {
        setTitle("Instructor Insights");
        setSize(600, 400);
        setLocationRelativeTo(null);

        UserService userService = new UserService(new JsonDatabaseManager());
        InstructorInsights data;

        try {
            data = userService.getInstructorInsights(instructorId);
        } catch (Exception ex) {
            data = null;
        }

        JTextArea area = new JTextArea();
        area.setEditable(false);

        if (data != null) {
            area.setText(
                "Total Courses: " + data.totalCourses + "\n" +
                "Total Students: " + data.totalStudents + "\n" +
                "Average Quiz Score: " + data.averageQuiz
            );
        }

        add(new JScrollPane(area));
    }
}

