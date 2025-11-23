import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class InstructorInsightsFrame extends JFrame {
    
    public InstructorInsightsFrame(String instructorId) {
        setTitle("Instructor Insights Dashboard");
        setSize(700, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JsonDatabaseManager db = JsonDatabaseManager.getInstance();
        
        try {
            // Get instructor's courses
            List<Course> allCourses = db.loadCourses();
            List<Course> instructorCourses = allCourses.stream()
                .filter(c -> c.getInstructorId().equals(instructorId))
                .toList();
            
            if (instructorCourses.isEmpty()) {
                JTextArea emptyArea = new JTextArea("You haven't created any courses yet.\n\n" +
                    "Create courses to see insights about your students!");
                emptyArea.setEditable(false);
                emptyArea.setFont(new Font("Arial", Font.PLAIN, 16));
                emptyArea.setMargin(new Insets(20, 20, 20, 20));
                add(new JScrollPane(emptyArea), BorderLayout.CENTER);
                return;
            }
            
            // Calculate statistics
            int totalCourses = instructorCourses.size();
            int totalStudents = 0;
            int totalLessons = 0;
            int totalQuizzes = 0;
            double totalQuizScores = 0;
            int quizCount = 0;
            
            List<User> allUsers = db.loadUsers();
            List<QuizResult> allQuizResults = db.loadQuizResults();
            
            StringBuilder courseDetails = new StringBuilder();
            
            for (Course course : instructorCourses) {
                int courseStudents = course.getStudents().size();
                totalStudents += courseStudents;
                
                int courseLessons = course.getLessons().size();
                totalLessons += courseLessons;
                
                int courseQuizzes = 0;
                for (Lesson lesson : course.getLessons()) {
                    if (lesson.getQuestions() != null && !lesson.getQuestions().isEmpty()) {
                        courseQuizzes++;
                    }
                }
                totalQuizzes += courseQuizzes;
                
                // Calculate completion rate for this course
                int totalPossibleCompletions = courseStudents * courseLessons;
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
                
                double completionRate = totalPossibleCompletions > 0 
                    ? (actualCompletions * 100.0 / totalPossibleCompletions) : 0;
                
                // Get average quiz score for this course
                List<QuizResult> courseQuizResults = allQuizResults.stream()
                    .filter(qr -> qr.getCourseId().equals(course.getCourseId()))
                    .toList();
                
                double avgQuizScore = 0;
                if (!courseQuizResults.isEmpty()) {
                    avgQuizScore = courseQuizResults.stream()
                        .mapToInt(QuizResult::getScore)
                        .average()
                        .orElse(0);
                    
                    totalQuizScores += avgQuizScore * courseQuizResults.size();
                    quizCount += courseQuizResults.size();
                }
                
                // Add to details
                courseDetails.append(String.format(
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "📚 Course: %s\n" +
                    "   ID: %s\n" +
                    "   Status: %s\n" +
                    "   👥 Students: %d\n" +
                    "   📖 Lessons: %d\n" +
                    "   ❓ Quizzes: %d\n" +
                    "   📊 Completion Rate: %.1f%%\n" +
                    "   🎯 Avg Quiz Score: %s\n\n",
                    course.getTitle(),
                    course.getCourseId(),
                    getStatusText(course.getApprovalStatus()),
                    courseStudents,
                    courseLessons,
                    courseQuizzes,
                    completionRate,
                    courseQuizResults.isEmpty() ? "No quizzes taken yet" : String.format("%.1f%%", avgQuizScore)
                ));
            }
            
            double overallAvgQuizScore = quizCount > 0 ? (totalQuizScores / quizCount) : 0;
            
            // Create main text area
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
            textArea.setMargin(new Insets(15, 15, 15, 15));
            
            String summary = String.format(
                "═══════════════════════════════════════════\n" +
                "       INSTRUCTOR INSIGHTS DASHBOARD\n" +
                "═══════════════════════════════════════════\n\n" +
                "📊 OVERALL STATISTICS\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "   Total Courses Created: %d\n" +
                "   Total Students Enrolled: %d\n" +
                "   Total Lessons: %d\n" +
                "   Total Quizzes: %d\n" +
                "   Overall Avg Quiz Score: %s\n" +
                "   Avg Students per Course: %.1f\n\n" +
                "📚 COURSE DETAILS\n",
                totalCourses,
                totalStudents,
                totalLessons,
                totalQuizzes,
                quizCount > 0 ? String.format("%.1f%%", overallAvgQuizScore) : "No data yet",
                totalCourses > 0 ? (totalStudents * 1.0 / totalCourses) : 0
            );
            
            textArea.setText(summary + courseDetails.toString());
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            add(scrollPane, BorderLayout.CENTER);
            
            // Add buttons panel
            JPanel buttonPanel = new JPanel();
            JButton refreshButton = new JButton("Refresh");
            JButton closeButton = new JButton("Close");
            
            refreshButton.addActionListener(e -> {
                dispose();
                new InstructorInsightsFrame(instructorId).setVisible(true);
            });
            
            closeButton.addActionListener(e -> dispose());
            
            buttonPanel.add(refreshButton);
            buttonPanel.add(closeButton);
            add(buttonPanel, BorderLayout.SOUTH);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading insights: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private String getStatusText(Course.ApprovalStatus status) {
        if (status == null) return "PENDING";
        switch (status) {
            case APPROVED: return "✓ APPROVED";
            case REJECTED: return "✗ REJECTED";
            case PENDING: return "⏳ PENDING";
            default: return "UNKNOWN";
        }
    }
}
