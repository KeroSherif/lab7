/*
 * Instructor Insights Dashboard
 */
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class InstructorInsightsFrame extends JFrame {

    public InstructorInsightsFrame(String instructorId) {
        setTitle("Instructor Insights");
        setSize(550, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        UserService userService = new UserService(JsonDatabaseManager.getInstance());
        int[] stats;
        try {
            stats = userService.getInstructorStatistics(instructorId);
        } catch (IOException e) {
            stats = null;
        }

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setMargin(new Insets(10, 10, 10, 10));

        if (stats != null && stats.length >= 2) {
            int createdCourses = stats[0];
            int totalStudents = stats[1];
            textArea.setText(
                "=== Instructor Insights ===\n\n" +
                "• Created Courses: " + createdCourses + "\n" +
                "• Total Enrolled Students: " + totalStudents + "\n\n" +
                "Note: Quiz analytics and charts will be available\n" +
                "once quizzes are implemented in lessons."
            );
        } else {
            textArea.setText("Error loading instructor statistics.");
        }

        add(new JScrollPane(textArea), BorderLayout.CENTER);
       
        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}