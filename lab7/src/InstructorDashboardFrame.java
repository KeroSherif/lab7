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
public class InstructorDashboardFrame extends JFrame {

    public InstructorDashboardFrame() {
        setTitle("Instructor Dashboard");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 1));

        JLabel title = new JLabel("Welcome Instructor!", SwingConstants.CENTER);
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

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }
}