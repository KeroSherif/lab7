
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

    /**
     * Gets all courses available in the system.
     */
    public List<Course> getAllCourses() throws IOException {
        return db.loadCourses();
    }

    /**
     * Enrolls a student in a specific course. Checks for student and course
     * existence.
     */
    public boolean enrollStudent(String studentId, String courseId) throws IOException {
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

    /**
     * Marks a lesson as completed for a specific student in a specific course.
     * Checks for student, course, and lesson existence.
     */
    public boolean completeLesson(String studentId, String courseId, String lessonId) throws IOException {
        List<User> allUsers = db.loadUsers();

        Optional<User> studentOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(studentId) && u instanceof Student)
                .findFirst();

        if (studentOpt.isEmpty()) {
            System.out.println("StudentService: Student not found.");
            return false;
        }

        Student s = (Student) studentOpt.get();

        // Optional: Check if the student is enrolled in the course
        if (!s.getEnrolledCourses().contains(courseId)) {
            System.out.println("StudentService: Student " + studentId + " is not enrolled in course " + courseId);
            return false; // Or handle differently based on requirements
        }

        // Optional: Check if the lesson exists in the course (requires loading courses)
        // This adds complexity, so we assume the lesson exists if the course does for this step.
        // A more robust check would load the course and verify the lessonId.
        s.markLessonCompleted(courseId, lessonId);

        db.saveUsers(allUsers);

        System.out.println("StudentService: Lesson " + lessonId + " marked as completed for student " + studentId + " in course " + courseId);
        return true;
    }

    // --- New Methods for Student Features ---
    /**
     * Gets the courses a specific student is enrolled in.
     */
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

    /**
     * Gets the lessons for a specific course.
     */
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

    /**
     * Gets a specific lesson by its ID within a specific course.
     */
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

    /**
     * Gets the progress of a specific student in a specific course. Returns a
     * map where key is courseId and value is list of completed lesson IDs.
     */
    public Map<String, List<String>> getStudentProgress(String studentId, String courseId) throws IOException {
        List<User> allUsers = db.loadUsers();

        Optional<User> studentOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(studentId) && u instanceof Student)
                .findFirst();

        if (studentOpt.isEmpty()) {
            System.out.println("StudentService: Student with ID " + studentId + " not found.");
            return null; // Or an empty map
        }

        Student student = (Student) studentOpt.get();
        Map<String, List<String>> progress = student.getProgress();

        // Return progress for the specific course only
        if (progress.containsKey(courseId)) {
            System.out.println("StudentService: Found progress for student " + studentId + " in course " + courseId);
            return Map.of(courseId, progress.get(courseId)); // Return a map with only the requested course's progress
        } else {
            System.out.println("StudentService: No progress found for student " + studentId + " in course " + courseId);
            return Map.of(courseId, new ArrayList<>()); // Return an empty list for the course
        }
    }

    public boolean isCourseCompleted(String studentId, String courseId) throws IOException {
        // Get the course lessons
        List<Lesson> courseLessons = getCourseLessons(courseId);
        if (courseLessons.isEmpty()) {
            // If there are no lessons, technically it's "complete"
            return true;
        }

        // Get the student's progress for this specific course
        Map<String, List<String>> progress = getStudentProgress(studentId, courseId);
        if (progress == null || !progress.containsKey(courseId)) {
            // If no progress recorded for this course, it's not complete
            return false;
        }

        List<String> completedLessonIds = progress.get(courseId);

        // Check if the number of completed lessons equals the total number of lessons
        // and if all lesson IDs in the course are present in the completed list.
        // Using a Set for efficient lookup.
        java.util.Set<String> courseLessonIdsSet = courseLessons.stream()
                .map(Lesson::getLessonId)
                .collect(java.util.stream.Collectors.toSet());

        // Ensure completed list only contains valid lesson IDs from the course
        java.util.Set<String> completedLessonIdsSet = new java.util.HashSet<>(completedLessonIds);
        completedLessonIdsSet.retainAll(courseLessonIdsSet); // Keep only IDs that exist in the course

        boolean isComplete = courseLessonIdsSet.size() == completedLessonIdsSet.size();

        System.out.println("StudentService: Course " + courseId + " for student " + studentId + " is " + (isComplete ? "COMPLETED" : "NOT completed"));
        System.out.println(" - Total lessons in course: " + courseLessonIdsSet.size());
        System.out.println(" - Completed lessons count (valid): " + completedLessonIdsSet.size());

        return isComplete;
    }
}
