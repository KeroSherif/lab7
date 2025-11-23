import java.util.List;

public class Question {
    private String questionString;
    private List<String> choices;
    private int correctAnswerIndex;
    public Question() {
    }
    Question(String questionString, List<String> choices, int correctAnswerIndex) {
        this.questionString = questionString;
        this.choices = choices;
        this.correctAnswerIndex = correctAnswerIndex;
    }
    public String getQuestionText() { return questionString; }
    public void setQuestionText(String questionString) { this.questionString = questionString; }
    
    public List<String> getOptions() { return choices; }
    public void setOptions(List<String> options) { this.choices      = options; }
    
    public int getCorrectOptionIndex() { return correctAnswerIndex; }
    public void setCorrectOptionIndex(int correctOptionIndex) { this.correctAnswerIndex = correctOptionIndex; }
}
