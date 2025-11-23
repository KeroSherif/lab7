import javax.swing.*;
import java.io.IOException;
import java.util.*;
import org.jfree.chart.*;
import org.jfree.data.general.DefaultPieDataset;

public class StudentEnrollmentPieChart extends JFrame {
    
    public StudentEnrollmentPieChart(String instructorId) {
        setTitle("Student Enrollment Distribution");
        setSize(700, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        DefaultPieDataset dataset = new DefaultPieDataset();
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
            
            for (Course course : instructorCourses) {
                int studentCount = course.getStudents().size();
                if (studentCount > 0) {
                    dataset.setValue(course.getTitle() + " (" + studentCount + ")", studentCount);
                }
            }
            
            if (dataset.getItemCount() == 0) {
                JOptionPane.showMessageDialog(this, 
                    "No students enrolled in your courses yet!", 
                    "No Data", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
                return;
            }
            
            JFreeChart chart = ChartFactory.createPieChart(
                "Student Enrollment Distribution by Course",
                dataset,
                true,
                true,
                false
            );
            
            ChartPanel chartPanel = new ChartPanel(chart);
            add(chartPanel);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading chart: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}