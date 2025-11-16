/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.util.*;

/**
 *
 * @author Kirolos sherif
 */


public class Student extends User {

    private List<String> enrolledCourses;
    private Map<String, List<String>> progress;
    // progress: key = courseId, value = list of completed lessonIds

    public Student() {
        super(); // لازم تستخدم constructor الـ parent فاضي لـ Jackson
        this.enrolledCourses = new ArrayList<>();
        this.progress = new HashMap<>();
    }

    public Student(String userId, String username, String email, String passwordHash) {
        super(userId, "student", username, email, passwordHash);
        this.enrolledCourses = new ArrayList<>();
        this.progress = new HashMap<>();
    }

    public List<String> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(List<String> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

    public Map<String, List<String>> getProgress() {
        return progress;
    }

    public void setProgress(Map<String, List<String>> progress) {
        this.progress = progress;
    }

    public void enrollCourse(String courseId) {
        if (!enrolledCourses.contains(courseId)) {
            enrolledCourses.add(courseId);
            progress.put(courseId, new ArrayList<>());
        }
    }

    public void markLessonCompleted(String courseId, String lessonId) {
        if (!progress.containsKey(courseId))
            progress.put(courseId, new ArrayList<>());

        List<String> completed = progress.get(courseId);
        if (!completed.contains(lessonId)) {
            completed.add(lessonId);
        }
    }
}

