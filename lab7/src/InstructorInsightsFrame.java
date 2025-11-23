/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
/**
 *
 * @author DANAH
 */
public class InstructorInsightsFrame extends JFrame {

    public InstructorInsightsFrame(String instructorId) {
        setTitle("Instructor Insights");
        setSize(500, 400);
        setLocationRelativeTo(null);

        UserService userService = new UserService(JsonDatabaseManager.getInstance());

        InstructorInsights insights;
        try {
            insights = userService.getInstructorInsights(instructorId);
        } catch (IOException e) {
            insights = null;
        }

        JTextArea area = new JTextArea();
        area.setEditable(false);

        if (insights != null) {
            area.setText(
                    "=== Instructor Insights ===\n\n" +
                    "Total Courses: " + insights.totalCourses + "\n" +
                    "Total Students: " + insights.totalStudents + "\n" +
                    "Average Quiz Score: " + insights.averageQuiz + "\n"
            );
        } else {
            area.setText("Error loading insights.");
        }

        add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton progressChartBtn = new JButton("Progress Chart");
        JButton quizChartBtn = new JButton("Quiz Averages Chart");

        buttons.add(progressChartBtn);
        buttons.add(quizChartBtn);
        add(buttons, BorderLayout.SOUTH);

        progressChartBtn.addActionListener(e -> {
            new ProgressChartFrame(instructorId).setVisible(true);
        });

        quizChartBtn.addActionListener(e -> {
            new QuizBarChartFrame(instructorId).setVisible(true);
        });
    }
}

