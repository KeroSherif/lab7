/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
/**
 *
 * @author DANAH
 */
public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginBtn, signupBtn;

    public LoginFrame() {
        setTitle("Login");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1));

        JPanel emailPanel = new JPanel();
        emailPanel.add(new JLabel("Email: "));
        emailField = new JTextField(20);
        emailPanel.add(emailField);
        add(emailPanel);
        
        JPanel btnPanel = new JPanel();
        loginBtn = new JButton("Login");
        signupBtn = new JButton("Go to Signup");
        btnPanel.add(loginBtn);
        btnPanel.add(signupBtn);
        add(btnPanel);
        
        loginBtn.addActionListener(e -> loginAction());
        signupBtn.addActionListener(e -> {
            dispose();
            new SignupFrame().setVisible(true);
        });
    }

    private void loginAction() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
         if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Invalid email format.");
            return;
        }
        
        String role = email.startsWith("i") ? "instructor" : "student";
        
        dispose();
        if (role.equals("student")) {
            new StudentDashboardFrame().setVisible(true);
        } else {
            new InstructorDashboardFrame().setVisible(true);
        }
    }
}