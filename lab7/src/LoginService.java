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
        User user = dbManager.getUserByEmail(email);
        if(user == null){
            throw new Exception("This Email is not found");
        }
        String enteredPasswordEncrypted = PasswordEncryption.hashPassword(plainPassword);
        if(!enteredPasswordEncrypted.equals(user.getPasswordHash())){ // صححت الاسم من getpasswordHash لـ getPasswordHash
            throw new Exception("Wrong password");
        }
        return user;
    }

    public User signup(String username, String email, String plainPassword, String role) throws Exception{
        if(dbManager.getUserByEmail(email) != null){
            throw new Exception("This email is unavailable");
        }
        String hashedPassword = PasswordEncryption.hashPassword(plainPassword);
        String userId = dbManager.getNextUserId();
        User newUser;
        if(role.equalsIgnoreCase("student")){
            newUser = new Student(userId, username, email, hashedPassword); // صححت الاسم من student لـ Student
        }else{
            newUser = new Instructor(userId, username, email, hashedPassword);
        }
        dbManager.saveUser(newUser);
        return newUser;
    }
}

