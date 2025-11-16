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
    private LoginService loginService;
    private JasonDatabaseManager dbManager;

    public LoginFrame() {
        setTitle("Login");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1));
        this.dbManager = JasonDatabaseManager.getInstance();
        this.loginService = new LoginService(dbManager);

        JPanel emailPanel = new JPanel();
        emailPanel.add(new JLabel("Email: "));
        emailField = new JTextField(20);
        emailPanel.add(emailField);
        add(emailPanel);
        
        JPanel passPanel = new JPanel();
        passPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField(20);
        passPanel.add(passwordField);
        add(passPanel);
        
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
        try{
            User loggedInUser = loginService.login(email, password);
            JOptionPane.showMessageDialog(this, "Logged In successfully\n" + loggedInUser.getUsername() + "\nWelcome to your second home.");
            dispose();
            if(loggedInUser.getRole().equal("student")){
                new StudentDashboardFrame(loggedInUser).setVisiable(true);
            }else if(loggedInUser.getRole().equal("instructor")){
                new InstructorDashboardFrame(loggedInUser).setVisiable(true);
            }
            catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Login Failed:\n" + ex.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
        }                
    }
}
