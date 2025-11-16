// Main.java
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // SwingUtilities.invokeLater يضمن إن الواجهة تشتكَل من thread مناسب
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}