/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import java.awt.*;

/**
 *
 * @author DANAH
 */
public class QuizBarChartFrame extends JFrame {
    public QuizBarChartFrame(String courseId) throws IOException {
        JsonDatabaseManager db = new JsonDatabaseManager();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<Lesson> lessons = db.getLessonsByCourseId(courseId);

        for (Lesson l : lessons) {
            double avg = db.getQuizResultsForLesson(l.getLessonId())
                    .stream()
                    .mapToInt(QuizResult::getScore)
                    .average().orElse(0);

            dataset.addValue(avg, "Average Score", l.getTitle());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Quiz Averages",
                "Lesson",
                "Score",
                dataset
        );

        add(new ChartPanel(chart));
        setSize(800, 600);
        setLocationRelativeTo(null);
    }
}

