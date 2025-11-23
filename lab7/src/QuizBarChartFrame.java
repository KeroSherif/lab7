import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class QuizBarChartFrame extends JFrame {

    public QuizBarChartFrame(String instructorId) {
        setTitle("Quiz Performance Chart");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        JsonDatabaseManager db = JsonDatabaseManager.getInstance();

        try {
            List<Course> allCourses = db.loadCourses();
            List<Course> instructorCourses = allCourses.stream()
                .filter(c -> c.getInstructorId().equals(instructorId))
                .toList();
            
            if (instructorCourses.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "You haven't created any courses yet!", 
                    "No Data", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
                return;
            }
            
            List<QuizResult> allQuizResults = db.loadQuizResults();
            boolean hasQuizData = false;
            
            for (Course course : instructorCourses) {
                for (Lesson lesson : course.getLessons()) {
                    // Check if lesson has quiz
                    if (lesson.getQuestions() == null || lesson.getQuestions().isEmpty()) {
                        continue;
                    }
                    
                    // Get quiz results for this lesson
                    List<QuizResult> lessonResults = allQuizResults.stream()
                        .filter(qr -> qr.getLessonId().equals(lesson.getLessonId()))
                        .toList();
                    
                    if (!lessonResults.isEmpty()) {
                        hasQuizData = true;
                        double avgScore = lessonResults.stream()
                            .mapToInt(QuizResult::getScore)
                            .average()
                            .orElse(0);
                        
                        String label = course.getTitle() + " - " + lesson.getTitle();
                        if (label.length() > 30) {
                            label = label.substring(0, 27) + "...";
                        }
                        
                        dataset.addValue(avgScore, "Avg Score", label);
                    }
                }
            }
            
            if (!hasQuizData) {
                JOptionPane.showMessageDialog(this, 
                    "No quiz data available yet!\n\n" +
                    "Students need to take quizzes for data to appear here.", 
                    "No Quiz Data", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
                return;
            }
            
            JFreeChart chart = ChartFactory.createBarChart(
                "Average Quiz Scores per Lesson",
                "Lesson",
                "Average Score (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            );
            
            // Customize chart
            chart.getCategoryPlot().getRangeAxis().setRange(0, 100);
            
            ChartPanel chartPanel = new ChartPanel(chart);
            add(chartPanel);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading quiz chart: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}