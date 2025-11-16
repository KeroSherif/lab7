/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class StudentService {
    private JsonDatabaseManager db;

    public StudentService(JsonDatabaseManager db) {
        this.db = db;
    }

    public List<Course> getAllCourses() throws IOException {
        return db.loadCourses();
    }

    public boolean enrollStudent(String studentId, String courseId) throws IOException {
        // 1. جيب القوائم من قاعدة البيانات
        List<User> allUsers = db.loadUsers();
        List<Course> allCourses = db.loadCourses();

        // 2. بحث عن الطالب والكورس في القوائم
        Optional<User> studentOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(studentId) && u instanceof Student) // تأكد من النوع
                .findFirst();

        Optional<Course> courseOpt = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst();

        // 3. تحقق من وجودهم
        if (studentOpt.isEmpty() || courseOpt.isEmpty()) {
            System.out.println("Student or Course not found.");
            return false; // فشل التسجيل
        }

        Student s = (Student) studentOpt.get();
        Course c = courseOpt.get();

        // 4. تحقق من أن الطالب مش مسجل من قبل
        if (s.getEnrolledCourses().contains(courseId)) {
            System.out.println("Student already enrolled in course: " + courseId);
            return false; // فشل التسجيل
        }

        // 5. أضف الكورس لقائمة الكورسات المسجلة في الكائن student
        s.enrollCourse(courseId);

        // 6. أضف الطالب لقائمة الطلاب في الكائن course
        c.addStudent(studentId);

        // 7. احفظ القوائم المحدثة
        db.saveUsers(allUsers);
        db.saveCourses(allCourses);

        System.out.println("Student " + studentId + " enrolled in course " + courseId);
        return true; // نجح التسجيل
    }

    public boolean completeLesson(String studentId, String courseId, String lessonId) throws IOException {
        // 1. جيب قائمة المستخدمين
        List<User> allUsers = db.loadUsers();

        // 2. بحث عن الطالب في القائمة
        Optional<User> studentOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(studentId) && u instanceof Student) // تأكد من النوع
                .findFirst();

        // 3. تحقق من وجود الطالب
        if (studentOpt.isEmpty()) {
            System.out.println("Student not found.");
            return false; // فشل
        }

        Student s = (Student) studentOpt.get();

        // 4. علّم الدرس على إنه مكتمل في الكائن student
        s.markLessonCompleted(courseId, lessonId);

        // 5. احفظ قائمة المستخدمين المحدثة
        db.saveUsers(allUsers);

        System.out.println("Lesson " + lessonId + " marked as completed for student " + studentId + " in course " + courseId);
        return true; // نجح
    }
}
