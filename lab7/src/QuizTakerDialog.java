import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.*;

public class QuizTakerDialog extends JDialog {
    private boolean quizCompleted = false;
    private int score = 0;

    public QuizTakerDialog(Frame parent, String lessonTitle, List<Question> questions) {
        super(parent, "Quiz: " + lessonTitle, true);
        setSize(500, 600);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        if (questions == null || questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No questions available for this quiz.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        List<ButtonGroup> buttonGroups = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            JPanel p = new JPanel(new GridLayout(0, 1));
            p.setBorder(BorderFactory.createTitledBorder("Q" + (i + 1) + ": " + q.getQuestionText()));
            
            ButtonGroup bg = new ButtonGroup();
            List<String> opts = q.getOptions();
            
            for (int j = 0; j < opts.size(); j++) {
                JRadioButton rb = new JRadioButton(opts.get(j));
                rb.setActionCommand(String.valueOf(j));
                bg.add(rb);
                p.add(rb);
            }
            
            buttonGroups.add(bg);
            add(p);
        }

        JButton submitBtn = new JButton("Submit Quiz");
        submitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitBtn.addActionListener(e -> {
            // Check if all questions are answered
            boolean allAnswered = true;
            for (ButtonGroup bg : buttonGroups) {
                if (bg.getSelection() == null) {
                    allAnswered = false;
                    break;
                }
            }

            if (!allAnswered) {
                JOptionPane.showMessageDialog(this, 
                    "Please answer all questions before submitting!", 
                    "Incomplete Quiz", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Calculate score
            int correctCount = 0;
            for (int i = 0; i < questions.size(); i++) {
                ButtonGroup bg = buttonGroups.get(i);
                int selectedIndex = Integer.parseInt(bg.getSelection().getActionCommand());
                if (selectedIndex == questions.get(i).getCorrectOptionIndex()) {
                    correctCount++;
                }
            }

            // Calculate percentage
            score = (int) Math.round(((double) correctCount / questions.size()) * 100);
            String message = "You got " + correctCount + "/" + questions.size() + " (" + score + "%)";

            if (score >= 50) {
                quizCompleted = true;
                JOptionPane.showMessageDialog(this, 
                    message + "\n\n✓ PASSED! The lesson will be marked as completed.", 
                    "Quiz Passed", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                quizCompleted = false;
                JOptionPane.showMessageDialog(this, 
                    message + "\n\n✗ FAILED. You need at least 50% to pass.\nPlease try again.", 
                    "Quiz Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
            dispose();
        });

        add(Box.createVerticalStrut(20));
        add(submitBtn);
    }

    public boolean isQuizCompleted() {
        return quizCompleted;
    }


   public int getScore() {
       return score;
    }
}