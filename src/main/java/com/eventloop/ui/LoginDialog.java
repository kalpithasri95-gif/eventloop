package com.eventloop.ui;

import com.eventloop.exception.ValidationException;
import com.eventloop.model.User;
import com.eventloop.service.AuthService;
import com.eventloop.ui.components.ModernUIUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginDialog extends JDialog {
    private final AuthService authService = AuthService.getInstance();
    private boolean authenticated = false;

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginDialog(Frame owner) {
        super(owner, "EventLoop - Smart Event Resource Reuse & Decision System", true);
        setSize(520, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ModernUIUtils.COLOR_BG);

        initComponents();
    }

    private void initComponents() {
        JPanel main = new JPanel(new BorderLayout(16, 16));
        main.setOpaque(false);
        main.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Header Banner
        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.setOpaque(false);

        JLabel lblLogo = new JLabel("🔄  EVENTLOOP");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblLogo.setForeground(ModernUIUtils.COLOR_PRIMARY);

        JLabel lblTag = new JLabel("Smart Event Resource Reuse and Pre-Purchase Decision System");
        lblTag.setFont(ModernUIUtils.FONT_REGULAR);
        lblTag.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        header.add(lblLogo, BorderLayout.NORTH);
        header.add(lblTag, BorderLayout.SOUTH);

        // Form Card
        JPanel formCard = ModernUIUtils.createCard();
        formCard.setLayout(new GridLayout(4, 1, 6, 6));

        txtUsername = new JTextField("admin");
        txtPassword = new JPasswordField("admin123");

        JLabel lblU = new JLabel("Username / Campus Email:");
        lblU.setFont(ModernUIUtils.FONT_BOLD);
        JLabel lblP = new JLabel("Password (Masked):");
        lblP.setFont(ModernUIUtils.FONT_BOLD);

        formCard.add(lblU);
        formCard.add(txtUsername);
        formCard.add(lblP);
        formCard.add(txtPassword);

        // Demo Quick Logins Card
        JPanel demoCard = ModernUIUtils.createCard();
        demoCard.setLayout(new GridLayout(4, 1, 6, 6));

        JLabel lblDemo = new JLabel("⚡ One-Click Demo Role Sign-In:");
        lblDemo.setFont(ModernUIUtils.FONT_SMALL_BOLD);
        lblDemo.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        JButton btnDemoAdmin = ModernUIUtils.createSecondaryButton("👑 Store Manager / Admin (Prof. Rajesh Sharma)");
        btnDemoAdmin.addActionListener(e -> {
            txtUsername.setText("admin");
            txtPassword.setText("admin123");
            performLogin();
        });

        JButton btnDemoOrg = ModernUIUtils.createSecondaryButton("🎯 IEEE Event Organizer (Priya Raman)");
        btnDemoOrg.addActionListener(e -> {
            txtUsername.setText("organizer");
            txtPassword.setText("org123");
            performLogin();
        });

        JButton btnDemoCult = ModernUIUtils.createSecondaryButton("🎭 Cultural Committee (Karthik Sundar)");
        btnDemoCult.addActionListener(e -> {
            txtUsername.setText("cultural");
            txtPassword.setText("cult123");
            performLogin();
        });

        demoCard.add(lblDemo);
        demoCard.add(btnDemoAdmin);
        demoCard.add(btnDemoOrg);
        demoCard.add(btnDemoCult);

        // Center container
        JPanel center = new JPanel(new BorderLayout(12, 12));
        center.setOpaque(false);
        center.add(formCard, BorderLayout.NORTH);
        center.add(demoCard, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        btnPanel.setOpaque(false);

        JButton btnLogin = ModernUIUtils.createPrimaryButton("Sign In to EventLoop");
        btnLogin.setFont(ModernUIUtils.FONT_HEADER);
        btnLogin.addActionListener(e -> performLogin());

        JButton btnRegister = ModernUIUtils.createSecondaryButton("Register New Account");
        btnRegister.addActionListener(e -> {
            RegisterDialog reg = new RegisterDialog(this);
            reg.setVisible(true);
        });

        btnPanel.add(btnLogin);
        btnPanel.add(btnRegister);

        main.add(header, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        main.add(btnPanel, BorderLayout.SOUTH);

        add(main);
    }

    private void performLogin() {
        String u = txtUsername.getText().trim();
        String p = new String(txtPassword.getPassword());

        try {
            User user = authService.login(u, p);
            JOptionPane.showMessageDialog(this, "Welcome back, " + user.getFullName() + "!\nRole: " + user.getRole() + " (" + user.getDepartment() + ")", "Sign In Successful", JOptionPane.INFORMATION_MESSAGE);
            authenticated = true;
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Authentication Failed", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
