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

public class Instructor extends User {

    private List<Integer> createdCourses = new ArrayList<>();

    public Instructor() {
        super(); 
    }

    public Instructor(String userId, String username, String email, String passwordHash) {
        super(userId, "instructor", username, email, passwordHash);
        this.createdCourses = new ArrayList<>();
    }

    
    public List<Integer> getCreatedCourses() {
        return createdCourses;
    }

    public void setCreatedCourses(List<Integer> createdCourses) {
        this.createdCourses = createdCourses;
    }


}
