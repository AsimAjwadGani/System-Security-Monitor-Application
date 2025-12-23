import javax.swing.*;
import java.awt.*;

public class SystemSecurityMonitor extends JFrame {
    private SecurityMonitorGUI gui;
    
    public SystemSecurityMonitor() {
        setTitle("System Security Monitor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        gui = new SecurityMonitorGUI();
        add(gui, BorderLayout.CENTER);
        
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SystemSecurityMonitor();
        });
    }
}