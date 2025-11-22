/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javax.swing.*;
import org.jfree.chart.*;
import org.jfree.chart.panel.ChartPanel;
/**
 *
 * @author DANAH
 */
public class ChartFrame extends JFrame {
    public ChartFrame(String title, JFreeChart chart) {
        setTitle(title);
        setSize(800, 600);
        setLocationRelativeTo(null);
        add(new ChartPanel(chart));
    }
}

