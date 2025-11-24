
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

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

        loginBtn.addActionListener(e -> loginAction());

        signupBtn.addActionListener(e -> {
            dispose();
            new SignupFrame().setVisible(true);
        });
    }

    private void loginAction() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if ("admin".equals(email) && "****".equals(password)) {
            try {
                showAdminSetupDialog();
                return;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error opening admin setup: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String validationError = Validation.validateLogin(email, password);
        if (!validationError.isEmpty()) {
            JOptionPane.showMessageDialog(this, validationError, "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User loggedInUser = loginService.login(email, password);

            JOptionPane.showMessageDialog(this,
                    "Logged in successfully\n" + loggedInUser.getUsername() + "\nWelcome!");

            dispose();

            String userRole = loggedInUser.getRole();
            System.out.println("DEBUG: Role from JSON = '" + userRole + "'");
            System.out.println("DEBUG: User class = " + loggedInUser.getClass().getName());

            if (userRole == null || userRole.isEmpty()) {
                if (loggedInUser instanceof Student) {
                    userRole = "student";
                } else if (loggedInUser instanceof Instructor) {
                    userRole = "instructor";
                } else if (loggedInUser instanceof Admin) {
                    userRole = "admin";
                }
            }

            if ("student".equalsIgnoreCase(userRole)) {
                new StudentDashboardFrame(loggedInUser).setVisible(true);
            } else if ("instructor".equalsIgnoreCase(userRole)) {
                new InstructorDashboardFrame(loggedInUser).setVisible(true);
            } else if ("admin".equalsIgnoreCase(userRole)) {
                new AdminDashboardFrame(loggedInUser).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Unknown user role: '" + userRole + "'\nContact system admin.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                new LoginFrame().setVisible(true);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Login Failed:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
private void showAdminSetupDialog() {
    JDialog dialog = new JDialog(this, "Create Admin Account", true);
    dialog.setSize(400, 300); 
    dialog.setLocationRelativeTo(this);

    dialog.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL; 

    JLabel userLabel = new JLabel("Username:");
    JTextField userField = new JTextField(20);
    gbc.gridx = 0; gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.EAST;
    dialog.add(userLabel, gbc);
    gbc.gridx = 1; gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    dialog.add(userField, gbc);
    
    JLabel emailLabel = new JLabel("Email:");
    JTextField emailField = new JTextField(20);
    gbc.gridx = 0; gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.EAST;
    dialog.add(emailLabel, gbc);
    gbc.gridx = 1; gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.WEST;
    dialog.add(emailField, gbc);

    JLabel passLabel = new JLabel("Password:");
    JPasswordField passField = new JPasswordField(20);
    gbc.gridx = 0; gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.EAST;
    dialog.add(passLabel, gbc);
    gbc.gridx = 1; gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.WEST;
    dialog.add(passField, gbc);

    JLabel confirmLabel = new JLabel("Confirm Password:");
    JPasswordField confirmField = new JPasswordField(20);
    gbc.gridx = 0; gbc.gridy = 3;
    gbc.anchor = GridBagConstraints.EAST;
    dialog.add(confirmLabel, gbc);
    gbc.gridx = 1; gbc.gridy = 3;
    gbc.anchor = GridBagConstraints.WEST;
    dialog.add(confirmField, gbc);

    JButton createBtn = new JButton("Create Admin");
    gbc.gridx = 0; gbc.gridy = 4;
    gbc.gridwidth = 2; 
    gbc.anchor = GridBagConstraints.CENTER;
    dialog.add(createBtn, gbc);

    JButton cancelBtn = new JButton("Cancel");
    gbc.gridx = 0; gbc.gridy = 5;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.CENTER;
    dialog.add(cancelBtn, gbc);

    createBtn.addActionListener(e -> {
        String user = userField.getText().trim();
        String email = emailField.getText().trim();
        String pass = new String(passField.getPassword());
        String confirm = new String(confirmField.getPassword());

        // 1. BASIC FORMAT VALIDATION
        String error = Validation.validateUsername(user);
        if (error.isEmpty()) error = Validation.validateEmail(email);
        if (error.isEmpty()) error = Validation.validatePassword(pass);
        if (error.isEmpty()) error = Validation.validatePasswordMatch(pass, confirm);

        if (!error.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, error, "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 2. CHECK FOR DUPLICATE EMAIL (New Addition)
            // We load the admins just to check the email, even if we overwrite later.
            List<Admin> existingAdmins = dbManager.loadAdmins();
            if (existingAdmins != null) {
                for (Admin a : existingAdmins) {
                    if (a.getEmail().equalsIgnoreCase(email)) {
                        JOptionPane.showMessageDialog(dialog, 
                            "This email is already used by another Admin.", 
                            "Validation Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            // 3. SAVE NEW ADMIN
            String hash = PasswordEncryption.hashPassword(pass);
            Admin admin = new Admin("A1", user, email, hash);
            
            // WARNING: This still overwrites all other admins. 
            // If you want to keep them, change List.of(admin) to existingAdmins.add(admin)
            dbManager.saveAdmins(List.of(admin)); 
            
            JOptionPane.showMessageDialog(dialog, "Admin account created successfully!\nNow log in with your credentials.");
            dialog.dispose();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    cancelBtn.addActionListener(e -> dialog.dispose());

    dialog.setVisible(true);
   }
}