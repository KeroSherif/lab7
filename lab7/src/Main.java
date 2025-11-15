/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author monic
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class Main {

    static ObjectMapper mapper = new ObjectMapper();
    static List<User> users = new ArrayList<>();
    static List<Course> courses = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        loadData();

        
        createCourse("1", "Java Basics");

        addLesson(0, "Lesson 1", "Content of Lesson 1");

        // عرض الطلاب المسجلين (مثال)
        viewEnrolledStudents(0);

        // حفظ البيانات بعد أي تعديل
        saveData();
    }

    // Load JSON files
    static void loadData() throws Exception {
        File u = new File("users.json");
        File c = new File("courses.json");

        if (u.exists())
            users = mapper.readValue(u, new TypeReference<List<User>>() {});
        if (c.exists())
            courses = mapper.readValue(c, new TypeReference<List<Course>>() {});
    }

    // Save JSON files
    static void saveData() throws Exception {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("users.json"), users);
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File("courses.json"), courses);
    }

    // Create a new course by instructor
    static void createCourse(String instructorId, String courseTitle) {
        User instructor = null;
        for (User u : users) {
            if (u.getUserId().equals(instructorId) && u.getRole().equals("instructor")) {
                instructor = u;
                break;
            }
        }
        if (instructor == null) {
            System.out.println("Instructor not found!");
            return;
        }

        Course course = new Course(courses.size(), courseTitle, instructor.getUserId());
        courses.add(course);

        // Add course to instructor's createdCourses
        instructor.getCreatedCourses().add(course.getCourseId());
        System.out.println("Course created: " + courseTitle);
    }

    // Add a lesson to a course
    static void addLesson(int courseId, String lessonTitle, String content) {
        if (courseId < 0 || courseId >= courses.size()) {
            System.out.println("Course not found!");
            return;
        }
        Lesson lesson = new Lesson(courses.get(courseId).getLessons().size(), lessonTitle, content);
        courses.get(courseId).getLessons().add(lesson);
        System.out.println("Lesson added: " + lessonTitle);
    }

    // View enrolled students
    static void viewEnrolledStudents(int courseId) {
        if (courseId < 0 || courseId >= courses.size()) {
            System.out.println("Course not found!");
            return;
        }
        Course course = courses.get(courseId);
        System.out.println("Enrolled Students for course '" + course.getTitle() + "':");
        for (User u : users) {
            if (u.getRole().equals("student") && u.getCreatedCourses().contains(courseId)) {
                System.out.println("- " + u.getUsername());
            }
        }
    }
}

