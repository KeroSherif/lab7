/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author monic
 */
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.*;

public class Main {

    static ObjectMapper mapper = new ObjectMapper();

    static List<User> users = new ArrayList<>();
    static List<Course> courses = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        loadData();

        createCourse(1, "Java Course");
        addLesson(1001, "Intro", "Welcome!");

        saveData();
    }


    static void loadData() throws Exception {
        File u = new File("users.json");
        File c = new File("courses.json");

        if (u.exists())
            users = mapper.readValue(u, new TypeReference<List<User>>() {});
        if (c.exists())
            courses = mapper.readValue(c, new TypeReference<List<Course>>() {});
    }

    static void saveData() throws Exception {
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File("users.json"), users);

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File("courses.json"), courses);
    }



    public static void createCourse(int instructorId, String title) throws Exception {
        Course course = new Course();
        course.courseId = generateId();
        course.title = title;
        course.instructorId = instructorId;
        course.lessons = new ArrayList<>();
        course.enrolledStudents = new ArrayList<>();

        courses.add(course);

        for (User u : users) {
            if (u.userId == instructorId) {
                u.createdCourses.add(course.courseId);
                break;
            }
        }

        saveData();
        System.out.println("Course created: " + title);
    }


    public static void addLesson(int courseId, String title, String content) throws Exception {
        for (Course c : courses) {
            if (c.courseId == courseId) {

                Lesson lesson = new Lesson();
                lesson.lessonId = generateId();
                lesson.title = title;
                lesson.content = content;

                c.lessons.add(lesson);
                saveData();
                System.out.println("Lesson added.");
                return;
            }
        }
    }


    public static int generateId() {
        return (int) (Math.random() * 100000);
    }
}
