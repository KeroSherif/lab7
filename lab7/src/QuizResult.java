public class QuizResult {
    private String studentId;
    private String courseId;
    private String lessonId;
    private int score;

    public QuizResult() {
    }

    public QuizResult(String studentId, String courseId, String lessonId, int score) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.score = score;
    }

    
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    
    public String getLessonId() { return lessonId; }
    public void setLessonId(String lessonId) { this.lessonId = lessonId; }
    
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}