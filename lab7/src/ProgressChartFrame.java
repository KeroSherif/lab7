/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import java.io.IOException;
/**
 *
 * @author DANAH
 */
public class ProgressChartFrame extends JFrame {

    public ProgressChartFrame(String instructorId) {
        setTitle("Progress Chart");
        setSize(700, 500);
        setLocationRelativeTo(null);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        JsonDatabaseManager db = JsonDatabaseManager.getInstance();
        UserService userService = new UserService(db);

        try {
            for (Course course : db.loadCourses()) {
                if (course.getInstructorId().equals(instructorId)) {
                    double avgProgress = userService.getAverageProgressForCourse(course.getCourseId());
                    dataset.addValue(avgProgress, "Progress", course.getTitle());
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading chart: " + ex.getMessage());
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Average Course Progress",
                "Course",
                "Progress (%)",
                dataset
        );

        add(new ChartPanel(chart));
    }
}


