/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author monic
 */
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // 1. استخدم Singleton JsonDatabaseManager
        JsonDatabaseManager dbManager = JsonDatabaseManager.getInstance();

        // 2. اصنع الـ Services
        InstructorService instructorService = new InstructorService(dbManager);
        StudentService studentService = new StudentService(dbManager);

        try {
            // 3. استخدم القيم من الملفات (تأكد إن الـ IDs في الملفات String)
            String instructorId = "1"; // userId للمُدرِّس من users.json
            // لازم تستخدم ID لطالب موجود فعليًا في users.json بعد تسجيل حساب من الواجهة
            // مثال: لو سجلت طالب من الواجهة وID بتاعه كان "U100"
            String studentId = "U100"; // <-- غيرها لـ ID الحقيقي لطالب موجود
            String courseId = "0";     // courseId من courses.json ( course with ID 0)

            System.out.println("--- Testing Instructor Features ---");

            // 4. جرب جلب الطلاب المسجلين (مافيش لازم يظهر list فاضي لو مافيش طلاب مسجّلين في الكورس دا)
            List<Student> enrolledStudents = instructorService.getEnrolledStudents(instructorId, courseId);
            System.out.println("Students enrolled in course '" + courseId + "': " + enrolledStudents.size());
            for (Student s : enrolledStudents) {
                System.out.println("  - " + s.getUsername() + " (ID: " + s.getUserId() + ")");
            }

            // 5. جرب إنشاء كورس جديد (ممكن تبص لو الكورس اتعمل فعلاً في courses.json)
            // String newCourseId = "NEW_COURSE_" + System.currentTimeMillis(); // ID فريد نسبيًا
            // Course newCourse = instructorService.createCourse(instructorId, newCourseId, "Test Course", "A course for testing", instructorId);
            // if (newCourse != null) {
            //     System.out.println("Created new course: " + newCourse.getTitle());
            // }

            // 6. جرب إضافة درس لكورس (ممكن تبص لو الدرس اتضاف فعلاً في courses.json)
            // Lesson newLesson = new Lesson("L999", "Test Lesson", "Content for test lesson.");
            // boolean lessonAdded = instructorService.addLessonToCourse(instructorId, courseId, newLesson);
            // if (lessonAdded) {
            //     System.out.println("Added lesson to course " + courseId);
            // }

            System.out.println("\n--- Testing Student Features (Requires a registered Student) ---");

            // 7. جرب جلب الكورسات المتاحة
            List<Course> allCourses = studentService.getAllCourses();
            System.out.println("Available Courses for student:");
            for (Course c : allCourses) {
                System.out.println("- " + c.getTitle() + " (ID: " + c.getCourseId() + ", Instructor: " + c.getInstructorId() + ")");
            }

            // 8. جرب تسجيل الطالب في كورس (ممكن تبص لو الطالب اتضاف فعلاً لقائمة الطلاب في courses.json واتسجّل في enrolledCourses في users.json)
            boolean enrolled = studentService.enrollStudent(studentId, courseId);
            if (enrolled) {
                System.out.println("Student " + studentId + " enrolled in course " + courseId);
            } else {
                System.out.println("Enrollment failed for student " + studentId + " in course " + courseId);
            }

            // 9. جرب إتمام درس (ممكن تبص لو الدرس اتضاف فعلاً في progress في users.json)
            // لازم تتأكد إن الدرس دا موجود فعلاً في الكورس دا (من الـ JSON أو من اللي اتعمل)
            String lessonToComplete = "0"; // <-- غيرها لـ ID حقيقي لدرس موجود في الكورس
            boolean completed = studentService.completeLesson(studentId, courseId, lessonToComplete);
            if (completed) {
                System.out.println("Student " + studentId + " marked lesson " + lessonToComplete + " as completed in course " + courseId);
            } else {
                System.out.println("Failed to mark lesson " + lessonToComplete + " as completed for student " + studentId);
            }

            // 10. جرب جلب الكورسات اللي الطالب مسجّل فيها
            List<Course> enrolledCourses = studentService.getEnrolledCourses(studentId);
            System.out.println("Courses enrolled by student " + studentId + ":");
            for (Course c : enrolledCourses) {
                System.out.println("  - " + c.getTitle() + " (ID: " + c.getCourseId() + ")");
            }

            // 11. جرب جلب الدروس لكورس معين
            List<Lesson> courseLessons = studentService.getCourseLessons(courseId);
            System.out.println("Lessons in course " + courseId + ":");
            for (Lesson l : courseLessons) {
                System.out.println("  - " + l.getTitle() + " (ID: " + l.getLessonId() + ")");
            }

            // 12. جرب جلب تقدم الطالب في كورس معين
            java.util.Map<String, List<String>> progress = studentService.getStudentProgress(studentId, courseId);
            System.out.println("Progress for student " + studentId + " in course " + courseId + ": " + progress.get(courseId));

        } catch (IOException e) {
            System.err.println("IO Error during testing: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("General Error during testing: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n--- Starting GUI ---");
        // 13. بعد انتهاء التجارب، شغّل الواجهة
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}