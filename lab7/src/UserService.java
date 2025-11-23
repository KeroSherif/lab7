
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for managing users (Students and Instructors) Provides methods
 * for user CRUD operations
 *
 * @author Kirolos Sherif
 */
public class UserService {

    private JsonDatabaseManager dbManager;

    public UserService(JsonDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // ==================== Get All Users ====================
    public List<User> getAllUsers() throws IOException {
        return dbManager.loadUsers();
    }

    public List<Student> getAllStudents() throws IOException {
        return getAllUsers().stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .collect(Collectors.toList());
    }

    public List<Instructor> getAllInstructors() throws IOException {
        return getAllUsers().stream()
                .filter(u -> u instanceof Instructor)
                .map(u -> (Instructor) u)
                .collect(Collectors.toList());
    }

    // ==================== Get User by ID ====================
    public User getUserById(String userId) throws IOException {
        return dbManager.findUserById(userId).orElse(null);
    }

    public Student getStudentById(String studentId) throws IOException {
        return dbManager.getStudentById(studentId);
    }

    public Instructor getInstructorById(String instructorId) throws IOException {
        return dbManager.getInstructorById(instructorId);
    }

    // ==================== Get User by Email ====================
    public User getUserByEmail(String email) throws IOException {
        return dbManager.getUserByEmail(email);
    }

    // ==================== Update User ====================
    public boolean updateUser(String userId, String newUsername, String newEmail) throws IOException {
        String validationErrorUsername = Validation.validateUsername(newUsername);
        if (!validationErrorUsername.isEmpty()) {
            System.out.println("UserService: " + validationErrorUsername);
            return false;
        }

        String validationErrorEmail = Validation.validateEmail(newEmail);
        if (!validationErrorEmail.isEmpty()) {
            System.out.println("UserService: " + validationErrorEmail);
            return false;
        }

        List<User> allUsers = dbManager.loadUsers();
        User user = allUsers.stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst()
                .orElse(null);

        if (user == null) {
            System.out.println("UserService: User with ID " + userId + " not found.");
            return false;
        }

        // Check email uniqueness (excluding current user)
        boolean emailExists = allUsers.stream()
                .anyMatch(u -> !u.getUserId().equals(userId) && newEmail.equals(u.getEmail()));

        if (emailExists) {
            System.out.println("UserService: Email " + newEmail + " is already taken.");
            return false;
        }

        user.setUsername(newUsername);
        user.setEmail(newEmail);

        dbManager.saveUsers(allUsers);
        System.out.println("UserService: User " + userId + " updated successfully.");
        return true;
    }

    public boolean updateUserPassword(String userId, String oldPassword, String newPassword) throws IOException {
        String validationError = Validation.validatePassword(newPassword);
        if (!validationError.isEmpty()) {
            System.out.println("UserService: " + validationError);
            return false;
        }

        List<User> allUsers = dbManager.loadUsers();
        User user = allUsers.stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst()
                .orElse(null);

        if (user == null) {
            System.out.println("UserService: User with ID " + userId + " not found.");
            return false;
        }

        try {
            if (!PasswordEncryption.hashPassword(oldPassword).equals(user.getPasswordHash())) {
                System.out.println("UserService: Old password is incorrect.");
                return false;
            }

            user.setPasswordHash(PasswordEncryption.hashPassword(newPassword));
            dbManager.saveUsers(allUsers);
            System.out.println("UserService: Password for user " + userId + " updated successfully.");
            return true;

        } catch (Exception e) {
            System.err.println("UserService: Error hashing password: " + e.getMessage());
            return false;
        }
    }

    // ==================== Delete User ====================
    public boolean deleteUser(String userId) throws IOException {
        User user = getUserById(userId);
        if (user == null) {
            System.out.println("UserService: User with ID " + userId + " not found.");
            return false;
        }

        List<Course> allCourses = dbManager.loadCourses();

        if (user instanceof Student) {
            Student student = (Student) user;
            for (String courseId : new ArrayList<>(student.getEnrolledCourses())) {
                Optional<Course> courseOpt = allCourses.stream()
                        .filter(c -> c.getCourseId().equals(courseId))
                        .findFirst();
                if (courseOpt.isPresent()) {
                    courseOpt.get().removeStudent(userId);
                }
            }
        }

        if (user instanceof Instructor) {
            Instructor instructor = (Instructor) user;
            List<String> createdCourses = new ArrayList<>(instructor.getCreatedCourses());

            // Remove courses
            allCourses.removeIf(c -> createdCourses.contains(c.getCourseId()));

            // Remove these courses from students' progress & enrollment
            List<User> allUsers = dbManager.loadUsers();
            for (User u : allUsers) {
                if (u instanceof Student) {
                    Student s = (Student) u;
                    s.getEnrolledCourses().removeAll(createdCourses);
                    createdCourses.forEach(s.getProgress()::remove);
                }
            }
            dbManager.saveUsers(allUsers);
        }

        // Remove user and save
        List<User> allUsers = dbManager.loadUsers();
        allUsers.removeIf(u -> u.getUserId().equals(userId));
        dbManager.saveUsers(allUsers);
        dbManager.saveCourses(allCourses);

        System.out.println("UserService: User " + userId + " deleted successfully.");
        return true;
    }

    // ==================== User Statistics ====================
    public int getTotalUsersCount() throws IOException {
        return getAllUsers().size();
    }

    public int getTotalStudentsCount() throws IOException {
        return getAllStudents().size();
    }

    public int getTotalInstructorsCount() throws IOException {
        return getAllInstructors().size();
    }

    public int[] getStudentStatistics(String studentId) throws IOException {
        Student student = getStudentById(studentId);
        if (student == null) {
            return new int[]{0, 0};
        }

        int enrolled = student.getEnrolledCourses().size();
        int completed = student.getProgress().values().stream()
                .mapToInt(List::size)
                .sum();

        return new int[]{enrolled, completed};
    }

    public int[] getInstructorStatistics(String instructorId) throws IOException {
        Instructor instructor = getInstructorById(instructorId);
        if (instructor == null) {
            return new int[]{0, 0};
        }

        List<Course> allCourses = dbManager.loadCourses();
        List<String> created = instructor.getCreatedCourses();

        int courseCount = (int) created.stream()
                .filter(id -> allCourses.stream().anyMatch(c -> c.getCourseId().equals(id)))
                .count();

        int studentCount = allCourses.stream()
                .filter(c -> created.contains(c.getCourseId()))
                .mapToInt(c -> c.getStudents().size())
                .sum();

        return new int[]{courseCount, studentCount};
    }

    // ==================== Validation & Search ====================
    public boolean userExists(String userId) throws IOException {
        return dbManager.findUserById(userId).isPresent();
    }

    public boolean emailExists(String email) throws IOException {
        return dbManager.getUserByEmail(email) != null;
    }

    public boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public List<User> searchUsersByUsername(String username) throws IOException {
        String term = username.toLowerCase();
        return getAllUsers().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }

    public List<User> searchUsersByEmail(String email) throws IOException {
        String term = email.toLowerCase();
        return getAllUsers().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().toLowerCase().contains(term))
                .collect(Collectors.toList());
    }

