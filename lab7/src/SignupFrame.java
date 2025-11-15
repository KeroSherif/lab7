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
