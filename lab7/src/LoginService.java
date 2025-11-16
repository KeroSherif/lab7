/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Mohamed
 */
public class LoginService {
    private JsonDatabaseManager dbManager;

    public LoginService(JsonDatabaseManager dbManager){
        this.dbManager = dbManager;
    }

    public User login(String email, String plainPassword) throws Exception{
        
        String validationError = Validation.validateLogin(email, plainPassword);
        if (!validationError.isEmpty()) {
            throw new Exception(validationError);
        }

        User user = dbManager.getUserByEmail(email);
        if(user == null){
            throw new Exception("This Email is not found");
        }
        String enteredPasswordEncrypted = PasswordEncryption.hashPassword(plainPassword);
        if(!enteredPasswordEncrypted.equals(user.getPasswordHash())){
            throw new Exception("Wrong password");
        }
        return user;
    }
    public User signup(String username, String email, String plainPassword, String role) throws Exception{
        String vUsername = Validation.validateUsername(username);
        if (!vUsername.isEmpty()) throw new Exception(vUsername);

        String vEmail = Validation.validateEmail(email);
        if (!vEmail.isEmpty()) throw new Exception(vEmail);

        
        String vPassword = Validation.validatePassword(plainPassword);
        if (!vPassword.isEmpty()) throw new Exception(vPassword);

        String vRole = Validation.validateRole(role);
        if (!vRole.isEmpty()) throw new Exception(vRole);
        
        if(dbManager.getUserByEmail(email) != null){
            throw new Exception("This email is unavailable");
        }
        String hashedPassword = PasswordEncryption.hashPassword(plainPassword);
        String userId = dbManager.getNextUserId();
        User newUser;
        if(role.equalsIgnoreCase("student")){
            newUser = new Student(userId, username, email, hashedPassword);
        }else{
            newUser = new Instructor(userId, username, email, hashedPassword);
        }
        dbManager.saveUser(newUser);
        return newUser;
    }
}

