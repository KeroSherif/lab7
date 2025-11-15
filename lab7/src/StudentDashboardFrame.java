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

    public StudentDashboardFrame() {
        setTitle("Student Dashboard");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1));

        JLabel title = new JLabel("Welcome Student!", SwingConstants.CENTER);
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
}