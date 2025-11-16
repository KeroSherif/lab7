/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
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
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1));

        JLabel title = new JLabel("Welcome " + user.getUsername() + "!", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title);

        JButton browseBtn = new JButton("Browse Courses");
        JButton enrolledBtn = new JButton("My Enrolled Courses");
        JButton logoutBtn = new JButton("Logout");

        add(browseBtn);
        add(enrolledBtn);
        add(logoutBtn);

        // Browse All Available Courses
        browseBtn.addActionListener(e -> {
            showBrowseCoursesDialog();
        });

        // View Enrolled Courses
        enrolledBtn.addActionListener(e -> {
            showEnrolledCoursesDialog();
        });

        // Logout
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
                    JOptionPane.YES_NO_OPTION);

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
            dialog.setSize(700, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            // Create table
            String[] columns = {"Course ID", "Title", "Description", "Lessons Count"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Course course : enrolledCourses) {
                model.addRow(new Object[]{
                    course.getCourseId(),
                    course.getTitle(),
                    course.getDescription(),
                    course.getLessons().size()
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

            if (lessons.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No lessons available in this course yet.",
                    "Course Lessons",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Get student progress
            Map<String, List<String>> progress = studentService.getStudentProgress(
                currentUser.getUserId(), courseId);
            List<String> completedLessons = progress.get(courseId);

            // Create dialog
            JDialog dialog = new JDialog(this, "Lessons - " + courseTitle, true);
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

                String lessonId = (String) model.getValueAt(selectedRow, 0);
                String lessonTitle = (String) model.getValueAt(selectedRow, 1);

                try {
                    boolean success = studentService.completeLesson(
                        currentUser.getUserId(), courseId, lessonId);
                    
                    if (success) {
                        model.setValueAt("✓ Completed", selectedRow, 2);
                        JOptionPane.showMessageDialog(dialog,
                            "Lesson marked as completed!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
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
            if (!lesson.getResources().isEmpty()) {
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

    // Constructor with no arguments (for compatibility)
    public StudentDashboardFrame() {
        this(new Student());
    }
}