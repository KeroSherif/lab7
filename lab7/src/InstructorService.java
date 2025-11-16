/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Kirolos sherif
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InstructorService {
    private JsonDatabaseManager dbManager;

    public InstructorService(JsonDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Creates a new course and associates it with the instructor.
     * Checks for instructor existence and course ID uniqueness.
     */
    public Course createCourse(String instructorId, String courseId, String title, String description, String instructorIdForCourse) throws IOException {
        List<User> allUsers = dbManager.loadUsers();
        List<Course> allCourses = dbManager.loadCourses();

        Optional<User> instructorOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(instructorId) && u instanceof Instructor)
                .findFirst();

        if (instructorOpt.isEmpty()) {
            System.out.println("InstructorService: Instructor with ID " + instructorId + " not found.");
            return null;
        }

        Instructor instructor = (Instructor) instructorOpt.get();

        if (dbManager.isCourseIdDuplicate(courseId)) {
            System.out.println("InstructorService: Course with ID " + courseId + " already exists.");
            return null;
        }

        Course newCourse = new Course(courseId, title, description, instructorIdForCourse);
        allCourses.add(newCourse);
        instructor.addCreatedCourse(courseId);

        dbManager.saveUsers(allUsers);
        dbManager.saveCourses(allCourses);
        System.out.println("InstructorService: Course '" + title + "' created successfully by instructor " + instructorId);
        return newCourse;
    }

    /**
     * Updates an existing course if the instructor is the owner.
     * Checks for course existence and instructor ownership.
     */
    public boolean updateCourse(String instructorId, String courseId, String newTitle, String newDescription) throws IOException {
        List<User> allUsers = dbManager.loadUsers();
        List<Course> allCourses = dbManager.loadCourses();

        Optional<User> instructorOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(instructorId) && u instanceof Instructor)
                .findFirst();

        if (instructorOpt.isEmpty()) {
            System.out.println("InstructorService: Instructor with ID " + instructorId + " not found.");
            return false;
        }

        Instructor instructor = (Instructor) instructorOpt.get();

        Optional<Course> courseOpt = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst();

        if (courseOpt.isEmpty()) {
            System.out.println("InstructorService: Course with ID " + courseId + " not found.");
            return false;
        }

        Course course = courseOpt.get();

        if (!course.getInstructorId().equals(instructorId)) {
            System.out.println("InstructorService: Instructor " + instructorId + " does not own course " + courseId);
            return false;
        }

        course.setTitle(newTitle);
        course.setDescription(newDescription);

        dbManager.saveCourses(allCourses);
        System.out.println("InstructorService: Course " + courseId + " updated successfully.");
        return true;
    }

    /**
     * Deletes a course if the instructor is the owner.
     * Checks for course existence and instructor ownership.
     * Also removes the course ID from the instructor's list and unenrolls students.
     */
    public boolean deleteCourse(String instructorId, String courseId) throws IOException {
        List<User> allUsers = dbManager.loadUsers();
        List<Course> allCourses = dbManager.loadCourses();

        Optional<User> instructorOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(instructorId) && u instanceof Instructor)
                .findFirst();

        if (instructorOpt.isEmpty()) {
            System.out.println("InstructorService: Instructor with ID " + instructorId + " not found.");
            return false;
        }

        Instructor instructor = (Instructor) instructorOpt.get();

        Optional<Course> courseOpt = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst();

        if (courseOpt.isEmpty()) {
            System.out.println("InstructorService: Course with ID " + courseId + " not found.");
            return false;
        }

        Course course = courseOpt.get();

        if (!course.getInstructorId().equals(instructorId)) {
            System.out.println("InstructorService: Instructor " + instructorId + " does not own course " + courseId);
            return false;
        }

        allCourses.remove(course);
        instructor.removeCreatedCourse(courseId);

        List<String> enrolledStudentIds = new ArrayList<>(course.getStudents());
        for (String studentId : enrolledStudentIds) {
            Optional<User> studentOpt = allUsers.stream()
                    .filter(u -> u.getUserId().equals(studentId) && u instanceof Student)
                    .findFirst();
            if (studentOpt.isPresent()) {
                Student student = (Student) studentOpt.get();
                student.getEnrolledCourses().remove(courseId);
                student.getProgress().remove(courseId);
            }
        }

        dbManager.saveUsers(allUsers);
        dbManager.saveCourses(allCourses);
        System.out.println("InstructorService: Course " + courseId + " deleted successfully.");
        return true;
    }

    /**
     * Adds a lesson to a course if the instructor is the owner.
     * Checks for course existence and instructor ownership.
     */
    public boolean addLessonToCourse(String instructorId, String courseId, Lesson lesson) throws IOException {
        List<Course> allCourses = dbManager.loadCourses();

        Optional<Course> courseOpt = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst();

        if (courseOpt.isEmpty()) {
            System.out.println("InstructorService: Course with ID " + courseId + " not found.");
            return false;
        }

        Course course = courseOpt.get();

        if (!course.getInstructorId().equals(instructorId)) {
            System.out.println("InstructorService: Instructor " + instructorId + " does not own course " + courseId);
            return false;
        }

        course.addLesson(lesson);

        dbManager.saveCourses(allCourses);
        System.out.println("InstructorService: Lesson '" + lesson.getTitle() + "' added to course " + courseId);
        return true;
    }

    /**
     * Updates a lesson within a course if the instructor is the owner.
     * Checks for course existence, instructor ownership, and lesson existence.
     */
    public boolean updateLesson(String instructorId, String courseId, String lessonId, String newTitle, String newContent) throws IOException {
        List<Course> allCourses = dbManager.loadCourses();

        Optional<Course> courseOpt = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst();

        if (courseOpt.isEmpty()) {
            System.out.println("InstructorService: Course with ID " + courseId + " not found.");
            return false;
        }

        Course course = courseOpt.get();

        if (!course.getInstructorId().equals(instructorId)) {
            System.out.println("InstructorService: Instructor " + instructorId + " does not own course " + courseId);
            return false;
        }

        Optional<Lesson> lessonOpt = course.getLessons().stream()
                .filter(l -> l.getLessonId().equals(lessonId))
                .findFirst();

        if (lessonOpt.isEmpty()) {
            System.out.println("InstructorService: Lesson with ID " + lessonId + " not found in course " + courseId);
            return false;
        }

        Lesson lesson = lessonOpt.get();
        lesson.setTitle(newTitle);
        lesson.setContent(newContent);

        dbManager.saveCourses(allCourses);
        System.out.println("InstructorService: Lesson " + lessonId + " in course " + courseId + " updated successfully.");
        return true;
    }

    /**
     * Removes a lesson from a course if the instructor is the owner.
     * Checks for course existence, instructor ownership, and lesson existence.
     */
    public boolean removeLessonFromCourse(String instructorId, String courseId, String lessonId) throws IOException {
        List<Course> allCourses = dbManager.loadCourses();

        Optional<Course> courseOpt = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst();

        if (courseOpt.isEmpty()) {
            System.out.println("InstructorService: Course with ID " + courseId + " not found.");
            return false;
        }

        Course course = courseOpt.get();

        if (!course.getInstructorId().equals(instructorId)) {
            System.out.println("InstructorService: Instructor " + instructorId + " does not own course " + courseId);
            return false;
        }

        boolean removed = course.removeLesson(lessonId);
        if (!removed) {
            System.out.println("InstructorService: Lesson with ID " + lessonId + " not found in course " + courseId);
            return false;
        }

        dbManager.saveCourses(allCourses);
        System.out.println("InstructorService: Lesson " + lessonId + " removed from course " + courseId);
        return true;
    }

    /**
     * Gets a list of students enrolled in a specific course, if the instructor is the owner.
     * Checks for course existence and instructor ownership.
     */
    public List<Student> getEnrolledStudents(String instructorId, String courseId) throws IOException {
        List<User> allUsers = dbManager.loadUsers();
        List<Course> allCourses = dbManager.loadCourses();

        Optional<Course> courseOpt = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst();

        if (courseOpt.isEmpty()) {
            System.out.println("InstructorService: Course with ID " + courseId + " not found.");
            return new ArrayList<>();
        }

        Course course = courseOpt.get();

        if (!course.getInstructorId().equals(instructorId)) {
            System.out.println("InstructorService: Instructor " + instructorId + " does not own course " + courseId);
            return new ArrayList<>();
        }

        List<String> enrolledStudentIds = course.getStudents();
        List<Student> enrolledStudents = allUsers.stream()
                .filter(u -> u instanceof Student && enrolledStudentIds.contains(u.getUserId()))
                .map(u -> (Student) u)
                .collect(Collectors.toList());

        System.out.println("InstructorService: Found " + enrolledStudents.size() + " students enrolled in course " + courseId);
        return enrolledStudents;
    }

    // --- API Methods ---
    /**
     * Generates a report of completion status for all students in a specific course,
     * if the instructor is the owner of the course.
     * @param instructorId The ID of the instructor requesting the report.
     * @param courseId The ID of the course.
     * @return A list of CourseStudentProgressReport objects containing student info and status.
     * @throws IOException if there's an error reading data.
     */
    public List<CourseStudentProgressReport> getCourseCompletionReport(String instructorId, String courseId) throws IOException {
        List<User> allUsers = dbManager.loadUsers();
        List<Course> allCourses = dbManager.loadCourses();

        Optional<Course> courseOpt = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst();
        if (courseOpt.isEmpty()) {
            System.out.println("InstructorService: Course with ID " + courseId + " not found.");
            return new ArrayList<>();
        }
        Course course = courseOpt.get();
        if (!course.getInstructorId().equals(instructorId)) {
            System.out.println("InstructorService: Instructor " + instructorId + " does not own course " + courseId);
            return new ArrayList<>();
        }

        List<String> enrolledStudentIds = new ArrayList<>(course.getStudents());
        List<CourseStudentProgressReport> report = new ArrayList<>();

        for (String studentId : enrolledStudentIds) {
            Optional<User> studentOpt = allUsers.stream()
                    .filter(u -> u.getUserId().equals(studentId) && u instanceof Student)
                    .findFirst();
            if (studentOpt.isPresent()) {
                Student student = (Student) studentOpt.get();
                String status = "Incomplete";
                int totalLessons = course.getLessons().size();
                int completedLessons = 0;

                Map<String, List<String>> progress = student.getProgress();
                if (progress.containsKey(courseId)) {
                    completedLessons = progress.get(courseId).size();
                    if (completedLessons == totalLessons && totalLessons > 0) {
                        status = "Complete";
                    }
                }
                report.add(new CourseStudentProgressReport(
                        student.getUserId(),
                        student.getUsername(),
                        student.getEmail(),
                        completedLessons,
                        totalLessons,
                        status
                ));
            }
        }

        System.out.println("InstructorService: Generated completion report for course " + courseId + ". Found " + report.size() + " students.");
        return report;
    }

    /**
     * Helper class to hold completion report data for a single student in a course.
     */
    class CourseStudentProgressReport {
        private String studentId;
        private String studentUsername;
        private String studentEmail;
        private int completedLessons;
        private int totalLessons;
        private String status;

        public CourseStudentProgressReport(String studentId, String studentUsername, String studentEmail, int completedLessons, int totalLessons, String status) {
            this.studentId = studentId;
            this.studentUsername = studentUsername;
            this.studentEmail = studentEmail;
            this.completedLessons = completedLessons;
            this.totalLessons = totalLessons;
            this.status = status;
        }

        // Getters
        public String getStudentId() { return studentId; }
        public String getStudentUsername() { return studentUsername; }
        public String getStudentEmail() { return studentEmail; }
        public int getCompletedLessons() { return completedLessons; }
        public int getTotalLessons() { return totalLessons; }
        public String getStatus() { return status; }
    }
}
