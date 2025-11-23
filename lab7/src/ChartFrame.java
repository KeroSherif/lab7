import javax.swing.*;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;

public class ChartFrame extends JFrame {

    public ChartFrame(String title, JFreeChart chart) {
        super(title);

       
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        
        ChartPanel chartPanel = new ChartPanel(chart);
        setContentPane(chartPanel);
    }
}
