/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author monic
 */
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // خلي النظام يبدأ من LoginFrame
        // SwingUtilities.invokeLater يضمن إن الواجهة تشتكَل من thread مناسب
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}