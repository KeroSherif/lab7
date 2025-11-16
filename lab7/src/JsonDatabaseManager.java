/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Kirolos sherif
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonDatabaseManager {

    private static final String USERS_FILE = "users.json";
    private static final String COURSES_FILE = "courses.json";
    private ObjectMapper mapper = new ObjectMapper(); // مثيل واحد لـ ObjectMapper
    private static JsonDatabaseManager instance = null; // متغير static لـ Singleton

    // Constructor خاص (private) لمنع إنشاء كائنات مباشرة
    private JsonDatabaseManager() {
        // تأكد من وجود الملفات أو انشئها فارغة
        initializeFile(USERS_FILE, new ArrayList<User>());
        initializeFile(COURSES_FILE, new ArrayList<Course>());
    }

    // الميثود اللي بترجع الكائن الوحيد (Singleton)
    public static JsonDatabaseManager getInstance() {
        if (instance == null) {
            instance = new JsonDatabaseManager();
        }
        return instance;
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
        return mapper.readValue(file, new TypeReference<List<User>>() {});
    }

    public List<Course> loadCourses() throws IOException {
        File file = new File(COURSES_FILE);
        return mapper.readValue(file, new TypeReference<List<Course>>() {});
    }

    // --- Save Methods ---
    public void saveUsers(List<User> users) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(USERS_FILE), users);
    }

    public void saveCourses(List<Course> courses) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(COURSES_FILE), courses);
    }

    // --- Save Single Object Methods ---
    public void saveUser(User user) throws IOException {
        List<User> users = loadUsers();
        users.add(user);
        saveUsers(users);
    }

    public void saveCourse(Course course) throws IOException {
        List<Course> courses = loadCourses();
        courses.add(course);
        saveCourses(courses);
    }

    // --- Helper Methods ---
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
        if (userOpt.isPresent() && userOpt.get() instanceof Student) {
            return (Student) userOpt.get();
        }
        return null;
    }

    public Instructor getInstructorById(String instructorId) throws IOException {
        Optional<User> userOpt = findUserById(instructorId);
        if (userOpt.isPresent() && userOpt.get() instanceof Instructor) {
            return (Instructor) userOpt.get();
        }
        return null;
    }

    public Course getCourseById(String courseId) throws IOException {
        Optional<Course> courseOpt = findCourseById(courseId);
        return courseOpt.orElse(null);
    }

    // --- Methods Required by LoginService ---
    public User getUserByEmail(String email) throws IOException {
        List<User> users = loadUsers();
        Optional<User> userOpt = users.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
        return userOpt.orElse(null);
    }

    public String getNextUserId() throws IOException {
        List<User> users = loadUsers();
        if (users.isEmpty()) {
            return "U1"; // أول ID
        }
        int maxId = 0;
        for (User u : users) {
            String id = u.getUserId();
            if (id != null && id.startsWith("U")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxId) {
                        maxId = num;
                    }
                } catch (NumberFormatException e) {
                    continue; // لو الـ ID مش نمط "U" + رقم، اتجاهل
                }
            }
        }
        return "U" + (maxId + 1); // ID الجديد
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