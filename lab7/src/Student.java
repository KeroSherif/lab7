import java.util.*;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 *
 * @author Kirolos sherif
 */
@JsonTypeName("student") // <-- مهم
public class Student extends User {
    private List<String> enrolledCourses;
    private Map<String, List<String>> progress;
    // progress: key = courseId, value = list of completed lessonIds
    private List<String> notifications; // <-- New field for notifications

    // Constructor فاضي لـ Jackson
    public Student() {
        super(); // لازم تستخدم constructor الـ parent فاضي لـ Jackson
        this.enrolledCourses = new ArrayList<>();
        this.progress = new HashMap<>(); // <-- مهم، مش null
        this.notifications = new ArrayList<>(); // Initialize notifications list
    }

    public Student(String userId, String username, String email, String passwordHash) {
        super(userId, "student", username, email, passwordHash);
        this.enrolledCourses = new ArrayList<>();
        this.progress = new HashMap<>();
        this.notifications = new ArrayList<>(); // Initialize notifications list
    }

    public List<String> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(List<String> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

    public Map<String, List<String>> getProgress() {
        return progress;
    }

    public void setProgress(Map<String, List<String>> progress) {
        if (progress == null) {
            this.progress = new HashMap<>(); // <-- مهم، مش null
        } else {
            this.progress = progress;
        }
    }

    // Getter and Setter for notifications
    public List<String> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<String> notifications) {
        if (notifications == null) {
            this.notifications = new ArrayList<>();
        } else {
            this.notifications = notifications;
        }
    }

    // Method to add a notification
    public void addNotification(String notification) {
        if (notification != null && !notification.trim().isEmpty()) {
            this.notifications.add(notification);
        }
    }

    public void enrollCourse(String courseId) {
        if (!enrolledCourses.contains(courseId)) {
            enrolledCourses.add(courseId);
            progress.put(courseId, new ArrayList<>());
        }
    }

    public void markLessonCompleted(String courseId, String lessonId) {
        if (!progress.containsKey(courseId))
            progress.put(courseId, new ArrayList<>());
        List<String> completed = progress.get(courseId);
        if (!completed.contains(lessonId)) {
            completed.add(lessonId);
        }
    }
    
    private List<Map<String, String>> certificates = new ArrayList<>();

    public List<Map<String, String>> getCertificates() {
    return certificates;
    }

    public void addCertificate(Map<String, String> cert) {
    certificates.add(cert);
    }

}