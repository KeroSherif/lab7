
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class InstructorDashboardFrame extends JFrame {

    private User currentUser;
    private InstructorService instructorService;
    private JsonDatabaseManager dbManager;

    public InstructorDashboardFrame(User user) {
        this.currentUser = user;
        this.dbManager = JsonDatabaseManager.getInstance();
        this.instructorService = new InstructorService(dbManager);

        setTitle("Instructor Dashboard - " + user.getUsername());
        setSize(600, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(7, 1));

        JLabel title = new JLabel("Welcome " + user.getUsername() + "!", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title);

        JButton createBtn = new JButton("Create Course");
        JButton manageBtn = new JButton("Manage Courses");
        JButton viewStudentsBtn = new JButton("View Enrolled Students");
        JButton insightsBtn = new JButton("View Insights");
        JButton chartsBtn = new JButton("View Charts");
        JButton myProfileBtn = new JButton("My Profile");
        JButton logoutBtn = new JButton("Logout");

        add(createBtn);
        add(manageBtn);
        add(viewStudentsBtn);
        add(insightsBtn);
        add(chartsBtn);
        add(myProfileBtn);
        add(logoutBtn);

        // Action Listeners
        createBtn.addActionListener(e -> showCreateCourseDialog());
        manageBtn.addActionListener(e -> showManageCoursesDialog());
        viewStudentsBtn.addActionListener(e -> showViewStudentsDialog());

        insightsBtn.addActionListener(e -> {
            new InstructorInsightsFrame(currentUser.getUserId()).setVisible(true);
        });

        chartsBtn.addActionListener(e -> {
            String[] options = {
                " Course Completion Progress",
                " Quiz Performance",
                " Student Enrollment",
                " View All Charts"
            };

            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Select which chart you want to view:",
                    "Charts & Analytics",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[3]
            );

            if (choice >= 0) {
                final String userId = currentUser.getUserId();
                SwingUtilities.invokeLater(() -> {
                    try {
                        switch (choice) {
                            case 0:
                                new ProgressChartFrame(userId).setVisible(true);
                                break;
                            case 1:
                                new QuizBarChartFrame(userId).setVisible(true);
                                break;
                            case 2:
                                new StudentEnrollmentPieChart(userId).setVisible(true);
                                break;
                            case 3:
                                new ProgressChartFrame(userId).setVisible(true);
                                new QuizBarChartFrame(userId).setVisible(true);
                                new StudentEnrollmentPieChart(userId).setVisible(true);
                                break;
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this,
                                "Error opening chart: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                });
            }
        });

        myProfileBtn.addActionListener(e -> showMyProfile());

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    // ==================== Manage Quiz Dialog ====================
    private List<Question> showManageQuizDialog(JDialog parent, List<Question> existingQuestions) {
        List<Question> tempQuestions = (existingQuestions != null) ? new ArrayList<>(existingQuestions) : new ArrayList<>();
        JDialog dialog = new JDialog(parent, "Manage Quiz", true);
        dialog.setSize(600, 500);
        dialog.setLayout(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(new String[]{"Question", "Correct Answer"}, 0);
        for (Question q : tempQuestions) {
            model.addRow(new Object[]{q.getQuestionText(), q.getOptions().get(q.getCorrectOptionIndex())});
        }

        JTable table = new JTable(model);
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(6, 2));
        JTextField qField = new JTextField();
        JTextField opt1 = new JTextField();
        JTextField opt2 = new JTextField();
        JTextField opt3 = new JTextField();
        JTextField opt4 = new JTextField();
        JComboBox<String> correctBox = new JComboBox<>(new String[]{"Option 1", "Option 2", "Option 3", "Option 4"});

        inputPanel.add(new JLabel("Question:"));
        inputPanel.add(qField);
        inputPanel.add(new JLabel("Option 1:"));
        inputPanel.add(opt1);
        inputPanel.add(new JLabel("Option 2:"));
        inputPanel.add(opt2);
        inputPanel.add(new JLabel("Option 3:"));
        inputPanel.add(opt3);
        inputPanel.add(new JLabel("Option 4:"));
        inputPanel.add(opt4);
        inputPanel.add(new JLabel("Correct One:"));
        inputPanel.add(correctBox);

        dialog.add(inputPanel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton("Add Question");
        JButton saveBtn = new JButton("Save Quiz");

        addBtn.addActionListener(e -> {
            String qText = qField.getText().trim();
            List<String> opts = new ArrayList<>();
            opts.add(opt1.getText());
            opts.add(opt2.getText());
            opts.add(opt3.getText());
            opts.add(opt4.getText());
            String error = Validation.validateQuestion(qText, opts);
            if (!error.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, error, "Validation Error", JOptionPane.WARNING_MESSAGE);
                return; // Stop here if invalid
            }
            Question q = new Question(qText, opts, correctBox.getSelectedIndex());
            tempQuestions.add(q);
            model.addRow(new Object[]{q.getQuestionText(), opts.get(q.getCorrectOptionIndex())});
            qField.setText("");
            opt1.setText("");
            opt2.setText("");
            opt3.setText("");
            opt4.setText("");
        });

        saveBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(addBtn);
        btnPanel.add(saveBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
        return tempQuestions;
    }

    // ==================== Create Course ====================
    private void showCreateCourseDialog() {
        JDialog dialog = new JDialog(this, "Create New Course", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 1, 10, 10));

        JPanel idPanel = new JPanel();
        idPanel.add(new JLabel("Course ID:"));
        JTextField idField = new JTextField(20);
        idPanel.add(idField);
        dialog.add(idPanel);

        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("Title:"));
        JTextField titleField = new JTextField(20);
        titlePanel.add(titleField);
        dialog.add(titlePanel);

        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
        JTextArea descArea = new JTextArea(5, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descArea);
        descPanel.add(scrollPane, BorderLayout.CENTER);
        dialog.add(descPanel);

        JPanel btnPanel = new JPanel();
        JButton createBtn = new JButton("Create");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(createBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel);

        createBtn.addActionListener(e -> {
            String courseId = idField.getText().trim();
            String title = titleField.getText().trim();
            String description = descArea.getText().trim();

            String err = Validation.validateCourseId(courseId);
            if (!err.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, err, "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            err = Validation.validateCourse(title, description, currentUser.getUserId());
            if (!err.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, err, "Validation Error", JOptionPane.WARNING_MESSAGE);
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

        JDialog dialog = new JDialog(this, "Manage My Courses", true);
        dialog.setSize(900, 500); // Made slightly wider to fit the status
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        
        String[] columns = {"Course ID", "Title", "Status", "Description", "Students", "Lessons"};
        
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (Course course : instructorCourses) {
           
            String statusText;
            if (course.getApprovalStatus() == Course.ApprovalStatus.APPROVED) {
                statusText = "Admin Approved";
            } else if (course.getApprovalStatus() == Course.ApprovalStatus.REJECTED) {
                statusText = "Admin Rejected";
            } else {
                statusText = "Pending Admin Approval";
            }

            model.addRow(new Object[]{
                course.getCourseId(),
                course.getTitle(),
                statusText, 
                course.getDescription(),
                course.getStudents().size(),
                course.getLessons().size()
            });
        }

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) value;
                if ("Admin Approved".equals(status)) {
                    setForeground(new Color(0, 128, 0)); 
                } else if ("Admin Rejected".equals(status)) {
                    setForeground(Color.RED);
                } else {
                    setForeground(new Color(200, 150, 0)); 
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        dialog.add(scrollPane, BorderLayout.CENTER);

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

       
        
        editBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Please select a course first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String courseId = (String) model.getValueAt(selectedRow, 0);
            Course selectedCourse = instructorCourses.stream().filter(c -> c.getCourseId().equals(courseId)).findFirst().orElse(null);
            if (selectedCourse != null) showEditCourseDialog(selectedCourse, dialog);
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Please select a course first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String courseId = (String) model.getValueAt(selectedRow, 0);
            String courseTitle = (String) model.getValueAt(selectedRow, 1);
            int confirm = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to delete:\n" + courseTitle + " (" + courseId + ")?\nThis will unenroll all students!", "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean success = instructorService.deleteCourse(currentUser.getUserId(), courseId);
                    if (success) {
                        model.removeRow(selectedRow);
                        JOptionPane.showMessageDialog(dialog, "Course deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to delete course.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog, "Error deleting course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        manageLessonsBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Please select a course first.", "No Selection", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== Edit Course ====================
    private void showEditCourseDialog(Course course, JDialog parentDialog) {
        JDialog dialog = new JDialog(this, "Edit Course", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 1, 10, 10));

        JPanel idPanel = new JPanel();
        idPanel.add(new JLabel("Course ID:"));
        JTextField idField = new JTextField(course.getCourseId(), 20);
        idField.setEditable(false);
        idPanel.add(idField);
        dialog.add(idPanel);

        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("Title:"));
        JTextField titleField = new JTextField(course.getTitle(), 20);
        titlePanel.add(titleField);
        dialog.add(titlePanel);

        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(new JLabel("Description:"), BorderLayout.NORTH);
        JTextArea descArea = new JTextArea(course.getDescription(), 5, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descArea);
        descPanel.add(scrollPane, BorderLayout.CENTER);
        dialog.add(descPanel);

        JPanel btnPanel = new JPanel();
        JButton saveBtn = new JButton("Save Changes");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel);

        saveBtn.addActionListener(e -> {
            String newTitle = titleField.getText().trim();
            String newDescription = descArea.getText().trim();
            String err = Validation.validateCourse(newTitle, newDescription, currentUser.getUserId());
            if (!err.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, err, "Validation Error", JOptionPane.WARNING_MESSAGE);
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

            JDialog dialog = new JDialog(this, "Manage Lessons - " + courseTitle, true);
            dialog.setSize(800, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

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

            addBtn.addActionListener(e -> showAddLessonDialog(courseId, dialog));
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
        List<Question> currentQuiz = new ArrayList<>();
        JButton manageQuizBtn = new JButton("Attach Quiz");
        manageQuizBtn.addActionListener(e -> {
            List<Question> updated = showManageQuizDialog(dialog, currentQuiz);
            currentQuiz.clear();
            currentQuiz.addAll(updated);
            JOptionPane.showMessageDialog(dialog, "Quiz now has " + currentQuiz.size() + " questions.");
        });

        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(5, 1, 10, 10));

        JPanel idPanel = new JPanel();
        idPanel.add(new JLabel("Lesson ID:"));
        JTextField idField = new JTextField(20);
        idPanel.add(idField);
        dialog.add(idPanel);

        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("Title:"));
        JTextField titleField = new JTextField(20);
        titlePanel.add(titleField);
        dialog.add(titlePanel);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Content:"), BorderLayout.NORTH);
        JTextArea contentArea = new JTextArea(5, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        dialog.add(contentPanel);

        JPanel btnPanel = new JPanel();
        JButton addBtn = new JButton("Add");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(manageQuizBtn);
        btnPanel.add(addBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel);

        addBtn.addActionListener(e -> {
            String lessonId = idField.getText().trim();
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();

            String err = Validation.validateLessonId(lessonId);
            if (!err.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, err, "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            err = Validation.validateLesson(title, content);
            if (!err.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, err, "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Lesson newLesson = new Lesson(lessonId, title, content);
            newLesson.setQuestions(currentQuiz);

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

        JPanel idPanel = new JPanel();
        idPanel.add(new JLabel("Lesson ID:"));
        JTextField idField = new JTextField(lesson.getLessonId(), 20);
        idField.setEditable(false);
        idPanel.add(idField);
        dialog.add(idPanel);

        JPanel titlePanel = new JPanel();
        titlePanel.add(new JLabel("Title:"));
        JTextField titleField = new JTextField(lesson.getTitle(), 20);
        titlePanel.add(titleField);
        dialog.add(titlePanel);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Content:"), BorderLayout.NORTH);
        JTextArea contentArea = new JTextArea(lesson.getContent(), 5, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        dialog.add(contentPanel);

        JPanel btnPanel = new JPanel();
        JButton saveBtn = new JButton("Save Changes");
        JButton cancelBtn = new JButton("Cancel");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel);

        saveBtn.addActionListener(e -> {
            String newTitle = titleField.getText().trim();
            String newContent = contentArea.getText().trim();
            String err = Validation.validateLesson(newTitle, newContent);
            if (!err.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, err, "Validation Error", JOptionPane.WARNING_MESSAGE);
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

            JDialog dialog = new JDialog(this, "Select Course to View Students", true);
            dialog.setSize(700, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

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

            JPanel btnPanel = new JPanel();
            JButton viewBtn = new JButton("View Students");
            JButton closeBtn = new JButton("Close");
            btnPanel.add(viewBtn);
            btnPanel.add(closeBtn);
            dialog.add(btnPanel, BorderLayout.SOUTH);

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
            List<Student> students = instructorService.getEnrolledStudents(currentUser.getUserId(), courseId);
            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No students enrolled in this course yet.",
                        "Course Students",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog(this, "Students - " + courseTitle, true);
            dialog.setSize(800, 500);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());

            String[] columns = {"Student ID", "Username", "Email", "Enrolled Courses", "Completed Lessons"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            List<Course> allCourses = dbManager.loadCourses();
            Optional<Course> courseOpt = allCourses.stream()
                    .filter(c -> c.getCourseId().equals(courseId))
                    .findFirst();

            int totalLessonsInCourse = courseOpt.map(c -> c.getLessons().size()).orElse(0);

            for (Student student : students) {
                int completedLessonsCount = 0;
                Map<String, List<String>> studentProgress = student.getProgress();
                if (studentProgress != null && studentProgress.containsKey(courseId)) {
                    List<String> completedLessonsForThisCourse = studentProgress.get(courseId);
                    if (completedLessonsForThisCourse != null) {
                        completedLessonsCount = completedLessonsForThisCourse.size();
                    }
                }

                String completedLessonsText = completedLessonsCount + " / " + totalLessonsInCourse;
                if (totalLessonsInCourse > 0 && completedLessonsCount == totalLessonsInCourse) {
                    completedLessonsText += " (COMPLETED!)";
                }

                model.addRow(new Object[]{
                    student.getUserId(),
                    student.getUsername(),
                    student.getEmail(),
                    student.getEnrolledCourses().size(),
                    completedLessonsText
                });
            }

            JTable table = new JTable(model);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollPane = new JScrollPane(table);
            dialog.add(scrollPane, BorderLayout.CENTER);

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

    private void showMyProfile() {
        UserService userService = new UserService(dbManager);
        try {
            int[] stats = userService.getInstructorStatistics(currentUser.getUserId());
            String message = String.format(
                    "=== My Profile ===\n"
                    + "User ID: %s\n"
                    + "Username: %s\n"
                    + "Email: %s\n"
                    + "Role: %s\n"
                    + "=== My Statistics ===\n"
                    + "Created Courses: %d\n"
                    + "Total Students: %d\n",
                    currentUser.getUserId(),
                    currentUser.getUsername(),
                    currentUser.getEmail(),
                    currentUser.getRole(),
                    stats[0],
                    stats[1]
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
            JOptionPane.showMessageDialog(this,
                    "Error loading profile: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEditProfileDialog() {
        UserService userService = new UserService(dbManager);
        String newUsername = JOptionPane.showInputDialog(this,
                "Enter new username:",
                currentUser.getUsername());
        if (newUsername == null) {
            return;
        }

        String newEmail = JOptionPane.showInputDialog(this,
                "Enter new email:",
                currentUser.getEmail());
        if (newEmail == null) {
            return;
        }

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
                    currentUser.getUserId(),
                    newUsername.trim(),
                    newEmail.trim()
            );
            if (success) {
                currentUser.setUsername(newUsername.trim());
                currentUser.setEmail(newEmail.trim());
                setTitle("Instructor Dashboard - " + newUsername.trim());
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
}
