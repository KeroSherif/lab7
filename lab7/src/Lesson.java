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

public class Lesson {
    private String lessonId;
    private String title;
    private String content;
    private List<String> resources; // ممكن تكون فاضية (List of URLs or file paths)

    // Constructor فاضي لـ Jackson
    public Lesson() {
        this.resources = new ArrayList<>();
    }

    // Constructor مع باراميترز
    public Lesson(String lessonId, String title, String content) {
        this.lessonId = lessonId;
        this.title = title;
        this.content = content;
        this.resources = new ArrayList<>(); // تبدأ فاضية
    }

    // Getters و Setters
    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getResources() {
        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    // Business Methods
    public void addResource(String resource) {
        if (!this.resources.contains(resource)) {
            this.resources.add(resource);
        }
    }

    public void removeResource(String resource) {
        this.resources.remove(resource);
    }
    

}