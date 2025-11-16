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
           
            String instructorId = "1";
            
            String studentId = "U100"; 
            String courseId = "0";    

            System.out.println("--- Testing Instructor Features ---");

            
            List<Student> enrolledStudents = instructorService.getEnrolledStudents(instructorId, courseId);
            System.out.println("Students enrolled in course '" + courseId + "': " + enrolledStudents.size());
            for (Student s : enrolledStudents) {
                System.out.println("  - " + s.getUsername() + " (ID: " + s.getUserId() + ")");
            }

           

            System.out.println("\n--- Testing Student Features (Requires a registered Student) ---");

           
            List<Course> allCourses = studentService.getAllCourses();
            System.out.println("Available Courses for student:");
            for (Course c : allCourses) {
                System.out.println("- " + c.getTitle() + " (ID: " + c.getCourseId() + ", Instructor: " + c.getInstructorId() + ")");
            }

            
            boolean enrolled = studentService.enrollStudent(studentId, courseId);
            if (enrolled) {
                System.out.println("Student " + studentId + " enrolled in course " + courseId);
            } else {
                System.out.println("Enrollment failed for student " + studentId + " in course " + courseId);
            }

            
            String lessonToComplete = "0"; 
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
    
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}