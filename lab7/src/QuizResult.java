/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author DANAH
 */
public class QuizResult {
    private String studentId;
    private String courseId;
    private String lessonId;
    private int score;

    public QuizResult(String studentId, String courseId, String lessonId, int score) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.score = score;
    }

    public String getStudentId() { return studentId; }
    public String getCourseId() { return courseId; }
    public String getLessonId() { return lessonId; }
    public int getScore() { return score; }
}
