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
        setLayout(new GridLayout(5, 1));

        JLabel title = new JLabel("Welcome " + user.getUsername() + "!", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title);

        JButton browseBtn = new JButton("Browse Courses");
        JButton enrolledBtn = new JButton("My Enrolled Courses");
        JButton profileBtn = new JButton("My Profile");
        JButton logoutBtn = new JButton("Logout");

        add(browseBtn);
        add(enrolledBtn);
        add(profileBtn);
        add(logoutBtn);

        browseBtn.addActionListener(e -> showBrowseCoursesDialog());
        enrolledBtn.addActionListener(e -> showEnrolledCoursesDialog());
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
            List<Course> enrolledCourses = studentService.getEnrolledCourses(currentUser.getUserId());
            if (enrolledCourses.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You are not enrolled in any courses.", "My Courses", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JDialog dialog = new JDialog(this, "My Enrolled Courses", true);
            dialog.setSize(800, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            String[] columns = {"Course ID", "Title", "Description", "Lessons", "Progress"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            for (Course course : enrolledCourses) {
                int totalLessons = course.getLessons() != null ? course.getLessons().size() : 0;
                int completed = 0;
                try {
                    Map<String, List<String>> prog = studentService.getStudentProgress(currentUser.getUserId(), course.getCourseId());
                    completed = (prog != null && prog.get(course.getCourseId()) != null) ? prog.get(course.getCourseId()).size() : 0;
                } catch (IOException ignored) {}
                String progress = completed + " / " + totalLessons + (totalLessons > 0 && completed == totalLessons ? " (COMPLETED!)" : "");
                model.addRow(new Object[]{
                    course.getCourseId(),
                    course.getTitle(),
                    course.getDescription(),
                    totalLessons,
                    progress
                });
            }
            JTable table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);
            dialog.add(scrollPane, BorderLayout.CENTER);

            JPanel btnPanel = new JPanel();
            JButton viewLessonsBtn = new JButton("View Lessons");
            JButton closeBtn = new JButton("Close");
            btnPanel.add(viewLessonsBtn);
            btnPanel.add(closeBtn);
            dialog.add(btnPanel, BorderLayout.SOUTH);

            viewLessonsBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(dialog, "Select a course first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String courseId = (String) model.getValueAt(row, 0);
                String title = (String) model.getValueAt(row, 1);
                dialog.dispose();
                showCourseLessonsDialog(courseId, title);
            });

            closeBtn.addActionListener(e -> dialog.dispose());
            dialog.setVisible(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading enrolled courses: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCourseLessonsDialog(String courseId, String courseTitle) {
        JOptionPane.showMessageDialog(this, "Lessons view not implemented yet.", "Info", JOptionPane.INFORMATION_MESSAGE);
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
            String message = String.format(
                "=== My Profile ===\n" +
                "User ID: %s\n" +
                "Username: %s\n" +
                "Email: %s\n" +
                "Role: %s\n" +
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
        if (newUsername == null || newUsername.trim().isEmpty()) return;
        String newEmail = JOptionPane.showInputDialog(this,
                "Enter new email:",
                currentUser.getEmail());
        if (newEmail == null || newEmail.trim().isEmpty()) return;
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
        if (oldPassword == null || oldPassword.isEmpty()) return;
        String newPassword = JOptionPane.showInputDialog(this, "Enter new password:");
        if (newPassword == null || newPassword.isEmpty()) return;
        String confirmPassword = JOptionPane.showInputDialog(this, "Confirm new password:");
        if (confirmPassword == null || confirmPassword.isEmpty()) return;
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