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
import java.util.Optional;

public class AdminService {

    private JsonDatabaseManager dbManager;

    public AdminService(JsonDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<User> getAllUsers() throws IOException {
        List<User> allUsers = dbManager.loadUsers();
        return allUsers.stream()
                .filter(u -> !(u instanceof Admin)) 
                .toList();
    }

   
    public boolean deleteUser(String userId) throws IOException {
        List<User> allUsers = dbManager.loadUsers();
        List<Course> allCourses = dbManager.loadCourses();

        Optional<User> userOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst();

        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();

      
        if (user instanceof Student) {
            Student s = (Student) user;
            for (String courseId : s.getEnrolledCourses()) {
                Optional<Course> courseOpt = allCourses.stream()
                        .filter(c -> c.getCourseId().equals(courseId))
                        .findFirst();
                courseOpt.ifPresent(c -> c.getStudents().remove(userId));
            }
        }

        
        if (user instanceof Instructor) {
            Instructor i = (Instructor) user;
            List<String> courseIds = new ArrayList<>(i.getCreatedCourses());
            allCourses.removeIf(c -> courseIds.contains(c.getCourseId()));
            
            for (User u : allUsers) {
                if (u instanceof Student) {
                    Student s = (Student) u;
                    s.getEnrolledCourses().removeAll(courseIds);
                    courseIds.forEach(s.getProgress()::remove);
                }
            }
        }

        allUsers.remove(user);
        dbManager.saveUsers(allUsers);
        dbManager.saveCourses(allCourses);
        return true;
    }

   
    public boolean promoteToAdmin(String userId) throws IOException {
       
        List<User> allUsers = dbManager.loadUsers();
        Optional<User> userOpt = allUsers.stream()
                .filter(u -> u.getUserId().equals(userId) && !(u instanceof Admin))
                .findFirst();

        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();

        Admin newAdmin = new Admin(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash()
        );

        
        List<Admin> admins = dbManager.loadAdmins();
        admins.add(newAdmin);
        dbManager.saveAdmins(admins);

        
        allUsers.remove(user);
        dbManager.saveUsers(allUsers);

        return true;
    }

    public List<Course> getPendingCourses() throws IOException {
        return dbManager.loadCourses()
                .stream()
                .filter(c -> c.getApprovalStatus() == Course.ApprovalStatus.PENDING)
                .toList();
    }

    public boolean approveCourse(String courseId) throws IOException {
        List<Course> courses = dbManager.loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                c.setApprovalStatus(Course.ApprovalStatus.APPROVED);
                dbManager.saveCourses(courses);
                return true;
            }
        }
        return false;
    }

    public boolean rejectCourse(String courseId) throws IOException {
        List<Course> courses = dbManager.loadCourses();
        for (Course c : courses) {
            if (c.getCourseId().equals(courseId)) {
                c.setApprovalStatus(Course.ApprovalStatus.REJECTED);
                dbManager.saveCourses(courses);
                return true;
            }
        }
        return false;
    }
}
