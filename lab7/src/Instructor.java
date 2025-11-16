/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author monic
 */
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;

@JsonTypeName("instructor") // <-- مهم
public class Instructor extends User {

    private List<String> createdCourses; // قائمة بـ IDs للكورسات

    // Constructor فاضي لـ Jackson
    public Instructor() {
        super(); // لازم تستخدم constructor الـ parent فاضي لـ Jackson
        this.createdCourses = new ArrayList<>(); // <-- مهم، مش null
    }

    public Instructor(String userId, String username, String email, String passwordHash) {
        super(userId, "instructor", username, email, passwordHash);
        this.createdCourses = new ArrayList<>();
    }

    public List<String> getCreatedCourses() {
        return createdCourses;
    }

    public void setCreatedCourses(List<String> createdCourses) {
        this.createdCourses = createdCourses;
    }

    public void addCreatedCourse(String courseId) {
        if (!this.createdCourses.contains(courseId)) {
            this.createdCourses.add(courseId);
        }
    }

    public void removeCreatedCourse(String courseId) {
        this.createdCourses.remove(courseId);
    }
}