    // ==================== Bulk Operations ====================
    public boolean deleteAllUsers() throws IOException {
        dbManager.saveUsers(new ArrayList<>());
        dbManager.saveCourses(new ArrayList<>());
        System.out.println("UserService: All users and courses deleted.");
        return true;
    }

    // ==================== Progress Calculation ====================
    public double getAverageProgressForCourse(String courseId) throws IOException {
        List<Course> allCourses = dbManager.loadCourses();
        Course course = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst()
                .orElse(null);

        if (course == null) {
            return 0.0;
        }

        int totalLessons = course.getLessons().size();
        List<String> studentIds = course.getStudents();
        int totalStudents = studentIds.size();

        if (totalLessons == 0 || totalStudents == 0) {
            return 0.0;
        }

        List<User> allUsers = dbManager.loadUsers();
        int totalCompleted = 0;

        for (String studentId : studentIds) {
            Student student = allUsers.stream()
                    .filter(u -> u.getUserId().equals(studentId) && u instanceof Student)
                    .map(u -> (Student) u)
                    .findFirst()
                    .orElse(null);

            if (student != null) {
                List<String> completed = student.getProgress().getOrDefault(courseId, new ArrayList<>());
                totalCompleted += completed.size();
            }
        }

        double totalPossible = totalLessons * totalStudents;
        return (totalPossible > 0) ? ((double) totalCompleted / totalPossible) * 100.0 : 0.0;
    }

    // ==================== Get Users by Role ====================
    public List<User> getUsersByRole(String role) throws IOException {
        if ("student".equalsIgnoreCase(role)) {
            return new ArrayList<>(getAllStudents()); // تحويل لـ List<User>
        } else if ("instructor".equalsIgnoreCase(role)) {
            return new ArrayList<>(getAllInstructors());
        }
        return new ArrayList<>();
    }
}
