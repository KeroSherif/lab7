import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonDatabaseManager {

    private static final String USERS_FILE = "users.json";
    private static final String COURSES_FILE = "courses.json";
    private static final String ADMINS_FILE = "admin.json";
    private static final String CERTIFICATES_FILE = "certificates.json";
    private static final String QUIZ_RESULTS_FILE = "quiz_results.json"; // ✅ جديد

    private ObjectMapper mapper;
    private static JsonDatabaseManager instance = null;

    JsonDatabaseManager() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.registerSubtypes(
            new NamedType(Student.class, "student"),
            new NamedType(Instructor.class, "instructor"),
            new NamedType(Admin.class, "Admin")
        );

        initializeFile(USERS_FILE, new ArrayList<User>());
        initializeFile(COURSES_FILE, new ArrayList<Course>());
        initializeAdminFile();
        initializeFile(QUIZ_RESULTS_FILE, new ArrayList<QuizResult>()); // ✅ تهيئة ملف الكويزات
    }

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

    private void initializeAdminFile() {
        File adminFile = new File(ADMINS_FILE);
        if (!adminFile.exists()) {
            try {
                mapper.writerWithDefaultPrettyPrinter()
                      .writeValue(adminFile, new ArrayList<Admin>());
                System.out.println("Created empty admin.json");
            } catch (IOException e) {
                System.err.println("Failed to create admin.json: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // --------------------------------------------------------------------
    //                          LOAD METHODS
    // --------------------------------------------------------------------
    public List<User> loadUsers() throws IOException {
        File file = new File(USERS_FILE);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try {
            List<User> users = mapper.readValue(file, new TypeReference<List<User>>() {});
            for (User user : users) {
                if (user.getRole() == null || user.getRole().isEmpty()) {
                    System.err.println("WARNING: Missing role for userId = " + user.getUserId());
                    if (user instanceof Student) user.setRole("student");
                    else if (user instanceof Instructor) user.setRole("instructor");
                    else if (user instanceof Admin) user.setRole("Admin");
                }
            }
            return users;
        } catch (Exception e) {
            System.err.println("ERROR reading users.json: " + e.getMessage());
            throw e;
        }
    }

    public List<Course> loadCourses() throws IOException {
        File file = new File(COURSES_FILE);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        List<Course> courses = mapper.readValue(file, new TypeReference<List<Course>>() {});
        for (Course c : courses) {
            if (c.getApprovalStatus() == null) {
                c.setApprovalStatus(Course.ApprovalStatus.PENDING);
            }
        }
        return courses;
    }

    public List<Admin> loadAdmins() throws IOException {
        File file = new File(ADMINS_FILE);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        return mapper.readValue(file, new TypeReference<List<Admin>>() {});
    }

    public List<Map<String, String>> loadCertificates() throws IOException {
        File file = new File(CERTIFICATES_FILE);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        return mapper.readValue(file, new TypeReference<List<Map<String, String>>>() {});
    }

    public List<QuizResult> loadQuizResults() throws IOException {
        File file = new File(QUIZ_RESULTS_FILE);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        return mapper.readValue(file, new TypeReference<List<QuizResult>>() {});
    }

    // --------------------------------------------------------------------
    //                          SAVE METHODS
    // --------------------------------------------------------------------
    public void saveUsers(List<User> users) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(USERS_FILE), users);
    }

    public void saveCourses(List<Course> courses) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(COURSES_FILE), courses);
    }

    public void saveAdmins(List<Admin> admins) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(ADMINS_FILE), admins);
    }

    public void saveCertificate(Map<String, String> certificate) throws IOException {
        List<Map<String, String>> certificates = loadCertificates();
        certificates.add(certificate);
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(CERTIFICATES_FILE), certificates);
    }

    public void saveQuizResult(QuizResult result) throws IOException {
        List<QuizResult> results = loadQuizResults();
        results.add(result);
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(QUIZ_RESULTS_FILE), results);
    }

    // --------------------------------------------------------------------
    //                    SAVE SINGLE OBJECT (USER / COURSE) — FIXED TO AVOID DUPLICATES
    // --------------------------------------------------------------------
    public void saveUser(User user) throws IOException {
        List<User> users = loadUsers();
        boolean found = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(user.getUserId())) {
                users.set(i, user);
                found = true;
                break;
            }
        }
        if (!found) {
            users.add(user);
        }
        saveUsers(users);
    }

    public void saveCourse(Course course) throws IOException {
        List<Course> courses = loadCourses();
        boolean found = false;
        for (int i = 0; i < courses.size(); i++) {
            if (courses.get(i).getCourseId().equals(course.getCourseId())) {
                courses.set(i, course);
                found = true;
                break;
            }
        }
        if (!found) {
            courses.add(course);
        }
        saveCourses(courses);
    }

    // --------------------------------------------------------------------
    //                          FIND METHODS
    // --------------------------------------------------------------------
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

    public User getUserByEmail(String email) throws IOException {
        List<User> users = loadUsers();
        return users.stream()
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    // --------------------------------------------------------------------
    //                  NEW: QUIZ RESULTS BY LESSON
    // --------------------------------------------------------------------
    public List<QuizResult> getQuizResultsForLesson(String lessonId) throws IOException {
        List<QuizResult> allResults = loadQuizResults();
        return allResults.stream()
                .filter(qr -> lessonId.equals(qr.getLessonId()))
                .collect(Collectors.toList());
    }

    // --------------------------------------------------------------------
    //                       AUTO-INCREMENT USER ID
    // --------------------------------------------------------------------
    public String getNextUserId() throws IOException {
        List<User> users = loadUsers();
        if (users.isEmpty()) {
            return "U1";
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
                    continue;
                }
            }
        }
        return "U" + (maxId + 1);
    }

    // --------------------------------------------------------------------
    //                  DUPLICATE CHECK HELPERS
    // --------------------------------------------------------------------
    public boolean isUserIdDuplicate(String userId) throws IOException {
        return findUserById(userId).isPresent();
    }

    public boolean isCourseIdDuplicate(String courseId) throws IOException {
        return findCourseById(courseId).isPresent();
    }
}