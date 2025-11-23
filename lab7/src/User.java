/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author monic
 */


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "role")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Student.class, name = "student"),
    @JsonSubTypes.Type(value = Instructor.class, name = "instructor"),
    @JsonSubTypes.Type(value = Admin.class, name = "Admin")
})
public abstract class User {
    protected String userId;
    protected String role; 
    protected String username;
    protected String email;
    protected String passwordHash;

    
   public User() {
    this.userId = null;
    this.role = null; 
    this.username = null;
    this.email = null;
    this.passwordHash = null;
}

    public User(String userId, String role, String username, String email, String passwordHash) {
        this.userId = userId;
        this.role = role; 
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() { 
        return role; 
    }

    public void setRole(String role) {
        if (role == null) {
            this.role = ""; 
        } else {
            this.role = role;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}