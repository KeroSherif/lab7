/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
/**
 *
 * @author DANAH
 */
public class ProgressChartFrame extends JFrame {
    public ProgressChartFrame(String studentId, String courseId) throws IOException {
        UserService userService = new UserService(new JsonDatabaseManager());

        double progress = userService.getCourseProgress(studentId, courseId);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(progress, "Progress", "Course");

        JFreeChart chart = ChartFactory.createLineChart(
                "Progress",
                "Course",
                "Progress %",
                dataset
        );

        add(new ChartPanel(chart));
        setSize(600, 400);
        setLocationRelativeTo(null);
    }
}

