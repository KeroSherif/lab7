import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author DANAH
 */
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
        setLayout(new GridLayout(5, 1)); // غير من 4 لـ 5

        JLabel title = new JLabel("Welcome " + user.getUsername() + "!", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title);

        JButton browseBtn = new JButton("Browse Courses");
        JButton enrolledBtn = new JButton("My Enrolled Courses");
        JButton profileBtn = new JButton("My Profile"); // جديد
        JButton logoutBtn = new JButton("Logout");

        add(browseBtn);
        add(enrolledBtn);
        add(profileBtn); // جديد
        add(logoutBtn);

        browseBtn.addActionListener(e -> showBrowseCoursesDialog());
        enrolledBtn.addActionListener(e -> showEnrolledCoursesDialog());
        // My Profile - جديد
        profileBtn.addActionListener(e -> showMyProfile());
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

            // Create dialog
            JDialog dialog = new JDialog(this, "Browse Courses", true);
            dialog.setSize(700, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            // Create table
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

            // Buttons panel
            JPanel btnPanel = new JPanel();
            JButton enrollBtn = new JButton("Enroll in Selected Course");
            JButton closeBtn = new JButton("Close");
            btnPanel.add(enrollBtn);
            btnPanel.add(closeBtn);
            dialog.add(btnPanel, BorderLayout.SOUTH);

            // Enroll button action
            enrollBtn.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(dialog,
                            "Please select a course first.",
                            "No Selection",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String courseId = (String) model.getValueAt(selectedRow, 0);
                String courseTitle = (String) model.getValueAt(selectedRow, 1);

                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "Do you want to enroll in:\n" + courseTitle + " (" + courseId + ")?",
                        "Confirm Enrollment",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        boolean success = studentService.enrollStudent(currentUser.getUserId(), courseId);
                        if (success) {
                            JOptionPane.showMessageDialog(dialog,
                                    "Successfully enrolled in " + courseTitle + "!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dialog,
                                    "Failed to enroll. You may already be enrolled.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(dialog,
                                "Error enrolling: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            closeBtn.addActionListener(e -> dialog.dispose());
            dialog.setVisible(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading courses: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== View Enrolled Courses ====================
    private void showEnrolledCoursesDialog() {
        try {
            List<Course> enrolledCourses = studentService.getEnrolledCourses(currentUser.getUserId());
            if (enrolledCourses.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "You are not enrolled in any courses yet.",
                        "My Courses",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Create dialog
            JDialog dialog = new JDialog(this, "My Enrolled Courses", true);
            dialog.setSize(800, 500); // Updated size for new column
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            // Create table
            String[] columns = {"Course ID", "Title", "Description", "Lessons Count", "Progress"}; // Added Progress column
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Course course : enrolledCourses) {
                // Calculate completed lessons count for this course for the current student
                int completedLessonsCount = 0;
                try {
                    Map<String, List<String>> progress = studentService.getStudentProgress(currentUser.getUserId(), course.getCourseId());
                    if (progress != null) {
                        List<String> completedLessons = progress.get(course.getCourseId());
                        if (completedLessons != null) {
                            completedLessonsCount = completedLessons.size();
                        }
                    }
                } catch (IOException ex) {
                    // Log the error or handle it gracefully, defaulting to 0 completed
                    System.err.println("Error getting progress for course " + course.getCourseId() + ": " + ex.getMessage());
                }

                int totalLessonsCount = course.getLessons() != null ? course.getLessons().size() : 0;
                String progressText = completedLessonsCount + " / " + totalLessonsCount + " Lessons";
                String statusText = "";
                if (totalLessonsCount > 0 && completedLessonsCount == totalLessonsCount) {
                    statusText = " (COMPLETED!)";
                }

                model.addRow(new Object[]{
                    course.getCourseId(),
                    course.getTitle(),
                    course.getDescription(),
                    course.getLessons() != null ? course.getLessons().size() : 0,
                    progressText + statusText // Populate the new Progress column
                });
            }

            JTable table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);
            dialog.add(scrollPane, BorderLayout.CENTER);

            // Buttons panel
            JPanel btnPanel = new JPanel();
            JButton viewLessonsBtn = new JButton("View Lessons");
            JButton closeBtn = new JButton("Close");
            btnPanel.add(viewLessonsBtn);
            btnPanel.add(closeBtn);
            dialog.add(btnPanel, BorderLayout.SOUTH);

            // View Lessons button action
            viewLessonsBtn.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(dialog,
                            "Please select a course first.",
                            "No Selection",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String courseId = (String) model.getValueAt(selectedRow, 0);
                String courseTitle = (String) model.getValueAt(selectedRow, 1);

                // Check if course is completed and show a message before opening lessons
                try {
                    if (studentService.isCourseCompleted(currentUser.getUserId(), courseId)) { // Assuming isCourseCompleted is available here
                        JOptionPane.showMessageDialog(dialog,
                                "You have already completed this course: " + courseTitle,
                                "Course Completed",
                                JOptionPane.INFORMATION_MESSAGE);
                        // Optionally, you can still open the lessons dialog but inform the user
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Error checking course completion status: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return; // Don't proceed if there's an error checking status
                }

                dialog.dispose();
                showCourseLessonsDialog(courseId, courseTitle);
            });

            closeBtn.addActionListener(e -> dialog.dispose());
            dialog.setVisible(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading enrolled courses: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== View Course Lessons ====================
    private void showCourseLessonsDialog(String courseId, String courseTitle) {
        try {
            List<Lesson> lessons = studentService.getCourseLessons(courseId);
            if (lessons == null || lessons.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No lessons available in this course yet.",
                        "Course Lessons",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Get student progress
            Map<String, List<String>> progress = studentService.getStudentProgress(
                    currentUser.getUserId(), courseId);
            List<String> completedLessons = (progress != null) ? progress.get(courseId) : null;
            if (completedLessons == null) {
                completedLessons = new ArrayList<>();
            }

            // Check if the course is completed
            boolean isCourseCompleted = false;
            try {
                isCourseCompleted = studentService.isCourseCompleted(currentUser.getUserId(), courseId);
            } catch (IOException ex) {
                // If check fails, keep false and continue (we already handle errors later)
                System.err.println("Error checking completion status: " + ex.getMessage());
            }

            // Create dialog
            JDialog dialog = new JDialog(this, "Lessons - " + courseTitle + (isCourseCompleted ? " (COMPLETED!)" : ""), true);
            dialog.setSize(800, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            // Create table
            String[] columns = {"Lesson ID", "Title", "Status"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Lesson lesson : lessons) {
                String status = completedLessons.contains(lesson.getLessonId())
                        ? "✓ Completed"
                        : "Not Completed";
                model.addRow(new Object[]{
                    lesson.getLessonId(),
                    lesson.getTitle(),
                    status
                });
            }
            JTable table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);
            dialog.add(scrollPane, BorderLayout.CENTER);

            // Buttons panel
            JPanel btnPanel = new JPanel();
            JButton viewContentBtn = new JButton("View Content");
            JButton markCompleteBtn = new JButton("Mark as Completed");
            JButton backBtn = new JButton("Back");

           // Disable "Mark as Completed" button if course is already completed
            if (isCourseCompleted) {
                markCompleteBtn.setEnabled(false);
                markCompleteBtn.setText("Course Completed!");
            } else {
               
                table.getSelectionModel().addListSelectionListener(e -> {
                    // Use the local variable isCourseCompleted from the method scope
                    boolean currentIsCourseCompleted = false;
                    try {
                        currentIsCourseCompleted = studentService.isCourseCompleted(currentUser.getUserId(), courseId);
                    } catch (IOException ioEx) {
                        // Log error or keep the previous state if check fails
                        System.err.println("Error re-checking course completion: " + ioEx.getMessage());
                    }
                    // Enable only if a row is selected AND the course is not completed
                    markCompleteBtn.setEnabled(table.getSelectedRow() >= 0 && !currentIsCourseCompleted);
                });
                // Ensure button is enabled initially if no row is selected but course is not completed
                markCompleteBtn.setEnabled(!isCourseCompleted);
            }

            btnPanel.add(viewContentBtn);
            btnPanel.add(markCompleteBtn);
            btnPanel.add(backBtn);
            dialog.add(btnPanel, BorderLayout.SOUTH);
            // View Content button
            viewContentBtn.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(dialog,
                            "Please select a lesson first.",
                            "No Selection",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String lessonId = (String) model.getValueAt(selectedRow, 0);
                showLessonContent(courseId, lessonId);
            });

            // Mark Complete button
            markCompleteBtn.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(dialog,
                            "Please select a lesson first.",
                            "No Selection",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // Check again if course is not already completed before attempting to complete a lesson
                try {
                    if (studentService.isCourseCompleted(currentUser.getUserId(), courseId)) {
                        JOptionPane.showMessageDialog(dialog,
                                "This course is already marked as completed!",
                                "Course Completed",
                                JOptionPane.INFORMATION_MESSAGE);
                        // Refresh dialog to reflect state
                        dialog.dispose();
                        showCourseLessonsDialog(courseId, courseTitle);
                        return;
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Error checking course completion status: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String lessonId = (String) model.getValueAt(selectedRow, 0);
                String lessonTitle = (String) model.getValueAt(selectedRow, 1);
                try {
                    boolean success = studentService.completeLesson(
                            currentUser.getUserId(), courseId, lessonId);
                    if (success) {
                        model.setValueAt("✓ Completed", selectedRow, 2);
                        // Re-check if the entire course is now completed
                        if (studentService.isCourseCompleted(currentUser.getUserId(), courseId)) {
                            JOptionPane.showMessageDialog(dialog,
                                    "Congratulations! You have completed the course: " + courseTitle,
                                    "Course Completed",
                                    JOptionPane.INFORMATION_MESSAGE);

                            // Add notification to student's profile
                            try {
                                List<User> allUsers = dbManager.loadUsers();
                                Optional<User> currentStudentOpt = allUsers.stream()
                                        .filter(u -> u.getUserId().equals(currentUser.getUserId()) && u instanceof Student)
                                        .findFirst();
                                if (currentStudentOpt.isPresent()) {
                                    Student currentStudent = (Student) currentStudentOpt.get();
                                    String notification = "You completed the course: " + courseTitle + " on " + new java.util.Date(); // Use current date/time
                                    currentStudent.addNotification(notification);
                                    dbManager.saveUsers(allUsers); // Save the updated user list
                                    System.out.println("Added notification to student " + currentUser.getUserId() + ": " + notification);
                                }
                            } catch (IOException ioEx) {
                                System.err.println("Error saving notification: " + ioEx.getMessage());
                                // Optionally, show an error message to the user, but don't break the flow
                            }

                            // Disable the button and update title after completion
                            markCompleteBtn.setEnabled(false);
                            markCompleteBtn.setText("Course Completed!");
                            dialog.setTitle("Lessons - " + courseTitle + " (COMPLETED!)");

                            // --- Add Certificate Button ---
                            btnPanel.removeAll(); // Clear existing buttons
                            JButton certificateBtn = new JButton("Download Certificate");
                            btnPanel.add(certificateBtn);
                            btnPanel.add(backBtn); // Add back button again

                            certificateBtn.addActionListener(certE -> {
                                // Temporary action: Show a message or print to console
                                JOptionPane.showMessageDialog(dialog,
                                        "Certificate for '" + courseTitle + "' would be downloaded now!",
                                        "Certificate Downloaded",
                                        JOptionPane.INFORMATION_MESSAGE);
                            });
                            dialog.revalidate(); // Refresh the panel
                            dialog.repaint();
                        } else {
                            JOptionPane.showMessageDialog(dialog,
                                    "Lesson '" + lessonTitle + "' marked as completed!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(dialog,
                                "Failed to mark lesson as completed. You might already be enrolled or an error occurred.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog,
                            "Error marking lesson: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            backBtn.addActionListener(e -> {
                dialog.dispose();
                showEnrolledCoursesDialog();
            });

            dialog.setVisible(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading lessons: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== View Lesson Content ====================
    private void showLessonContent(String courseId, String lessonId) {
        try {
            Lesson lesson = studentService.getLessonById(courseId, lessonId);
            if (lesson == null) {
                JOptionPane.showMessageDialog(this,
                        "Lesson not found.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create dialog
            JDialog dialog = new JDialog(this, "Lesson: " + lesson.getTitle(), true);
            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            // Content area
            JTextArea contentArea = new JTextArea(lesson.getContent());
            contentArea.setEditable(false);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setFont(new Font("Arial", Font.PLAIN, 14));
            contentArea.setMargin(new Insets(10, 10, 10, 10));
            JScrollPane scrollPane = new JScrollPane(contentArea);
            dialog.add(scrollPane, BorderLayout.CENTER);

            // Resources section
            if (lesson.getResources() != null && !lesson.getResources().isEmpty()) {
                JPanel resourcePanel = new JPanel(new BorderLayout());
                resourcePanel.setBorder(BorderFactory.createTitledBorder("Resources"));
                JTextArea resourceArea = new JTextArea();
                resourceArea.setEditable(false);
                resourceArea.setText(String.join("\n", lesson.getResources()));
                resourcePanel.add(new JScrollPane(resourceArea), BorderLayout.CENTER);
                resourcePanel.setPreferredSize(new Dimension(600, 100));
                dialog.add(resourcePanel, BorderLayout.SOUTH);
            }

            // Close button
            JButton closeBtn = new JButton("Close");
            closeBtn.addActionListener(e -> dialog.dispose());
            JPanel btnPanel = new JPanel();
            btnPanel.add(closeBtn);
            dialog.add(btnPanel, BorderLayout.NORTH);

            dialog.setVisible(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading lesson: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    public StudentDashboardFrame() {
        this(new Student());
    }

 

    private void showMyProfile() {
        UserService userService = new UserService(dbManager);
        try {
            int[] stats = userService.getStudentStatistics(currentUser.getUserId());
            List<Course> enrolledCourses = studentService.getEnrolledCourses(currentUser.getUserId());
            int totalLessons = 0;
            for (Course c : enrolledCourses) {
                totalLessons += c.getLessons().size();
            }
            double completionRate = (totalLessons > 0) ? (stats[1] * 100.0 / totalLessons) : 0.0;

            String message = String.format(
                "=== My Profile ===\n" +
                "User ID: %s\n" +
                "Username: %s\n" +
                "Email: %s\n" +
                "Role: %s\n\n" +
                "=== My Statistics ===\n" +
                "Enrolled Courses: %d\n" +
                "Completed Lessons: %d / %d\n" +
                "Completion Rate: %.1f%%",
                currentUser.getUserId(),
                currentUser.getUsername(),
                currentUser.getEmail(),
                currentUser.getRole(),
                stats[0],
                stats[1],
                totalLessons,
                completionRate
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
        String newUsername = JOptionPane.showInputDialog(this,
                "Enter new username:",
                currentUser.getUsername());
        if (newUsername == null || newUsername.trim().isEmpty()) {
            return;
        }
        String newEmail = JOptionPane.showInputDialog(this,
                "Enter new email:",
                currentUser.getEmail());
        if (newEmail == null || newEmail.trim().isEmpty()) {
            return;
        }
        try {
            boolean success = userService.updateUser(
                    currentUser.getUserId(),
                    newUsername.trim(),
                    newEmail.trim()
            );
            if (success) {
                currentUser.setUsername(newUsername.trim());
                currentUser.setEmail(newEmail.trim());
                setTitle("Student Dashboard - " + newUsername.trim());
                JOptionPane.showMessageDialog(this,
                        "Profile updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update profile. Email may be taken.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating profile: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 6 characters.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            boolean success = userService.updateUserPassword(
                    currentUser.getUserId(),
                    oldPassword,
                    newPassword
            );
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Password changed successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to change password. Old password is incorrect.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error changing password: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    JButton certBtn = new JButton("View Certificate");
    panel.add(certBtn);
    certBtn.addActionListener(e -> {
    StudentService service = new StudentService();
    List<Lesson> lessons = JsonDatabaseManager.getLessonsByCourseId(selectedCourseId);

    if (service.isCourseCompleted(currentStudent, lessons)) {
        Map<String, String> cert = service.generateCertificate(currentStudent, selectedCourseId);
        JOptionPane.showMessageDialog(this,
            "Certificate Generated!\nCertificate ID: " + cert.get("certificateId")
        );
    } else {
        JOptionPane.showMessageDialog(this,
            "You must complete all lessons and quizzes before earning the certificate."
        );
    }
    });


}