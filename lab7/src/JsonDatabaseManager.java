import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonDatabaseManager {

    private static final String USERS_FILE = "users.json";
    private static final String COURSES_FILE = "courses.json";
    private ObjectMapper mapper;
    private static JsonDatabaseManager instance = null;

    private JsonDatabaseManager() {
        // إنشاء ObjectMapper بشكل بسيط
        this.mapper = new ObjectMapper();
        
        // تفعيل الـ pretty printing
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // تأكد من وجود الملفات
        initializeFile(USERS_FILE, new ArrayList<User>());
        initializeFile(COURSES_FILE, new ArrayList<Course>());
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

    // Load Methods
   public List<User> loadUsers() throws IOException {
    File file = new File(USERS_FILE);
    
    // لو الملف مش موجود أو فاضي
    if (!file.exists() || file.length() == 0) {
        return new ArrayList<>();
    }
    
    try {
        List<User> users = mapper.readValue(file, new TypeReference<List<User>>() {});
        
        // تحقق إن كل user عنده role
        for (User user : users) {
            if (user.getRole() == null || user.getRole().isEmpty()) {
                System.err.println("WARNING: User " + user.getUserId() + " has no role. Fixing...");
                // حاول تحدد الـ role من نوع الكلاس
                if (user instanceof Student) {
                    user.setRole("student");
                } else if (user instanceof Instructor) {
                    user.setRole("instructor");
                }
            }
        }
        
        return users;
        
    } catch (Exception e) {
        System.err.println("ERROR reading users.json: " + e.getMessage());
        System.err.println("Please delete users.json and restart the application.");
        throw e; // ارمي الـ error بدل ما تمسح الملف
    }
}

    public List<Course> loadCourses() throws IOException {
        File file = new File(COURSES_FILE);
        if (file.length() == 0) {
            return new ArrayList<>();
        }
        return mapper.readValue(file, new TypeReference<List<Course>>() {});
    }

    // Save Methods
    public void saveUsers(List<User> users) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(USERS_FILE), users);
    }

    public void saveCourses(List<Course> courses) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(COURSES_FILE), courses);
    }

    // Save Single Object Methods
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

    // Helper Methods
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
        Optional<User> userOpt = users.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
        return userOpt.orElse(null);
    }

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

    public boolean isUserIdDuplicate(String userId) throws IOException {
        Optional<User> existingUser = findUserById(userId);
        return existingUser.isPresent();
    }

    public boolean isCourseIdDuplicate(String courseId) throws IOException {
        Optional<Course> existingCourse = findCourseById(courseId);
        return existingCourse.isPresent();
    }
    
    public void saveQuizResult(QuizResult result) throws IOException {
    List<QuizResult> results = loadQuizResults();
    results.add(result);
    saveQuizResults(results);
}

public List<QuizResult> loadQuizResults() throws IOException {
    return loadList("quiz_results.json", QuizResult[].class);
}

public void saveQuizResults(List<QuizResult> list) throws IOException {
    saveList("quiz_results.json", list);
}

public List<QuizResult> getQuizResultsForLesson(String lessonId) throws IOException {
    return loadQuizResults()
            .stream()
            .filter(r -> r.getLessonId().equals(lessonId))
            .collect(Collectors.toList());
}

public List<QuizResult> getQuizResultsForCourse(String courseId) throws IOException {
    return loadQuizResults()
            .stream()
            .filter(r -> r.getCourseId().equals(courseId))
            .collect(Collectors.toList());
}

}