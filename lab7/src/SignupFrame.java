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
public class SignupFrame extends JFrame {

    private JTextField usernameField, emailField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> roleBox;
    private JButton signupBtn, backBtn;

    public SignupFrame() {
        setTitle("Signup");
        setSize(450, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 1));

        JPanel p1 = new JPanel();
        p1.add(new JLabel("Username: "));
        usernameField = new JTextField(20);
        p1.add(usernameField);
        add(p1);
        
        JPanel p2 = new JPanel();
        p2.add(new JLabel("Email: "));
        emailField = new JTextField(20);
        p2.add(emailField);
        add(p2);
        
        JPanel p3 = new JPanel();
        p3.add(new JLabel("Password: "));
        passwordField = new JPasswordField(20);
        p3.add(passwordField);
        add(p3);   

        JPanel p4 = new JPanel();
        p4.add(new JLabel("Confirm Password: "));
        confirmPasswordField = new JPasswordField(20);
        p4.add(confirmPasswordField);
        add(p4);
        
        JPanel p5 = new JPanel();
        p5.add(new JLabel("Role: "));
        roleBox = new JComboBox<>(new String[]{"Student", "Instructor"});
        p5.add(roleBox);
        add(p5);

        JPanel p6 = new JPanel();
        signupBtn = new JButton("Create Account");
        backBtn = new JButton("Back to Login");
        p6.add(signupBtn);
        p6.add(backBtn);
        add(p6);

        signupBtn.addActionListener(e -> signupAction());
        backBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
    }
    
 private void signupAction() {
        String user = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());
        String role = roleBox.getSelectedItem().toString().toLowerCase();

       if (user.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Invalid email format.");
            return;
        }
        if (pass.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.");
            return;
        }
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }
        
 JOptionPane.showMessageDialog(this, "Account created successfully!");
        dispose();
        new LoginFrame().setVisible(true);
    }
}