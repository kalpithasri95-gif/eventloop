package com.eventloop.main;

import com.eventloop.dao.DatabaseManager;
import com.eventloop.ui.LoginDialog;
import com.eventloop.ui.MainFrame;
import java.awt.Color;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application launcher for EventLoop.
 * Initializes SQLite database schema, seeds rich sample datasets,
 * sets modern desktop UI styling, and coordinates authentication workflow.
 */
public class EventLoopApp {

    public static void main(String[] args) {
        // Set modern FlatLaf Look and Feel with custom rounded styling
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("TabbedPane.tabInsets", new java.awt.Insets(10, 18, 10, 18));
            UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
        } catch (Throwable t) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ex) {
                // Fallback
            }
        }

        // Initialize SQLite Database schema and sample records
        System.out.println("Initializing EventLoop Database and Schema...");
        DatabaseManager.initializeDatabase();
        System.out.println("Database Initialized successfully.");

        // Start UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> launchApp());
    }

    public static void launchApp() {
        LoginDialog loginDialog = new LoginDialog(null);
        loginDialog.setVisible(true);

        if (loginDialog.isAuthenticated()) {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        } else {
            System.exit(0);
        }
    }

    public static void restartLogin() {
        SwingUtilities.invokeLater(() -> launchApp());
    }
}
