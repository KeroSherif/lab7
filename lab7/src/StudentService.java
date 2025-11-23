import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StudentService {
    private JsonDatabaseManager db;

    public StudentService(JsonDatabaseManager db) {
        this.db = db;
    }

    
    public List<Course> getAllCourses() throws IOException {
        return db.loadCourses();
    }

    
    public boolean enrollStudent(String studentId, String courseId) throws IOException {
        String err = Validation.validateUserId(studentId);
        if (!err.isEmpty()) {
            System.out.println("StudentService: " + err);
            return false;
        }
        err = Validation.validateCourseId(courseId);
        if (!err.isEmpty()) {
            System.out.println("StudentService: " + err);
            return false;
        }
        List<User> allUsers = db.loadUsers();
        List<Course> allCourses = db.loadCourses();
        Optional<User> studentOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(studentId) && u instanceof Student)
                .findFirst();
        Optional<Course> courseOpt = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst();

        if (studentOpt.isEmpty() || courseOpt.isEmpty()) {
            System.out.println("StudentService: Student or Course not found.");
            return false;
        }

        Student s = (Student) studentOpt.get();
        Course c = courseOpt.get();

        if (s.getEnrolledCourses().contains(courseId)) {
            System.out.println("StudentService: Student already enrolled in course: " + courseId);
            return false;
        }

        s.enrollCourse(courseId);
        c.addStudent(studentId);

        db.saveUsers(allUsers);
        db.saveCourses(allCourses);
        System.out.println("StudentService: Student " + studentId + " enrolled in course " + courseId);
        return true;
    }

   
    public boolean completeLesson(String studentId, String courseId, String lessonId) throws IOException {
       String err = Validation.validateUserId(studentId);
        if (!err.isEmpty()) {
            System.out.println("StudentService: " + err);
            return false;
        }
        err = Validation.validateCourseId(courseId);
        if (!err.isEmpty()) {
            System.out.println("StudentService: " + err);
            return false;
        }
        err = Validation.validateLessonId(lessonId);
        if (!err.isEmpty()) {
            System.out.println("StudentService: " + err);
            return false;
        }
        
        List<User> allUsers = db.loadUsers();
        Optional<User> studentOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(studentId) && u instanceof Student)
                .findFirst();

        if (studentOpt.isEmpty()) {
            System.out.println("StudentService: Student not found.");
            return false;
        }

        Student s = (Student) studentOpt.get();

    
        if (!s.getEnrolledCourses().contains(courseId)) {
             System.out.println("StudentService: Student " + studentId + " is not enrolled in course " + courseId);
             return false; 
        }

        
        s.markLessonCompleted(courseId, lessonId);
        db.saveUsers(allUsers);
        System.out.println("StudentService: Lesson " + lessonId + " marked as completed for student " + studentId + " in course " + courseId);
        return true;
    }

    
    public List<Course> getEnrolledCourses(String studentId) throws IOException {
        List<User> allUsers = db.loadUsers();
        List<Course> allCourses = db.loadCourses();
        Optional<User> studentOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(studentId) && u instanceof Student)
                .findFirst();

        if (studentOpt.isEmpty()) {
            System.out.println("StudentService: Student with ID " + studentId + " not found.");
            return new ArrayList<>();
        }

        Student student = (Student) studentOpt.get();
        List<String> enrolledCourseIds = student.getEnrolledCourses();

        List<Course> enrolledCourses = allCourses.stream()
                .filter(c -> enrolledCourseIds.contains(c.getCourseId()))
                .collect(Collectors.toList());

        System.out.println("StudentService: Found " + enrolledCourses.size() + " enrolled courses for student " + studentId);
        return enrolledCourses;
    }

    
    public List<Lesson> getCourseLessons(String courseId) throws IOException {
        List<Course> allCourses = db.loadCourses();
        Optional<Course> courseOpt = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst();

        if (courseOpt.isEmpty()) {
            System.out.println("StudentService: Course with ID " + courseId + " not found.");
            return new ArrayList<>();
        }

        Course course = courseOpt.get();
        List<Lesson> lessons = course.getLessons();
        System.out.println("StudentService: Found " + lessons.size() + " lessons in course " + courseId);
        return lessons;
    }

   
    public Lesson getLessonById(String courseId, String lessonId) throws IOException {
        List<Course> allCourses = db.loadCourses();
        Optional<Course> courseOpt = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst();

        if (courseOpt.isEmpty()) {
            System.out.println("StudentService: Course with ID " + courseId + " not found.");
            return null;
        }

        Course course = courseOpt.get();
        Optional<Lesson> lessonOpt = course.getLessons().stream()
                .filter(l -> l.getLessonId().equals(lessonId))
                .findFirst();

        if (lessonOpt.isEmpty()) {
            System.out.println("StudentService: Lesson with ID " + lessonId + " not found in course " + courseId);
            return null;
        }

        System.out.println("StudentService: Found lesson " + lessonId + " in course " + courseId);
        return lessonOpt.get();
    }

   
    public Map<String, List<String>> getStudentProgress(String studentId, String courseId) throws IOException {
        List<User> allUsers = db.loadUsers();
        Optional<User> studentOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(studentId) && u instanceof Student)
                .findFirst();

        if (studentOpt.isEmpty()) {
            System.out.println("StudentService: Student with ID " + studentId + " not found.");
            return null; 
        }

        Student student = (Student) studentOpt.get();
        Map<String, List<String>> progress = student.getProgress();

        if (progress.containsKey(courseId)) {
            System.out.println("StudentService: Found progress for student " + studentId + " in course " + courseId);
            return Map.of(courseId, progress.get(courseId));
        } else {
            System.out.println("StudentService: No progress found for student " + studentId + " in course " + courseId);
            return Map.of(courseId, new ArrayList<>());
        }
    }

   
    public boolean isCourseCompleted(String studentId, String courseId) throws IOException {
        // Get the course lessons
        List<Lesson> courseLessons = getCourseLessons(courseId);
        if (courseLessons.isEmpty()) {

            return false;
        }

        Map<String, List<String>> progress = getStudentProgress(studentId, courseId);
        if (progress == null || !progress.containsKey(courseId)) {
            return false;
        }

        List<String> completedLessonIds = progress.get(courseId);

        java.util.Set<String> courseLessonIdsSet = courseLessons.stream()
                .map(Lesson::getLessonId)
                .collect(java.util.stream.Collectors.toSet());

        java.util.Set<String> completedLessonIdsSet = new java.util.HashSet<>(completedLessonIds);
        completedLessonIdsSet.retainAll(courseLessonIdsSet); 

        boolean isComplete = courseLessonIdsSet.size() == completedLessonIdsSet.size();

        System.out.println("StudentService: Course " + courseId + " for student " + studentId + " is " + (isComplete ? "COMPLETED" : "NOT completed"));
        System.out.println(" - Total lessons in course: " + courseLessonIdsSet.size());
        System.out.println(" - Completed lessons count (valid): " + completedLessonIdsSet.size());

        return isComplete;
    }

   
    public String getStudentCourseCompletionStatus(String studentId, String courseId) throws 
            IOException {
        return isCourseCompleted(studentId, courseId) ? "Complete" : "Incomplete";
    }

   
    public List<String> getCompletedCourses(String studentId) throws IOException {
        List<String> completedCourseIds = new ArrayList<>();
        List<Course> allCourses = db.loadCourses();

        for (Course course : allCourses) {
            if (isCourseCompleted(studentId, course.getCourseId())) {
                completedCourseIds.add(course.getCourseId());
            }
        }
        System.out.println("StudentService: Found " + completedCourseIds.size() + " completed courses for student " + studentId);
        return completedCourseIds;
    }
    public boolean isCourseCompleted(Student student, List<Lesson> lessons) {
    for (Lesson lesson : lessons) {
        if (!student.getPassedQuizzes().contains(lesson.getQuizId())) {
            return false;
        }
    }
    return true;
    }
    public Map<String, String> generateCertificate(Student student, String courseId) {
    Map<String, String> cert = new HashMap<>();

    String certId = "CERT-" + UUID.randomUUID().toString().substring(0, 8);

    cert.put("certificateId", certId);
    cert.put("studentId", student.getId());
    cert.put("courseId", courseId);
    cert.put("issueDate", LocalDate.now().toString());

    student.addCertificate(cert);
    JsonDatabaseManager.saveStudent(student);

    JsonDatabaseManager.saveCertificate(cert);

    return cert;
}


}
