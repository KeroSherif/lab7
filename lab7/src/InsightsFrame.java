
import javax.swing.*;
import java.awt.*;

/**
 *
 * @author DANAH
 */
public class InsightsFrame extends JFrame {
    public InsightsFrame(String instructorId) {
        setTitle("Instructor Insights");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 

        UserService userService = new UserService(new JsonDatabaseManager());
        int[] data = null;

        try {
            data = userService.getInstructorStatistics(instructorId);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Failed to load instructor insights:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        if (data != null && data.length >= 3) {
            area.setText(
                "Total Courses: " + data[0] + "\n" +
                "Total Students: " + data[1] + "\n" +
                "Average Quiz Score: " + data[2] + "%"  // لو بييجي كنسبة
            );
        } else {
            area.setText("No statistics available for this instructor.");
        }

        add(new JScrollPane(area));
    }
}