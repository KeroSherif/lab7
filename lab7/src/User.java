/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author monic
 */
import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String role; 
    private String username;
    private String email;
    private String passwordHash;
    private List<Integer> createdCourses = new ArrayList<>();

    public User() {} 
    
    public User(String userId, String role, String username, String email, String passwordHash) {
        this.userId = userId;
        this.role = role;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdCourses = new ArrayList<>();
    }

  
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public List<Integer> getCreatedCourses() { return createdCourses; }
    public void setCreatedCourses(List<Integer> createdCourses) { this.createdCourses = createdCourses; }
}
