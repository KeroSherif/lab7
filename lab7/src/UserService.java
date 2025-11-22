/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
/**
 *
 * @author Kirolos sherif
 */


/**
 * Service class for managing users (Students and Instructors)
 * Provides methods for user CRUD operations
 */
public class UserService {

    private JsonDatabaseManager dbManager;

    public UserService(JsonDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    // ==================== Get All Users ====================
    
    /**
     * Gets all users in the system
     */
    public List<User> getAllUsers() throws IOException {
        return dbManager.loadUsers();
    }

    /**
     * Gets all students in the system
     */
    public List<Student> getAllStudents() throws IOException {
        List<User> allUsers = dbManager.loadUsers();
        return allUsers.stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .collect(Collectors.toList());
    }

    /**
     * Gets all instructors in the system
     */
    public List<Instructor> getAllInstructors() throws IOException {
        List<User> allUsers = dbManager.loadUsers();
        return allUsers.stream()
                .filter(u -> u instanceof Instructor)
                .map(u -> (Instructor) u)
                .collect(Collectors.toList());
    }

    // ==================== Get User by ID ====================

    /**
     * Gets a user by their ID
     */
    public User getUserById(String userId) throws IOException {
        Optional<User> userOpt = dbManager.findUserById(userId);
        return userOpt.orElse(null);
    }

    /**
     * Gets a student by their ID
     */
    public Student getStudentById(String studentId) throws IOException {
        return dbManager.getStudentById(studentId);
    }

    /**
     * Gets an instructor by their ID
     */
    public Instructor getInstructorById(String instructorId) throws IOException {
        return dbManager.getInstructorById(instructorId);
    }

    // ==================== Get User by Email ====================

    /**
     * Gets a user by their email
     */
    public User getUserByEmail(String email) throws IOException {
        return dbManager.getUserByEmail(email);
    }

    // ==================== Update User ====================

    /**
     * Updates a user's information (username, email)
     * Password cannot be changed through this method
     */
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

        Optional<User> userOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst();

        if (userOpt.isEmpty()) {
            System.out.println("UserService: User with ID " + userId + " not found.");
            return false;
        }

        User user = userOpt.get();

        // Check if new email is already taken by another user
        if (!user.getEmail().equals(newEmail)) {
            boolean emailExists = allUsers.stream()
                    .anyMatch(u -> !u.getUserId().equals(userId) && u.getEmail().equals(newEmail));
            
            if (emailExists) {
                System.out.println("UserService: Email " + newEmail + " is already taken.");
                return false;
            }
        }

        user.setUsername(newUsername);
        user.setEmail(newEmail);

        dbManager.saveUsers(allUsers);

        System.out.println("UserService: User " + userId + " updated successfully.");
        return true;
    }

    /**
     * Updates a user's password
     */
    public boolean updateUserPassword(String userId, String oldPassword, String newPassword) throws IOException {
       String validationError = Validation.validatePassword(newPassword);
        if (!validationError.isEmpty()) {
            System.out.println("UserService: " + validationError);
            return false;
        }
        List<User> allUsers = dbManager.loadUsers();

        Optional<User> userOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst();

        if (userOpt.isEmpty()) {
            System.out.println("UserService: User with ID " + userId + " not found.");
            return false;
        }

        User user = userOpt.get();

        // Verify old password
        try {
            String oldPasswordHash = PasswordEncryption.hashPassword(oldPassword);
            if (!oldPasswordHash.equals(user.getPasswordHash())) {
                System.out.println("UserService: Old password is incorrect.");
                return false;
            }

            // Set new password
            String newPasswordHash = PasswordEncryption.hashPassword(newPassword);
            user.setPasswordHash(newPasswordHash);

            dbManager.saveUsers(allUsers);

            System.out.println("UserService: Password for user " + userId + " updated successfully.");
            return true;

        } catch (Exception e) {
            System.err.println("UserService: Error hashing password: " + e.getMessage());
            return false;
        }
    }

    // ==================== Delete User ====================

