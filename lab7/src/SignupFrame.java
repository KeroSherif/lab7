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
