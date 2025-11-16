/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;
/**
 *
 * @author DANAH
 */
public class InstructorDashboardFrame extends JFrame {

    private User currentUser;
    private InstructorService instructorService;
    private JsonDatabaseManager dbManager;

    public InstructorDashboardFrame(User user) {
        this.currentUser = user;
        this.dbManager = JsonDatabaseManager.getInstance();
        this.instructorService = new InstructorService(dbManager);
        
        setTitle("Instructor Dashboard - " + user.getUsername());
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 1));

        JLabel title = new JLabel("Welcome " + user.getUsername() + "!", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title);

        JButton createBtn = new JButton("Create Course");
        JButton manageBtn = new JButton("Manage Courses");
        JButton viewStudentsBtn = new JButton("View Enrolled Students");
        JButton logoutBtn = new JButton("Logout");

        add(createBtn);
        add(manageBtn);
        add(viewStudentsBtn);
        add(logoutBtn);

        // Create Course
        createBtn.addActionListener(e -> {
            showCreateCourseDialog();
        });

        // Manage Courses
        manageBtn.addActionListener(e -> {
            showManageCoursesDialog();
        });

        // View Students
        viewStudentsBtn.addActionListener(e -> {
            showViewStudentsDialog();
        });

        // Logout
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    // ==================== Create Course ====================
    private void showCreateCourseDialog() {
        JDialog dialog = new JDialog(this, "Create New Course", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 1, 10, 10));

        // Course ID
        JPanel idPanel = new JPanel();
        idPanel.add(new JLabel("Course ID:"));
        JTextField idField = new JTextField(20);
        idPanel.add(idField);
        dialog.add(idPanel);

        // Course Title
        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("Title:"));
        JTextField titleField = new JTextField(20);
        titlePanel.add(titleField);
        dialog.add(titlePanel);

        // Course Description
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
        JTextArea descArea = new JTextArea(5, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descArea);
        descPanel.add(scrollPane, BorderLayout.CENTER);
        dialog.add(descPanel);

        // Buttons
        JPanel btnPanel = new JPanel();
        JButton createBtn = new JButton("Create");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(createBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel);

        // Create button action
        createBtn.addActionListener(e -> {
            String courseId = idField.getText().trim();
            String title = titleField.getText().trim();
            String description = descArea.getText().trim();

            if (courseId.isEmpty() || title.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please fill all fields.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Course newCourse = instructorService.createCourse(
                    currentUser.getUserId(),
                    courseId,
                    title,
                    description,
                    currentUser.getUserId()
                );

                if (newCourse != null) {
                    JOptionPane.showMessageDialog(dialog,
                        "Course created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Failed to create course. Course ID may already exist.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error creating course: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    // ==================== Manage Courses ====================
    private void showManageCoursesDialog() {
        try {
            // Load instructor's courses
            List<Course> allCourses = dbManager.loadCourses();
            List<Course> instructorCourses = allCourses.stream()
                .filter(c -> c.getInstructorId().equals(currentUser.getUserId()))
                .collect(java.util.stream.Collectors.toList());

            if (instructorCourses.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "You haven't created any courses yet.",
                    "My Courses",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Create dialog
            JDialog dialog = new JDialog(this, "Manage My Courses", true);
            dialog.setSize(800, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            // Create table
            String[] columns = {"Course ID", "Title", "Description", "Students", "Lessons"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Course course : instructorCourses) {
                model.addRow(new Object[]{
                    course.getCourseId(),
                    course.getTitle(),
                    course.getDescription(),
                    course.getStudents().size(),
                    course.getLessons().size()
                });
            }

            JTable table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);
            dialog.add(scrollPane, BorderLayout.CENTER);

            // Buttons panel
            JPanel btnPanel = new JPanel();
            JButton editBtn = new JButton("Edit Course");
            JButton deleteBtn = new JButton("Delete Course");
            JButton manageLessonsBtn = new JButton("Manage Lessons");
            JButton closeBtn = new JButton("Close");
            btnPanel.add(editBtn);
            btnPanel.add(deleteBtn);
            btnPanel.add(manageLessonsBtn);
            btnPanel.add(closeBtn);
            dialog.add(btnPanel, BorderLayout.SOUTH);

            // Edit Course
            editBtn.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(dialog,
                        "Please select a course first.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String courseId = (String) model.getValueAt(selectedRow, 0);
                Course selectedCourse = instructorCourses.stream()
                    .filter(c -> c.getCourseId().equals(courseId))
                    .findFirst()
                    .orElse(null);

                if (selectedCourse != null) {
                    showEditCourseDialog(selectedCourse, dialog);
                }
            });

            // Delete Course
            deleteBtn.addActionListener(e -> {
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
                    "Are you sure you want to delete:\n" + courseTitle + " (" + courseId + ")?\n\nThis will unenroll all students!",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        boolean success = instructorService.deleteCourse(currentUser.getUserId(), courseId);
                        if (success) {
                            model.removeRow(selectedRow);
                            JOptionPane.showMessageDialog(dialog,
                                "Course deleted successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dialog,
                                "Failed to delete course.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error deleting course: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // Manage Lessons
            manageLessonsBtn.addActionListener(e -> {
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
                showManageLessonsDialog(courseId, courseTitle);
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

    // ==================== Edit Course ====================
    private void showEditCourseDialog(Course course, JDialog parentDialog) {
        JDialog dialog = new JDialog(this, "Edit Course", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 1, 10, 10));

        // Course ID (readonly)
        JPanel idPanel = new JPanel();
        idPanel.add(new JLabel("Course ID:"));
        JTextField idField = new JTextField(course.getCourseId(), 20);
        idField.setEditable(false);
        idPanel.add(idField);
        dialog.add(idPanel);

        // Course Title
        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("Title:"));
        JTextField titleField = new JTextField(course.getTitle(), 20);
        titlePanel.add(titleField);
        dialog.add(titlePanel);

        // Course Description
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
        JTextArea descArea = new JTextArea(course.getDescription(), 5, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descArea);
        descPanel.add(scrollPane, BorderLayout.CENTER);
        dialog.add(descPanel);

        // Buttons
        JPanel btnPanel = new JPanel();
        JButton saveBtn = new JButton("Save Changes");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel);

        // Save button action
        saveBtn.addActionListener(e -> {
            String newTitle = titleField.getText().trim();
            String newDescription = descArea.getText().trim();

            if (newTitle.isEmpty() || newDescription.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please fill all fields.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                boolean success = instructorService.updateCourse(
                    currentUser.getUserId(),
                    course.getCourseId(),
                    newTitle,
                    newDescription
                );

                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                        "Course updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    parentDialog.dispose();
                    showManageCoursesDialog();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Failed to update course.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error updating course: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    // ==================== Manage Lessons ====================
    private void showManageLessonsDialog(String courseId, String courseTitle) {
        try {
            Course course = dbManager.getCourseById(courseId);
            if (course == null) {
                JOptionPane.showMessageDialog(this,
                    "Course not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Lesson> lessons = course.getLessons();

            // Create dialog
            JDialog dialog = new JDialog(this, "Manage Lessons - " + courseTitle, true);
            dialog.setSize(800, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            // Create table
            String[] columns = {"Lesson ID", "Title", "Content Preview"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Lesson lesson : lessons) {
                String preview = lesson.getContent().length() > 50 
                    ? lesson.getContent().substring(0, 50) + "..." 
                    : lesson.getContent();
                model.addRow(new Object[]{
                    lesson.getLessonId(),
                    lesson.getTitle(),
                    preview
                });
            }

            JTable table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);
            dialog.add(scrollPane, BorderLayout.CENTER);

            // Buttons panel
            JPanel btnPanel = new JPanel();
            JButton addBtn = new JButton("Add Lesson");
            JButton editBtn = new JButton("Edit Lesson");
            JButton deleteBtn = new JButton("Delete Lesson");
            JButton backBtn = new JButton("Back");
            btnPanel.add(addBtn);
            btnPanel.add(editBtn);
            btnPanel.add(deleteBtn);
            btnPanel.add(backBtn);
            dialog.add(btnPanel, BorderLayout.SOUTH);

            // Add Lesson
            addBtn.addActionListener(e -> {
                showAddLessonDialog(courseId, dialog);
            });

            // Edit Lesson
            editBtn.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(dialog,
                        "Please select a lesson first.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String lessonId = (String) model.getValueAt(selectedRow, 0);
                Lesson selectedLesson = lessons.stream()
                    .filter(l -> l.getLessonId().equals(lessonId))
                    .findFirst()
                    .orElse(null);

                if (selectedLesson != null) {
                    showEditLessonDialog(courseId, selectedLesson, dialog);
                }
            });

            // Delete Lesson
            deleteBtn.addActionListener(e -> {
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

                int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Are you sure you want to delete:\n" + lessonTitle + " (" + lessonId + ")?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        boolean success = instructorService.removeLessonFromCourse(
                            currentUser.getUserId(), courseId, lessonId);
                        
                        if (success) {
                            model.removeRow(selectedRow);
                            JOptionPane.showMessageDialog(dialog,
                                "Lesson deleted successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dialog,
                                "Failed to delete lesson.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(dialog,
                            "Error deleting lesson: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            backBtn.addActionListener(e -> {
                dialog.dispose();
                showManageCoursesDialog();
            });

            dialog.setVisible(true);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading lessons: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== Add Lesson ====================
    private void showAddLessonDialog(String courseId, JDialog parentDialog) {
        JDialog dialog = new JDialog(this, "Add New Lesson", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 1, 10, 10));

        // Lesson ID
        JPanel idPanel = new JPanel();
        idPanel.add(new JLabel("Lesson ID:"));
        JTextField idField = new JTextField(20);
        idPanel.add(idField);
        dialog.add(idPanel);

        // Lesson Title
        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("Title:"));
        JTextField titleField = new JTextField(20);
        titlePanel.add(titleField);
        dialog.add(titlePanel);

        // Lesson Content
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Content:"), BorderLayout.NORTH);
        JTextArea contentArea = new JTextArea(5, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        dialog.add(contentPanel);

        // Buttons
        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(addBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel);

        // Add button action
        addBtn.addActionListener(e -> {
            String lessonId = idField.getText().trim();
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();

            if (lessonId.isEmpty() || title.isEmpty() || content.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please fill all fields.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            Lesson newLesson = new Lesson(lessonId, title, content);

            try {
                boolean success = instructorService.addLessonToCourse(
                    currentUser.getUserId(), courseId, newLesson);

                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                        "Lesson added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    parentDialog.dispose();
                    showManageCoursesDialog();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Failed to add lesson.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error adding lesson: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    // ==================== Edit Lesson ====================
    private void showEditLessonDialog(String courseId, Lesson lesson, JDialog parentDialog) {
        JDialog dialog = new JDialog(this, "Edit Lesson", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 1, 10, 10));

        // Lesson ID (readonly)
        JPanel idPanel = new JPanel();
        idPanel.add(new JLabel("Lesson ID:"));
        JTextField idField = new JTextField(lesson.getLessonId(), 20);
        idField.setEditable(false);
        idPanel.add(idField);
        dialog.add(idPanel);

        // Lesson Title
        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("Title:"));
        JTextField titleField = new JTextField(lesson.getTitle(), 20);
        titlePanel.add(titleField);
        dialog.add(titlePanel);

        // Lesson Content
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Content:"), BorderLayout.NORTH);
        JTextArea contentArea = new JTextArea(lesson.getContent(), 5, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        dialog.add(contentPanel);

        // Buttons
        JPanel btnPanel = new JPanel();
        JButton saveBtn = new JButton("Save Changes");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel);

        // Save button action
        saveBtn.addActionListener(e -> {
            String newTitle = titleField.getText().trim();
            String newContent = contentArea.getText().trim();

            if (newTitle.isEmpty() || newContent.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please fill all fields.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                boolean success = instructorService.updateLesson(
                    currentUser.getUserId(),
                    courseId,
                    lesson.getLessonId(),
                    newTitle,
                    newContent
                );

                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                        "Lesson updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    parentDialog.dispose();
                    showManageCoursesDialog();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Failed to update lesson.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Error updating lesson: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    // ==================== View Enrolled Students ====================
    private void showViewStudentsDialog() {
        try {
            // Load instructor's courses
            List<Course> allCourses = dbManager.loadCourses();
            List<Course> instructorCourses = allCourses.stream()
                .filter(c -> c.getInstructorId().equals(currentUser.getUserId()))
                .collect(java.util.stream.Collectors.toList());

            if (instructorCourses.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "You haven't created any courses yet.",
                    "View Students",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Create dialog
            JDialog dialog = new JDialog(this, "Select Course to View Students", true);
            dialog.setSize(700, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            // Create table
            String[] columns = {"Course ID", "Title", "Enrolled Students"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Course course : instructorCourses) {
                model.addRow(new Object[]{
                    course.getCourseId(),
                    course.getTitle(),
                    course.getStudents().size()
                });
            }

            JTable table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);
            dialog.add(scrollPane, BorderLayout.CENTER);

            // Buttons panel
            JPanel btnPanel = new JPanel();
            JButton viewBtn = new JButton("View Students");
            JButton closeBtn = new JButton("Close");
            btnPanel.add(viewBtn);
            btnPanel.add(closeBtn);
            dialog.add(btnPanel, BorderLayout.SOUTH);

            // View Students button
            viewBtn.addActionListener(e -> {
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
                showCourseStudentsDialog(courseId, courseTitle);
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

    // ==================== Show Course Students ====================
    private void showCourseStudentsDialog(String courseId, String courseTitle) {
        try {
            List<Student> students = instructorService.getEnrolledStudents(
                currentUser.getUserId(), courseId);

            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "No students enrolled in this course yet.",
                    "Course Students",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Create dialog
            JDialog dialog = new JDialog(this, "Students - " + courseTitle, true);
            dialog.setSize(700, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            // Create table
            String[] columns = {"Student ID", "Username", "Email", "Enrolled Courses"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Student student : students) {
                model.addRow(new Object[]{
                    student.getUserId(),
                    student.getUsername(),
                    student.getEmail(),
                    student.getEnrolledCourses().size()
                });
            }

            JTable table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);
            dialog.add(scrollPane, BorderLayout.CENTER);

            // Close button
            JPanel btnPanel = new JPanel();
            JButton closeBtn = new JButton("Close");
            btnPanel.add(closeBtn);
            dialog.add(btnPanel, BorderLayout.SOUTH);

            closeBtn.addActionListener(e -> {
                dialog.dispose();
                showViewStudentsDialog();
            });

            dialog.setVisible(true);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading students: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Constructor with no arguments (for compatibility)
    public InstructorDashboardFrame() {
        this(new Instructor());
    }
}