    /**
     * Deletes a user from the system
     * Also removes them from all enrolled courses (if student)
     * Or removes all their created courses (if instructor)
     */
    public boolean deleteUser(String userId) throws IOException {
        List<User> allUsers = dbManager.loadUsers();
        List<Course> allCourses = dbManager.loadCourses();

        Optional<User> userOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst();

        if (userOpt.isEmpty()) {
            System.out.println("UserService: User with ID " + userId + " not found.");
            return false;
        }

        User user = userOpt.get();

        // If Student: remove from all enrolled courses
        if (user instanceof Student) {
            Student student = (Student) user;
            List<String> enrolledCourses = student.getEnrolledCourses();

            for (String courseId : enrolledCourses) {
                Optional<Course> courseOpt = allCourses.stream()
                        .filter(c -> c.getCourseId().equals(courseId))
                        .findFirst();

                if (courseOpt.isPresent()) {
                    Course course = courseOpt.get();
                    course.removeStudent(userId);
                }
            }
        }

        // If Instructor: delete all their courses
        if (user instanceof Instructor) {
            Instructor instructor = (Instructor) user;
            List<String> createdCourses = instructor.getCreatedCourses();

            // Remove all courses created by this instructor
            allCourses.removeIf(c -> createdCourses.contains(c.getCourseId()));

            // Also unenroll students from deleted courses
            for (User u : allUsers) {
                if (u instanceof Student) {
                    Student s = (Student) u;
                    s.getEnrolledCourses().removeAll(createdCourses);
                    for (String courseId : createdCourses) {
                        s.getProgress().remove(courseId);
                    }
                }
            }
        }

        // Remove user
        allUsers.remove(user);

        // Save changes
        dbManager.saveUsers(allUsers);
        dbManager.saveCourses(allCourses);

        System.out.println("UserService: User " + userId + " deleted successfully.");
        return true;
    }

    // ==================== User Statistics ====================

    /**
     * Gets the total number of users
     */
    public int getTotalUsersCount() throws IOException {
        return dbManager.loadUsers().size();
    }

    /**
     * Gets the total number of students
     */
    public int getTotalStudentsCount() throws IOException {
        return getAllStudents().size();
    }

    /**
     * Gets the total number of instructors
     */
    public int getTotalInstructorsCount() throws IOException {
        return getAllInstructors().size();
    }

    /**
     * Gets detailed statistics for a student
     * Returns: [total enrolled courses, total completed lessons]
     */
    public int[] getStudentStatistics(String studentId) throws IOException {
        Student student = getStudentById(studentId);
        
        if (student == null) {
            return new int[]{0, 0};
        }

        int enrolledCoursesCount = student.getEnrolledCourses().size();
        
        int completedLessonsCount = 0;
        for (List<String> completedLessons : student.getProgress().values()) {
            completedLessonsCount += completedLessons.size();
        }

        return new int[]{enrolledCoursesCount, completedLessonsCount};
    }

    /**
     * Gets detailed statistics for an instructor
     * Returns: [total created courses, total enrolled students across all courses]
     */
    public int[] getInstructorStatistics(String instructorId) throws IOException {
        Instructor instructor = getInstructorById(instructorId);
        List<Course> allCourses = dbManager.loadCourses();

        if (instructor == null) {
            return new int[]{0, 0};
        }

        List<String> createdCourses = instructor.getCreatedCourses();
        int createdCoursesCount = createdCourses.size();

        int totalStudents = 0;
        for (Course course : allCourses) {
            if (createdCourses.contains(course.getCourseId())) {
                totalStudents += course.getStudents().size();
            }
        }

        return new int[]{createdCoursesCount, totalStudents};
    }

    // ==================== Validation Methods ====================

    /**
     * Checks if a user ID exists
     */
    public boolean userExists(String userId) throws IOException {
        return dbManager.findUserById(userId).isPresent();
    }

    /**
     * Checks if an email is already registered
     */
    public boolean emailExists(String email) throws IOException {
        return dbManager.getUserByEmail(email) != null;
    }

    /**
     * Validates email format
     */
    public boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    /**
     * Validates password strength
     * Password must be at least 6 characters
     */
    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // ==================== Search Methods ====================

    /**
     * Searches for users by username (case-insensitive)
     */
    public List<User> searchUsersByUsername(String username) throws IOException {
        List<User> allUsers = dbManager.loadUsers();
        String searchTerm = username.toLowerCase();
        
        return allUsers.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }

