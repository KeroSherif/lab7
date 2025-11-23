import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class ProgressChartFrame extends JFrame {

    public ProgressChartFrame(String instructorId) {
        setTitle("Course Completion Progress Chart");
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
            
            List<User> allUsers = db.loadUsers();
            
            for (Course course : instructorCourses) {
                int totalLessons = course.getLessons().size();
                int totalStudents = course.getStudents().size();
                
                if (totalLessons == 0 || totalStudents == 0) {
                    dataset.addValue(0, "Progress", course.getTitle());
                    continue;
                }
                
                int totalPossibleCompletions = totalStudents * totalLessons;
                int actualCompletions = 0;
                
                for (String studentId : course.getStudents()) {
                    Optional<User> studentOpt = allUsers.stream()
                        .filter(u -> u.getUserId().equals(studentId) && u instanceof Student)
                        .findFirst();
                    
                    if (studentOpt.isPresent()) {
                        Student student = (Student) studentOpt.get();
                        Map<String, List<String>> progress = student.getProgress();
                        if (progress != null && progress.containsKey(course.getCourseId())) {
                            actualCompletions += progress.get(course.getCourseId()).size();
                        }
                    }
                }
                
                double completionRate = (actualCompletions * 100.0) / totalPossibleCompletions;
                dataset.addValue(completionRate, "Completion %", course.getTitle());
            }
            
            JFreeChart chart = ChartFactory.createBarChart(
                "Course Completion Progress by Students",
                "Course",
                "Completion Rate (%)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
            );
            
            ChartPanel chartPanel = new ChartPanel(chart);
            add(chartPanel);
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading chart data: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}