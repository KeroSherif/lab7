public class Validation {
    
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 50;
    
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 128;
    
    private static final int MIN_TITLE_LENGTH = 1;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_DESCRIPTION_LENGTH = 5000;
    
    public static String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Username cannot be empty.";
        }
        
        username = username.trim();
        
        if (username.length() < MIN_USERNAME_LENGTH) {
            return "Username must be at least " + MIN_USERNAME_LENGTH + " characters long.";
        }
        
        if (username.length() > MAX_USERNAME_LENGTH) {
            return "Username cannot exceed " + MAX_USERNAME_LENGTH + " characters.";
        }
        
        for (char c : username.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '.' && c != '_' && c != '-') {
                return "Username can only contain letters, numbers, dots, underscores, and hyphens.";
            }
        }
        
        return "";
    }
    
    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Email cannot be empty.";
        }
        
        email = email.trim();
        
        if (!email.contains("@") || !email.contains(".")) {
            return "Invalid email format. Please use format: example@domain.com";
        }
        
        int atIndex = email.indexOf("@");
        int dotIndex = email.lastIndexOf(".");
        if (atIndex >= dotIndex) {
            return "Invalid email format. Please use format: example@domain.com";
        }
        
        if (atIndex == 0) {
            return "Invalid email format. Please use format: example@domain.com";
        }
        
        if (dotIndex - atIndex < 2) {
            return "Invalid email format. Please use format: example@domain.com";
        }
        
        if (email.length() > 255) {
            return "Email address is too long.";
        }
        
        return "";
    }
    
    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty.";
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.";
        }
        
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return "Password cannot exceed " + MAX_PASSWORD_LENGTH + " characters.";
        }
        
        return "";
    }
    
    public static String validatePasswordMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return "Password fields cannot be null.";
        }
        
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }
        
        return "";
    }
    
    public static String validateRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return "Role cannot be empty.";
        }
        
        role = role.trim().toLowerCase();
        
        if (!role.equals("student") && !role.equals("instructor")) {
            return "Role must be either 'student' or 'instructor'.";
        }
        
        return "";
    }
    
    public static String validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return "User ID cannot be empty.";
        }
        
        userId = userId.trim();
        if (userId.length() != 36) {
            return "Invalid user ID format.";
        }
        
        if (userId.charAt(8) != '-' || userId.charAt(13) != '-' || 
            userId.charAt(18) != '-' || userId.charAt(23) != '-') {
            return "Invalid user ID format.";
        }
        
        for (int i = 0; i < userId.length(); i++) {
            if (i == 8 || i == 13 || i == 18 || i == 23) {
                continue;
            }
            char c = userId.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                return "Invalid user ID format.";
            }
        }
        
        return "";
    }
    
    public static String validateUserSignup(String username, String email, String password, 
                                           String confirmPassword, String role) {
        String error;
        
        error = validateUsername(username);
        if (!error.isEmpty()) return error;
        
        error = validateEmail(email);
        if (!error.isEmpty()) return error;
        
        error = validatePassword(password);
        if (!error.isEmpty()) return error;
        
        error = validatePasswordMatch(password, confirmPassword);
        if (!error.isEmpty()) return error;
        
        error = validateRole(role);
        if (!error.isEmpty()) return error;
        
        return "";
    }
    
    public static String validateLogin(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return "Email cannot be empty.";
        }
        
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty.";
        }
        
        String error = validateEmail(email);
        if (!error.isEmpty()) return error;
        
        return "";
    }
    
    public static String validateCourseTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Course title cannot be empty.";
        }
        
        title = title.trim();
        
        if (title.length() < MIN_TITLE_LENGTH) {
            return "Course title must have at least " + MIN_TITLE_LENGTH + " character.";
        }
        
        if (title.length() > MAX_TITLE_LENGTH) {
            return "Course title cannot exceed " + MAX_TITLE_LENGTH + " characters.";
        }
        
        return "";
    }
    
    public static String validateCourseDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "Course description cannot be empty.";
        }
        
        description = description.trim();
        
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            return "Course description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters.";
        }
        
        return "";
    }
    
    public static String validateCourseId(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            return "Course ID cannot be empty.";
        }
        
        return "";
    }
    
    public static String validateInstructorId(String instructorId) {
        if (instructorId == null || instructorId.trim().isEmpty()) {
            return "Instructor ID cannot be empty.";
        }
        
        return "";
    }
    
    public static String validateCourse(String title, String description, String instructorId) {
        String error;
        
        error = validateCourseTitle(title);
        if (!error.isEmpty()) return error;
        
        error = validateCourseDescription(description);
        if (!error.isEmpty()) return error;
        
        error = validateInstructorId(instructorId);
        if (!error.isEmpty()) return error;
        
        return "";
    }
    
    public static String validateLessonTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Lesson title cannot be empty.";
        }
        
        title = title.trim();
        
        if (title.length() < MIN_TITLE_LENGTH) {
            return "Lesson title must have at least " + MIN_TITLE_LENGTH + " character.";
        }
        
        if (title.length() > MAX_TITLE_LENGTH) {
            return "Lesson title cannot exceed " + MAX_TITLE_LENGTH + " characters.";
        }
        
        return "";
    }
    
    public static String validateLessonContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "Lesson content cannot be empty.";
        }
        
        content = content.trim();
        
        if (content.length() > MAX_DESCRIPTION_LENGTH) {
            return "Lesson content cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters.";
        }
        
        return "";
    }
    
    public static String validateLessonId(String lessonId) {
        if (lessonId == null || lessonId.trim().isEmpty()) {
            return "Lesson ID cannot be empty.";
        }
        
        return "";
    }
    
    public static String validateLessonResource(String resource) {
        if (resource == null || resource.trim().isEmpty()) {
            return "Resource cannot be empty.";
        }
        
        resource = resource.trim();
        
        if (resource.length() > 2048) {
            return "Resource path/URL is too long.";
        }
        
        return "";
    }
    
    public static String validateLesson(String title, String content) {
        String error;
        
        error = validateLessonTitle(title);
        if (!error.isEmpty()) return error;
        
        error = validateLessonContent(content);
        if (!error.isEmpty()) return error;
        
        return "";
    }
    
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    public static String sanitizeInput(String str) {
        return str == null ? "" : str.trim();
    }
}
