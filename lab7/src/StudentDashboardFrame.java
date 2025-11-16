/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import java.awt.*;
/**
 *
 * @author DANAH
 */

public class StudentDashboardFrame extends JFrame {

    private User currentUser; // متغير لتخزين اليوزر

    public StudentDashboardFrame(User user) { // Constructor جديد
        this.currentUser = user; // خزن اليوزر
        setTitle("Student Dashboard - " + user.getUsername()); // مثلاً
        setSize(500, 300);
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

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }

    // Constructor فاضي ل万一 لازم Swing بس ماتستخدمش ده
    public StudentDashboardFrame() {
        this(new Student()); // استخدم constructor الجديد بـ User فاضي
    }
}