    /**
     * Searches for users by email (case-insensitive)
     */
    public List<User> searchUsersByEmail(String email) throws IOException {
        List<User> allUsers = dbManager.loadUsers();
        String searchTerm = email.toLowerCase();
        
        return allUsers.stream()
                .filter(u -> u.getEmail().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }

    // ==================== Bulk Operations ====================

    /**
     * Deletes all users (DANGEROUS - use with caution)
     */
    public boolean deleteAllUsers() throws IOException {
        List<Course> allCourses = dbManager.loadCourses();
        
        // Clear all students from all courses
        for (Course course : allCourses) {
            course.getStudents().clear();
        }
        
        // Delete all courses (since instructors are being deleted)
        allCourses.clear();
        
        dbManager.saveUsers(new java.util.ArrayList<>());
        dbManager.saveCourses(allCourses);
        
        System.out.println("UserService: All users deleted.");
        return true;
    }

    /**
     * Gets users by role
     */
    public List<User> getUsersByRole(String role) throws IOException {
        List<User> allUsers = dbManager.loadUsers();
        
        if ("student".equalsIgnoreCase(role)) {
            return allUsers.stream()
                    .filter(u -> u instanceof Student)
                    .collect(Collectors.toList());
        } else if ("instructor".equalsIgnoreCase(role)) {
            return allUsers.stream()
                    .filter(u -> u instanceof Instructor)
                    .collect(Collectors.toList());
        }
        
        return new java.util.ArrayList<>();
    }

      public void saveQuizResult(String studentId, String courseId, String lessonId, int score) throws IOException {
    QuizResult result = new QuizResult(studentId, courseId, lessonId, score);
    dbManager.saveQuizResult(result);
}
      
  public void markLessonCompleted(String studentId, String courseId, String lessonId) throws IOException {
    Student student = getStudentById(studentId);
    if (student == null) return;

    student.getProgress().putIfAbsent(courseId, new java.util.ArrayList<>());
    List<String> completed = student.getProgress().get(courseId);

    if (!completed.contains(lessonId)) {
        completed.add(lessonId);
        dbManager.saveUsers(dbManager.loadUsers());
    }
}
    
      public double getCourseProgress(String studentId, String courseId) throws IOException {
    Student student = getStudentById(studentId);
    Course course = dbManager.findCourseById(courseId).orElse(null);

    if (student == null || course == null)
        return 0;

    int totalLessons = course.getLessons().size();
    int completed = student.getProgress()
            .getOrDefault(courseId, List.of())
            .size();

    if (totalLessons == 0) return 0;

    return (completed * 100.0) / totalLessons;
}
      
   public double getAverageProgressForCourse(String courseId) throws IOException {
    List<Student> allStudents = getAllStudents();
    double total = 0;
    int count = 0;

    for (Student s : allStudents) {
        if (s.getEnrolledCourses().contains(courseId)) {
            total += getCourseProgress(s.getUserId(), courseId);
            count++;
        }
    }

    return count == 0 ? 0 : total / count;
}

 public double getCourseAverageQuiz(String courseId) throws IOException {
    List<QuizResult> results = dbManager.getQuizResultsForCourse(courseId);
    if (results.isEmpty()) return 0;

    return results.stream().mapToInt(QuizResult::getScore).average().orElse(0);
}

 public double getLessonAverageQuiz(String lessonId) throws IOException {
    List<QuizResult> results = dbManager.getQuizResultsForLesson(lessonId);
    if (results.isEmpty()) return 0;

    return results.stream()
            .mapToInt(QuizResult::getScore)
            .average()
            .orElse(0);
}

 public InstructorInsights getInstructorInsights(String instructorId) throws IOException {
    Instructor instructor = getInstructorById(instructorId);
    if (instructor == null) return null;

    int totalCourses = instructor.getCreatedCourses().size();

    List<Course> courses = dbManager.loadCourses().stream()
            .filter(c -> instructor.getCreatedCourses().contains(c.getCourseId()))
            .collect(Collectors.toList());

    double avgQuiz = 0;
    int totalStudents = 0;

    for (Course c : courses) {
        totalStudents += c.getStudents().size();
        avgQuiz += getCourseAverageQuiz(c.getCourseId());
    }

    if (totalCourses > 0) avgQuiz /= totalCourses;

    return new InstructorInsights(totalCourses, totalStudents, avgQuiz);
}

}
