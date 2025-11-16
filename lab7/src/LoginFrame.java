import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author DANAH
 */
public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginBtn, signupBtn;
    private LoginService loginService;
    private JsonDatabaseManager dbManager;

    public LoginFrame() {
        setTitle("Login");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1));
        this.dbManager = JsonDatabaseManager.getInstance();
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

        loginBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginAction();
            }
        });
        signupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new SignupFrame().setVisible(true);
            }
        });
    }

    private void loginAction() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

         String validationError = Validation.validateLogin(email, password);
        if (!validationError.isEmpty()) {
            JOptionPane.showMessageDialog(this, validationError, "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            User loggedInUser = loginService.login(email, password);
            JOptionPane.showMessageDialog(this, "Logged In successfully\n" + loggedInUser.getUsername() + "\nWelcome to your second home.");

            
            dispose(); 

            String userRole = loggedInUser.getRole();

            System.out.println("DEBUG: User role = '" + userRole + "'");
            System.out.println("DEBUG: User class = " + loggedInUser.getClass().getName());

            if (userRole == null || userRole.isEmpty()) {
               
                if (loggedInUser instanceof Student) {
                    userRole = "student";
                } else if (loggedInUser instanceof Instructor) {
                    userRole = "instructor";
                }
            }

            if ("student".equalsIgnoreCase(userRole)) { // استخدم equalsIgnoreCase
                new StudentDashboardFrame(loggedInUser).setVisible(true);
            } else if ("instructor".equalsIgnoreCase(userRole)) {
                new InstructorDashboardFrame(loggedInUser).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Unknown user role: '" + userRole + "'. Contact admin.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                new LoginFrame().setVisible(true);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Login Failed:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}