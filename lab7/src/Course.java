/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 * @author monic
 * @author Kirolos sherif
 */

import java.util.ArrayList;
import java.util.List;

public class Course {
    private String courseId;
    private String title;
    private String description;
    private String instructorId;
    private List<Lesson> lessons;
    private List<String> students; // قائمة بـ userIds للطلاب اللي اشتركوا

    // Constructor فاضي لـ Jackson
    public Course() {
        this.lessons = new ArrayList<>();
        this.students = new ArrayList<>();
    }

    // Constructor مع باراميترز
    public Course(String courseId, String title, String description, String instructorId) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.instructorId = instructorId;
        this.lessons = new ArrayList<>();
        this.students = new ArrayList<>();
    }

    // Getters و Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    public List<String> getStudents() {
        return students;
    }

    public void setStudents(List<String> students) {
        this.students = students;
    }

    // Business Methods
    public void addStudent(String studentId) {
        if (!this.students.contains(studentId)) {
            this.students.add(studentId);
        }
    }

    public void removeStudent(String studentId) {
        this.students.remove(studentId);
    }

    public void addLesson(Lesson lesson) {
        this.lessons.add(lesson);
    }

    public boolean removeLesson(String lessonId) {
        return this.lessons.removeIf(lesson -> lesson.getLessonId().equals(lessonId));
    }

