import java.awt.Component;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

public class QuizTakerDialog extends JDialog {
    private boolean quizCompleted = false;
    private int score = 0;
    
    public QuizTakerDialog(Frame parent, String lessonTitle, List<Question> questions) {
        super(parent, "Quiz: " + lessonTitle, true);
        setSize(500, 600);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        List<ButtonGroup> buttonGroups = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            JPanel p = new JPanel(new GridLayout(0, 1));
            p.setBorder(BorderFactory.createTitledBorder("Q" + (i+1) + ": " + q.getQuestionText()));
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
            int correctCount = 0;
            for (int i = 0; i < questions.size(); i++) {
                ButtonGroup bg = buttonGroups.get(i);
                if (bg.getSelection() != null) {
                    int selectedIndex = Integer.parseInt(bg.getSelection().getActionCommand());
                    if (selectedIndex == questions.get(i).getCorrectOptionIndex()) {
                        correctCount++;
                    }
                }
            }

            // 3. Calculate Score
            score = (int) (((double)correctCount / questions.size()) * 100);
            String message = "You got " + correctCount + "/" + questions.size() + " (" + score + "%)";
            
            // 4. Pass/Fail Logic (e.g., must get > 50%)
            if (score >= 50) {
                quizCompleted = true;
                JOptionPane.showMessageDialog(this, message + "\nPASSED!", "Result", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                quizCompleted = false;
                JOptionPane.showMessageDialog(this, message + "\nFAILED. You need 50% to complete this lesson.", "Result", JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        });

        add(Box.createVerticalStrut(20));
        add(new JScrollPane(submitBtn)); // Wrap if many questions
        
        // Wrap everything in ScrollPane
        // (Simplified for brevity, ideally wrap the main panel)
    }

    public boolean isQuizCompleted() { return quizCompleted; }
}
