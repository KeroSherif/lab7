/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7;

/**
 *
 * @author Kirolos sherif
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode; // من اللينك، ممكن تستخدمها لدمج JSON
import java.io.File;
import java.io.IOException;
import java.util.*;


public class JsonDatabaseManager {

    private static final String USERS_FILE = "users.json";
    private static final String COURSES_FILE = "courses.json";
    private ObjectMapper mapper = new ObjectMapper(); // مثيل واحد لـ ObjectMapper

    public JsonDatabaseManager() {
        // تأكد من وجود الملفات أو انشئها فارغة
        initializeFile(USERS_FILE, new ArrayList<User>());
        initializeFile(COURSES_FILE, new ArrayList<Course>());
    }

    
    private void initializeFile(String filename, Object defaultValue) {
        File file = new File(filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, defaultValue);
                System.out.println("Created new file: " + filename);
            } catch (IOException e) {
                System.err.println("Error creating file: " + filename + ", " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // --- Load Methods ---

    public List<User> loadUsers() throws IOException {
        File file = new File(USERS_FILE);
        // TypeReference لتحديد النوع بشكل دقيق (Generics)
        return mapper.readValue(file, new TypeReference<List<User>>() {});
    }

 
    public List<Course> loadCourses() throws IOException {
        File file = new File(COURSES_FILE);
        return mapper.readValue(file, new TypeReference<List<Course>>() {});
    }

    // --- Save Methods ---

    public void saveUsers(List<User> users) throws IOException {
        // writerWithDefaultPrettyPrinter() لتنسيق الملف بصيغة جميلة
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(USERS_FILE), users);
    }

    public void saveCourses(List<Course> courses) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(COURSES_FILE), courses);
    }

    // --- Helper Methods (اختيارية، بس مفيدة) ---
    public Optional<User> findUserById(String userId) throws IOException {
        List<User> users = loadUsers();
        return users.stream().filter(u -> u.getUserId().equals(userId)).findFirst();
    }

 
    public Optional<Course> findCourseById(String courseId) throws IOException {
        List<Course> courses = loadCourses();
        return courses.stream().filter(c -> c.getCourseId().equals(courseId)).findFirst();
    }

  
    public Student getStudentById(String studentId) throws IOException {
        Optional<User> userOpt = findUserById(studentId);
        if (userOpt.isPresent() && userOpt.get() instanceof Student) { // تحقق من النوع
            return (Student) userOpt.get();
        }
        return null;
    }

   
    public Instructor getInstructorById(String instructorId) throws IOException {
        Optional<User> userOpt = findUserById(instructorId);
        if (userOpt.isPresent() && userOpt.get() instanceof Instructor) { // تحقق من النوع
            return (Instructor) userOpt.get();
        }
        return null;
    }

    public Course getCourseById(String courseId) throws IOException {
        Optional<Course> courseOpt = findCourseById(courseId);
        return courseOpt.orElse(null);
    }

   
    public void mergeObjectNodes(ObjectNode obj1, ObjectNode obj2) {
        obj1.setAll(obj2); // دمج obj2 في obj1
    }

   

    public boolean isUserIdDuplicate(String userId) throws IOException {
        Optional<User> existingUser = findUserById(userId);
        return existingUser.isPresent();
    }
    
    public boolean isCourseIdDuplicate(String courseId) throws IOException {
        Optional<Course> existingCourse = findCourseById(courseId);
        return existingCourse.isPresent();
    }
}

