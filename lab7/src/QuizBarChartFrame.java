/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import java.io.IOException;
import org.jfree.chart.*;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author DANAH
 */
public class QuizBarChartFrame extends JFrame {

    public QuizBarChartFrame(String instructorId) {
        setTitle("Quiz Averages Chart");
        setSize(700, 500);
        setLocationRelativeTo(null);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        JsonDatabaseManager db = JsonDatabaseManager.getInstance();

        try {
            for (Course course : db.loadCourses()) {
                if (course.getInstructorId().equals(instructorId)) {
                    for (Lesson lesson : course.getLessons()) {
                        double avg = db.getQuizResultsForLesson(lesson.getLessonId())
                                .stream().mapToInt(QuizResult::getScore)
                                .average().orElse(0);
                        dataset.addValue(avg, "Score", lesson.getTitle());
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading chart: " + e.getMessage());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Quiz Average per Lesson",
                "Lesson",
                "Average Score",
                dataset
        );

        add(new ChartPanel(chart));
    }
}
