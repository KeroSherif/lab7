
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class StudentDashboardFrame extends JFrame {

    private User currentUser;
    private StudentService studentService;
    private JsonDatabaseManager dbManager;

    public StudentDashboardFrame(User user) {
        this.currentUser = user;
        this.dbManager = JsonDatabaseManager.getInstance();
        this.studentService = new StudentService(dbManager);
        setTitle("Student Dashboard - " + user.getUsername());
        setSize(600, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 1));

        JLabel title = new JLabel("Welcome " + user.getUsername() + "!", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title);

        JButton browseBtn = new JButton("Browse Courses");
        JButton enrolledBtn = new JButton("My Enrolled Courses");
        JButton profileBtn = new JButton("My Profile");
        JButton certBtn = new JButton("My Certificates");
        JButton logoutBtn = new JButton("Logout");

        add(browseBtn);
        add(enrolledBtn);
        add(profileBtn);
        add(certBtn);
        add(logoutBtn);

        browseBtn.addActionListener(e -> showBrowseCoursesDialog());
        enrolledBtn.addActionListener(e -> showEnrolledCoursesDialog());
        profileBtn.addActionListener(e -> showMyProfile());
        certBtn.addActionListener(e -> showCertificates());
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    // ==================== Browse All Courses ====================
    private void showBrowseCoursesDialog() {
        try {
            List<Course> allCourses = studentService.getAllCourses();
            if (allCourses.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No courses available yet.",
                        "Browse Courses",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JDialog dialog = new JDialog(this, "Browse Courses", true);
            dialog.setSize(700, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            String[] columns = {"Course ID", "Title", "Description", "Instructor ID"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            for (Course course : allCourses) {
                model.addRow(new Object[]{
                    course.getCourseId(),
                    course.getTitle(),
                    course.getDescription(),
                    course.getInstructorId()
                });
            }
            JTable table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);
            dialog.add(scrollPane, BorderLayout.CENTER);

            JPanel btnPanel = new JPanel();
            JButton enrollBtn = new JButton("Enroll in Selected Course");
            JButton closeBtn = new JButton("Close");
            btnPanel.add(enrollBtn);
            btnPanel.add(closeBtn);
            dialog.add(btnPanel, BorderLayout.SOUTH);

            enrollBtn.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(dialog, "Please select a course first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String courseId = (String) model.getValueAt(selectedRow, 0);
                String courseTitle = (String) model.getValueAt(selectedRow, 1);
                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "Do you want to enroll in:\n" + courseTitle + " (" + courseId + ")?",
                        "Confirm Enrollment", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        boolean success = studentService.enrollStudent(currentUser.getUserId(), courseId);
                        JOptionPane.showMessageDialog(dialog,
                                success ? "Successfully enrolled!" : "Failed to enroll.",
                                success ? "Success" : "Error",
                                success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            closeBtn.addActionListener(e -> dialog.dispose());
            dialog.setVisible(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== View Enrolled Courses ====================
    private void showEnrolledCoursesDialog() {
    try {
        // Refresh current user data from database first!
        currentUser = dbManager.findUserById(currentUser.getUserId()).orElse(currentUser);
        
        List<Course> enrolledCourses = studentService.getEnrolledCourses(currentUser.getUserId());
        if (enrolledCourses.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "You are not enrolled in any courses.", 
                "My Courses", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog(this, "My Enrolled Courses", true);
        dialog.setSize(900, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        String[] columns = {"Course ID", "Title", "Description", "Total Lessons", "Completed", "Progress %", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override 
            public boolean isCellEditable(int row, int column) { 
                return false; 
            }
        };
        
        Student student = (Student) currentUser;
        Map<String, List<String>> studentProgress = student.getProgress();
        
        for (Course course : enrolledCourses) {
            int totalLessons = course.getLessons() != null ? course.getLessons().size() : 0;
            int completedLessons = 0;
            
            if (studentProgress != null && studentProgress.containsKey(course.getCourseId())) {
                completedLessons = studentProgress.get(course.getCourseId()).size();
            }
            
            double progressPercent = totalLessons > 0 ? (completedLessons * 100.0 / totalLessons) : 0;
            String progressText = String.format("%.0f%%", progressPercent);
            
            String status;
            if (totalLessons == 0) {
                status = "No Lessons";
            } else if (completedLessons == totalLessons) {
                status = "✓ COMPLETED";
            } else if (completedLessons > 0) {
                status = "In Progress";
            } else {
                status = "Not Started";
            }
            
            model.addRow(new Object[]{
                course.getCourseId(),
                course.getTitle(),
                course.getDescription(),
                totalLessons,
                completedLessons,
                progressText,
                status
            });
        }
        
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Make the status column stand out with colors
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (column == 6 && !isSelected) { // Status column
                    String status = value.toString();
                    if (status.contains("COMPLETED")) {
                        c.setBackground(new java.awt.Color(200, 255, 200)); // Light green
                        c.setForeground(new java.awt.Color(0, 100, 0)); // Dark green
                    } else if (status.equals("In Progress")) {
                        c.setBackground(new java.awt.Color(255, 255, 200)); // Light yellow
                        c.setForeground(new java.awt.Color(100, 100, 0)); // Dark yellow
                    } else {
                        c.setBackground(java.awt.Color.WHITE);
                        c.setForeground(java.awt.Color.BLACK);
                    }
                } else if (!isSelected) {
                    c.setBackground(java.awt.Color.WHITE);
                    c.setForeground(java.awt.Color.BLACK);
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton viewLessonsBtn = new JButton("View Lessons");
        JButton refreshBtn = new JButton("Refresh");
        JButton closeBtn = new JButton("Close");
        
        btnPanel.add(viewLessonsBtn);
        btnPanel.add(refreshBtn);
        btnPanel.add(closeBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        viewLessonsBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please select a course first.", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            String courseId = (String) model.getValueAt(row, 0);
            String title = (String) model.getValueAt(row, 1);
            dialog.dispose();
            showCourseLessonsDialog(courseId, title);
        });
        
        refreshBtn.addActionListener(e -> {
            dialog.dispose();
            SwingUtilities.invokeLater(() -> showEnrolledCoursesDialog());
        });

        closeBtn.addActionListener(e -> dialog.dispose());
        
        dialog.setVisible(true);
    } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, 
            "Error loading enrolled courses: " + ex.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}

    // ==================== View Course Lessons ====================
  private void showCourseLessonsDialog(String courseId, String courseTitle) {
    try {
        Course course = dbManager.getCourseById(courseId);
        if (course == null || course.getLessons().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No lessons available for this course.", 
                "Info", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        List<Lesson> lessons = course.getLessons();
        Map<String, List<String>> progress = studentService.getStudentProgress(currentUser.getUserId(), courseId);
        List<String> completed = progress.getOrDefault(courseId, new ArrayList<>());

        JDialog dialog = new JDialog(this, "Lessons - " + courseTitle, true);
        dialog.setSize(900, 550);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);
        
        // Add instructions panel at the top
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
        instructionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel instructionLabel = new JLabel("<html><b>📚 Instructions:</b><br>" +
            "• Lessons <u>without quiz</u>: Click 'Mark as Completed' after studying<br>" +
            "• Lessons <u>with quiz</u>: Must take quiz and score ≥50% to complete<br>" +
            "• Complete all lessons to earn your certificate! 🏆</html>");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        instructionsPanel.add(instructionLabel);
        
        dialog.add(instructionsPanel, BorderLayout.NORTH);

        String[] cols = {"Lesson ID", "Title", "Status", "Quiz", "Score"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override 
            public boolean isCellEditable(int r, int c) { 
                return false; 
            }
        };

        // Load quiz results for this student
        List<QuizResult> allQuizResults = dbManager.loadQuizResults();
        
        for (Lesson lesson : lessons) {
            String status = completed.contains(lesson.getLessonId()) ? "✓ Completed" : "Not Completed";
            String hasQuiz = (lesson.getQuestions() != null && !lesson.getQuestions().isEmpty()) ? "Yes" : "No";
            
            // Find the latest quiz score for this lesson
            String scoreText = "-";
            if (lesson.getQuestions() != null && !lesson.getQuestions().isEmpty()) {
                QuizResult latestResult = allQuizResults.stream()
                    .filter(qr -> qr.getStudentId().equals(currentUser.getUserId()) 
                               && qr.getLessonId().equals(lesson.getLessonId()))
                    .reduce((first, second) -> second) // Get the last one
                    .orElse(null);
                
                if (latestResult != null) {
                    scoreText = latestResult.getScore() + "%";
                }
            }
            
            model.addRow(new Object[]{
                lesson.getLessonId(), 
                lesson.getTitle(), 
                status, 
                hasQuiz,
                scoreText
            });
        }

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        
        // Color code the table rows
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String status = (String) model.getValueAt(row, 2); // Status column
                    String hasQuiz = (String) model.getValueAt(row, 3); // Quiz column
                    
                    if (status.contains("Completed")) {
                        c.setBackground(new java.awt.Color(220, 255, 220)); // Light green
                    } else if ("Yes".equals(hasQuiz)) {
                        c.setBackground(new java.awt.Color(255, 240, 220)); // Light orange (has quiz)
                    } else {
                        c.setBackground(java.awt.Color.WHITE);
                    }
                    c.setForeground(java.awt.Color.BLACK);
                } else {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                
                return c;
            }
        });
        
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton viewContent = new JButton("View Content");
        JButton takeQuiz = new JButton("Take Quiz");
        JButton markComplete = new JButton("Mark as Completed");
        JButton back = new JButton("Back");

        viewContent.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String lid = (String) model.getValueAt(row, 0);
                showLessonContent(courseId, lid);
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Please select a lesson first.", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        takeQuiz.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please select a lesson first.", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            String lid = (String) model.getValueAt(row, 0);
            Lesson lesson = lessons.stream()
                .filter(l -> l.getLessonId().equals(lid))
                .findFirst()
                .orElse(null);

            if (lesson == null) {
                JOptionPane.showMessageDialog(dialog, 
                    "Lesson not found.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (lesson.getQuestions() == null || lesson.getQuestions().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "This lesson has no quiz.", 
                    "Info", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Take the quiz
            QuizTakerDialog quizDialog = new QuizTakerDialog(this, lesson.getTitle(), lesson.getQuestions());
            quizDialog.setVisible(true);

            // Save quiz result and update completion
            if (quizDialog.isQuizCompleted()) {
                try {
                    // Save quiz result
                    QuizResult qr = new QuizResult(
                        currentUser.getUserId(),
                        courseId,
                        lid,
                        quizDialog.getScore()
                    );
                    dbManager.saveQuizResult(qr);
                    
                    // Mark lesson as completed
                    boolean success = studentService.completeLesson(
                        currentUser.getUserId(), 
                        courseId, 
                        lid
                    );
                    
                    if (success) {
                        // Update table
                        model.setValueAt("✓ Completed", row, 2);
                        model.setValueAt(quizDialog.getScore() + "%", row, 4);
                        
                        // Refresh user data from database
                        try {
                            currentUser = dbManager.findUserById(currentUser.getUserId()).orElse(currentUser);
                        } catch (IOException refreshEx) {
                            System.err.println("Error refreshing user data: " + refreshEx.getMessage());
                        }
                        
                        // Check if course is completed
                        if (studentService.isCourseCompleted(currentUser.getUserId(), courseId)) {
                            JOptionPane.showMessageDialog(dialog, 
                                "🎉 Congratulations!\n\nYou have completed all lessons in this course!\n" +
                                "A certificate has been generated for you.", 
                                "Course Completed!", 
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            Student student = (Student) currentUser;
                            studentService.generateCertificate(student, courseId);
                            
                            // Close dialog and refresh course list
                            dialog.dispose();
                            SwingUtilities.invokeLater(() -> showEnrolledCoursesDialog());
                        }
                    } else {
                        JOptionPane.showMessageDialog(dialog, 
                            "Failed to mark lesson as completed.", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Error saving quiz result: " + ex.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        markComplete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please select a lesson first.", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            String lid = (String) model.getValueAt(row, 0);
            String hasQuiz = (String) model.getValueAt(row, 3);
            String currentScore = (String) model.getValueAt(row, 4);

            // If lesson has a quiz, MUST take and pass it first!
            if ("Yes".equals(hasQuiz)) {
                if ("-".equals(currentScore)) {
                    JOptionPane.showMessageDialog(dialog, 
                        "⚠️ This lesson has a quiz!\n\n" +
                        "You MUST take the quiz and score at least 50% to complete this lesson.\n\n" +
                        "Click 'Take Quiz' button first.", 
                        "Quiz Required", 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Check if score is passing (>= 50%)
                try {
                    int score = Integer.parseInt(currentScore.replace("%", ""));
                    if (score < 50) {
                        JOptionPane.showMessageDialog(dialog, 
                            "❌ You scored " + score + "% on the quiz.\n\n" +
                            "You need at least 50% to complete this lesson.\n\n" +
                            "Please retake the quiz.", 
                            "Quiz Not Passed", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Error reading quiz score. Please take the quiz again.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // If we reach here, quiz is passed - lesson should already be marked complete
                JOptionPane.showMessageDialog(dialog, 
                    "✓ This lesson is already completed!\n\n" +
                    "You passed the quiz with " + currentScore, 
                    "Already Completed", 
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // No quiz - allow manual completion
            try {
                boolean success = studentService.completeLesson(
                    currentUser.getUserId(), 
                    courseId, 
                    lid
                );
                
                if (success) {
                    model.setValueAt("✓ Completed", row, 2);
                    
                    // Refresh user data from database
                    try {
                        currentUser = dbManager.findUserById(currentUser.getUserId()).orElse(currentUser);
                    } catch (IOException refreshEx) {
                        System.err.println("Error refreshing user data: " + refreshEx.getMessage());
                    }
                    
                    JOptionPane.showMessageDialog(dialog, 
                        "Lesson marked as completed!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Check course completion
                    if (studentService.isCourseCompleted(currentUser.getUserId(), courseId)) {
                        JOptionPane.showMessageDialog(dialog, 
                            "🎉 Congratulations!\n\nYou have completed all lessons in this course!\n" +
                            "A certificate has been generated for you.", 
                            "Course Completed!", 
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        Student student = (Student) currentUser;
                        studentService.generateCertificate(student, courseId);
                        
                        // Close dialog and refresh course list
                        dialog.dispose();
                        SwingUtilities.invokeLater(() -> showEnrolledCoursesDialog());
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, 
                        "Lesson is already completed or error occurred.", 
                        "Info", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Error: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        back.addActionListener(e -> {
            dialog.dispose();
            // Refresh the enrolled courses dialog to show updated progress
            SwingUtilities.invokeLater(() -> showEnrolledCoursesDialog());
        });

        btnPanel.add(viewContent);
        btnPanel.add(takeQuiz);
        btnPanel.add(markComplete);
        btnPanel.add(back);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, 
            "Error loading lessons: " + ex.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}

    // ==================== View Lesson Content ====================
    private void showLessonContent(String courseId, String lessonId) {
        try {
            Lesson lesson = studentService.getLessonById(courseId, lessonId);
            if (lesson == null) {
                JOptionPane.showMessageDialog(this, "Lesson not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JDialog dialog = new JDialog(this, "Lesson: " + lesson.getTitle(), true);
            dialog.setSize(600, 400);
            dialog.setLayout(new BorderLayout());

            JTextArea content = new JTextArea(lesson.getContent());
            content.setEditable(false);
            content.setLineWrap(true);
            content.setWrapStyleWord(true);
            dialog.add(new JScrollPane(content), BorderLayout.CENTER);

            JButton close = new JButton("Close");
            close.addActionListener(e -> dialog.dispose());
            dialog.add(close, BorderLayout.SOUTH);

            dialog.setVisible(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading lesson: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== View Certificates ====================
    private void showCertificates() {
        try {
            Student student = (Student) currentUser;
            List<Map<String, String>> certs = student.getCertificates();
            if (certs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You have no certificates yet.", "Certificates", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            StringBuilder sb = new StringBuilder("=== My Certificates ===\n\n");
            for (Map<String, String> cert : certs) {
                sb.append("Certificate ID: ").append(cert.get("certificateId")).append("\n")
                        .append("Course ID: ").append(cert.get("courseId")).append("\n")
                        .append("Issue Date: ").append(cert.get("issueDate")).append("\n")
                        .append("-------------------------------\n");
            }
            JTextArea area = new JTextArea(sb.toString());
            area.setEditable(false);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "My Certificates", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading certificates: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== Profile & Auth ====================
    private void showMyProfile() {
        UserService userService = new UserService(dbManager);
        try {
            int[] stats = userService.getStudentStatistics(currentUser.getUserId());
            List<Course> enrolledCourses = studentService.getEnrolledCourses(currentUser.getUserId());
            int totalLessons = 0;
            for (Course c : enrolledCourses) {
                totalLessons += (c.getLessons() != null ? c.getLessons().size() : 0);
            }
            double completionRate = (totalLessons > 0) ? (stats[1] * 100.0 / totalLessons) : 0.0;

            Student student = (Student) currentUser;
            String notificationsText = student.getNotifications().isEmpty() ? "None" : String.join("\n• ", student.getNotifications());

            String message = String.format(
                    "=== My Profile ===\n"
                    + "User ID: %s\n"
                    + "Username: %s\n"
                    + "Email: %s\n"
                    + "Role: %s\n"
                    + "=== My Statistics ===\n"
                    + "Enrolled Courses: %d\n"
                    + "Completed Lessons: %d / %d\n"
                    + "Completion Rate: %.1f%%\n"
                    + "=== Notifications ===\n• %s",
                    currentUser.getUserId(),
                    currentUser.getUsername(),
                    currentUser.getEmail(),
                    currentUser.getRole(),
                    stats[0],
                    stats[1],
                    totalLessons,
                    completionRate,
                    notificationsText
            );
            String[] options = {"Edit Profile", "Change Password", "Close"};
            int choice = JOptionPane.showOptionDialog(this,
                    message,
                    "My Profile",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[2]);
            if (choice == 0) {
                showEditProfileDialog();
            } else if (choice == 1) {
                showChangePasswordDialog();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading profile: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEditProfileDialog() {
        UserService userService = new UserService(dbManager);
        String newUsername = JOptionPane.showInputDialog(this, "Enter new username:", currentUser.getUsername());
        if (newUsername == null || newUsername.trim().isEmpty()) {
            return;
        }
        String newEmail = JOptionPane.showInputDialog(this, "Enter new email:", currentUser.getEmail());
        if (newEmail == null || newEmail.trim().isEmpty()) {
            return;
        }
        try {
            boolean success = userService.updateUser(currentUser.getUserId(), newUsername.trim(), newEmail.trim());
            if (success) {
                currentUser.setUsername(newUsername.trim());
                currentUser.setEmail(newEmail.trim());
                setTitle("Student Dashboard - " + newUsername.trim());
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile. Email may be taken.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error updating profile: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showChangePasswordDialog() {
        UserService userService = new UserService(dbManager);
        String oldPassword = JOptionPane.showInputDialog(this, "Enter old password:");
        if (oldPassword == null || oldPassword.isEmpty()) {
            return;
        }
        String newPassword = JOptionPane.showInputDialog(this, "Enter new password:");
        if (newPassword == null || newPassword.isEmpty()) {
            return;
        }
        String confirmPassword = JOptionPane.showInputDialog(this, "Confirm new password:");
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            boolean success = userService.updateUserPassword(currentUser.getUserId(), oldPassword, newPassword);
            if (success) {
                JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to change password. Old password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error changing password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
