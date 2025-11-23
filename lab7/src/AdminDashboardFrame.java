import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class AdminDashboardFrame extends JFrame {

    private User admin;
    private AdminService adminService;

    public AdminDashboardFrame(User adminUser) {
        this.admin = adminUser;
        this.adminService = new AdminService(JsonDatabaseManager.getInstance());

        setTitle("Admin Dashboard");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Welcome, Admin " + admin.getUsername(), SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(4, 1, 10, 10));
        JButton reviewBtn = new JButton("Review Pending Courses");
        JButton manageUsersBtn = new JButton("Manage Users");
        JButton myProfileBtn = new JButton("My Profile");
        JButton logoutBtn = new JButton("Logout");

        centerPanel.add(reviewBtn);
        centerPanel.add(manageUsersBtn);
        centerPanel.add(myProfileBtn);
        centerPanel.add(logoutBtn);
        add(centerPanel, BorderLayout.CENTER);

        reviewBtn.addActionListener(e -> openPendingCourses());
        manageUsersBtn.addActionListener(e -> openUserManagement());
        myProfileBtn.addActionListener(e -> showMyProfile());
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    private void openPendingCourses() {
        try {
            List<Course> pending = adminService.getPendingCourses();
            if (pending.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No pending courses to review.");
                return;
            }

            JDialog dialog = new JDialog(this, "Pending Courses", true);
            dialog.setSize(800, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            String[] columns = {"Course ID", "Title", "Instructor ID"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            for (Course c : pending) {
                model.addRow(new Object[]{c.getCourseId(), c.getTitle(), c.getInstructorId()});
            }

            JTable table = new JTable(model);
            dialog.add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel btnPanel = new JPanel();
            JButton approveBtn = new JButton("Approve");
            JButton rejectBtn = new JButton("Reject");
            JButton closeBtn = new JButton("Close");
            btnPanel.add(approveBtn);
            btnPanel.add(rejectBtn);
            btnPanel.add(closeBtn);
            dialog.add(btnPanel, BorderLayout.SOUTH);

            approveBtn.addActionListener(e -> {
                int selected = table.getSelectedRow();
                if (selected == -1) return;
                String id = (String) model.getValueAt(selected, 0);
                try {
                    adminService.approveCourse(id);
                    model.removeRow(selected);
                    JOptionPane.showMessageDialog(dialog, "Course approved.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            });

            rejectBtn.addActionListener(e -> {
                int selected = table.getSelectedRow();
                if (selected == -1) return;
                String id = (String) model.getValueAt(selected, 0);
                try {
                    adminService.rejectCourse(id);
                    model.removeRow(selected);
                    JOptionPane.showMessageDialog(dialog, "Course rejected.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                }
            });

            closeBtn.addActionListener(e -> dialog.dispose());

            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void openUserManagement() {
        try {
            List<User> users = adminService.getAllUsers();
            if (users.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No users to manage.");
                return;
            }

            JDialog dialog = new JDialog(this, "Manage Users", true);
            dialog.setSize(800, 500);
            dialog.setLayout(new BorderLayout());

            String[] cols = {"User ID", "Username", "Email", "Role"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };
            for (User u : users) {
                model.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getEmail(), u.getRole()});
            }

            JTable table = new JTable(model);
            dialog.add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel btnPanel = new JPanel();
            JButton deleteBtn = new JButton("Delete User");
            JButton promoteBtn = new JButton("Promote to Admin");
            JButton closeBtn = new JButton("Close");
            btnPanel.add(deleteBtn);
            btnPanel.add(promoteBtn);
            btnPanel.add(closeBtn);
            dialog.add(btnPanel, BorderLayout.SOUTH);

            deleteBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row == -1) return;
                String userId = (String) model.getValueAt(row, 0);
                String username = (String) model.getValueAt(row, 1);
                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "Delete user: " + username + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        adminService.deleteUser(userId);
                        model.removeRow(row);
                        JOptionPane.showMessageDialog(dialog, "User deleted.");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                    }
                }
            });

            promoteBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row == -1) return;
                String userId = (String) model.getValueAt(row, 0);
                String username = (String) model.getValueAt(row, 1);
                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "Promote " + username + " to Admin?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        adminService.promoteToAdmin(userId);
                        model.removeRow(row);
                        JOptionPane.showMessageDialog(dialog, "User promoted to Admin!");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
                    }
                }
            });

            closeBtn.addActionListener(e -> dialog.dispose());

            dialog.setVisible(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + ex.getMessage());
        }
    }

    private void showMyProfile() {
        JsonDatabaseManager db = JsonDatabaseManager.getInstance();
        try {
            List<User> allUsers = db.loadUsers();
            List<Course> allCourses = db.loadCourses();

            long totalUsers = allUsers.size();
            long totalCourses = allCourses.size();
            long pending = allCourses.stream().filter(c -> c.getApprovalStatus() == Course.ApprovalStatus.PENDING).count();
            long approved = allCourses.stream().filter(c -> c.getApprovalStatus() == Course.ApprovalStatus.APPROVED).count();
            long rejected = allCourses.stream().filter(c -> c.getApprovalStatus() == Course.ApprovalStatus.REJECTED).count();

            String message = String.format(
                "=== My Profile ===\n" +
                "User ID: %s\n" +
                "Username: %s\n" +
                "Email: %s\n" +
                "Role: %s\n\n" +
                "=== System Statistics ===\n" +
                "Total Users: %d\n" +
                "Total Courses: %d\n" +
                "Pending: %d | Approved: %d | Rejected: %d",
                admin.getUserId(),
                admin.getUsername(),
                admin.getEmail(),
                admin.getRole(),
                totalUsers,
                totalCourses,
                pending, approved, rejected
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
        UserService userService = new UserService(JsonDatabaseManager.getInstance());
        String newUsername = JOptionPane.showInputDialog(this,
                "Enter new username:",
                admin.getUsername());
        if (newUsername == null || newUsername.trim().isEmpty()) return;

        String newEmail = JOptionPane.showInputDialog(this,
                "Enter new email:",
                admin.getEmail());
        if (newEmail == null || newEmail.trim().isEmpty()) return;

        String err = Validation.validateUsername(newUsername.trim());
        if (!err.isEmpty()) {
            JOptionPane.showMessageDialog(this, err, "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        err = Validation.validateEmail(newEmail.trim());
        if (!err.isEmpty()) {
            JOptionPane.showMessageDialog(this, err, "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean success = userService.updateUser(
                    admin.getUserId(),
                    newUsername.trim(),
                    newEmail.trim()
            );
            if (success) {
                admin.setUsername(newUsername.trim());
                admin.setEmail(newEmail.trim());
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update profile. Email may be taken.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error updating profile: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showChangePasswordDialog() {
        UserService userService = new UserService(JsonDatabaseManager.getInstance());
        String oldPassword = JOptionPane.showInputDialog(this, "Enter old password:");
        if (oldPassword == null || oldPassword.isEmpty()) return;

        String newPassword = JOptionPane.showInputDialog(this, "Enter new password:");
        if (newPassword == null || newPassword.isEmpty()) return;

        String confirmPassword = JOptionPane.showInputDialog(this, "Confirm new password:");
        if (confirmPassword == null || confirmPassword.isEmpty()) return;

        String err = Validation.validatePassword(newPassword);
        if (!err.isEmpty()) {
            JOptionPane.showMessageDialog(this, err, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        err = Validation.validatePasswordMatch(newPassword, confirmPassword);
        if (!err.isEmpty()) {
            JOptionPane.showMessageDialog(this, err, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            boolean success = userService.updateUserPassword(
                    admin.getUserId(),
                    oldPassword,
                    newPassword
            );
            if (success) {
                JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Old password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error changing password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}