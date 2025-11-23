
import com.fasterxml.jackson.annotation.JsonTypeName;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Kirolos sherif
 */
@JsonTypeName("Admin")
public class Admin extends User {

    public Admin() {
        super();
        this.role = "Admin";
    }

    public Admin(String userId, String username, String email, String passwordHash) {
        super(userId,"Admin",username, email, passwordHash);
    }
}
    