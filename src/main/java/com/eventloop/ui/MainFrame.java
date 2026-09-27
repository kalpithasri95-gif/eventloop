package com.eventloop.ui;

import com.eventloop.model.User;
import com.eventloop.service.AuthService;
import com.eventloop.ui.components.ModernUIUtils;
import com.eventloop.ui.panels.DashboardPanel;
import com.eventloop.ui.panels.EventManagementPanel;
import com.eventloop.ui.panels.OOPPolymorphismDemoPanel;
import com.eventloop.ui.panels.ReportsPanel;
import com.eventloop.ui.panels.RequirementMatchingPanel;
import com.eventloop.ui.panels.ReservationPanel;
import com.eventloop.ui.panels.ResourceCatalogPanel;
import com.eventloop.ui.panels.VerificationPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {
    private final AuthService authService = AuthService.getInstance();

    private JTabbedPane tabbedPane;
    private DashboardPanel dashboardPanel;
    private ResourceCatalogPanel catalogPanel;
    private VerificationPanel verificationPanel;
    private EventManagementPanel eventPanel;
    private RequirementMatchingPanel matchingPanel;
    private ReservationPanel reservationPanel;
    private ReportsPanel reportsPanel;
    private OOPPolymorphismDemoPanel oopDemoPanel;

    private JLabel lblUserProfile;
    private JLabel lblUserRole;

    public MainFrame() {
        super("EventLoop – Smart Event Resource Reuse and Pre-Purchase Decision System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 840);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(ModernUIUtils.COLOR_BG);

        initComponents();
        updateUserHeader();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Top Navigation & Profile Bar
        JPanel topBar = new JPanel(new BorderLayout(15, 0));
        topBar.setBackground(ModernUIUtils.COLOR_PRIMARY);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        // Brand Logo & Title
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel lblLogo = new JLabel("EVENTLOOP");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setForeground(Color.WHITE);

        JLabel lblTag = new JLabel("| Smart Resource Reuse & Pre-Purchase Intelligence");
        lblTag.setFont(ModernUIUtils.FONT_REGULAR);
        lblTag.setForeground(new Color(203, 213, 225));

        brandPanel.add(lblLogo);
        brandPanel.add(lblTag);

        // User Profile & Sign Out
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userPanel.setOpaque(false);

        lblUserProfile = new JLabel("User: Guest");
        lblUserProfile.setFont(ModernUIUtils.FONT_BOLD);
        lblUserProfile.setForeground(Color.WHITE);

        lblUserRole = new JLabel(" [ROLE] ");
        lblUserRole.setFont(ModernUIUtils.FONT_SMALL);
        lblUserRole.setForeground(ModernUIUtils.COLOR_SUCCESS);
        lblUserRole.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernUIUtils.COLOR_SUCCESS, 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));

        JButton btnLogout = ModernUIUtils.createButton("Sign Out", new Color(30, 41, 59), Color.WHITE);
        btnLogout.setFont(ModernUIUtils.FONT_SMALL);
        btnLogout.addActionListener(e -> confirmLogout());

        userPanel.add(lblUserProfile);
        userPanel.add(lblUserRole);
        userPanel.add(btnLogout);

        topBar.add(brandPanel, BorderLayout.WEST);
        topBar.add(userPanel, BorderLayout.EAST);

        // Main Tabbed Navigation
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(ModernUIUtils.FONT_BOLD);
        tabbedPane.setBackground(ModernUIUtils.COLOR_BG);

        dashboardPanel = new DashboardPanel(this);
        catalogPanel = new ResourceCatalogPanel();
        verificationPanel = new VerificationPanel();
        eventPanel = new EventManagementPanel(this);
        matchingPanel = new RequirementMatchingPanel();
        reservationPanel = new ReservationPanel();
        reportsPanel = new ReportsPanel();
        oopDemoPanel = new OOPPolymorphismDemoPanel();

        tabbedPane.addTab("  📊 Dashboard  ", dashboardPanel);
        tabbedPane.addTab("  📦 Resource Catalogue  ", catalogPanel);
        tabbedPane.addTab("  🛡️ Verification Audit Gate  ", verificationPanel);
        tabbedPane.addTab("  📅 Event Lifecycle  ", eventPanel);
        tabbedPane.addTab("  ⚡ Smart Matching & Decisions  ", matchingPanel);
        tabbedPane.addTab("  📋 Reservations & Returns  ", reservationPanel);
        tabbedPane.addTab("  📈 Reports & Analytics  ", reportsPanel);
        tabbedPane.addTab("  🧪 OOP & Interfaces Lab  ", oopDemoPanel);

        // Tab change listener for auto-refresh
        tabbedPane.addChangeListener(e -> {
            int sel = tabbedPane.getSelectedIndex();
            switch (sel) {
                case 0: dashboardPanel.refreshData(); break;
                case 1: catalogPanel.loadResources(); break;
                case 2: verificationPanel.loadData(); break;
                case 3: eventPanel.loadEvents(); break;
                case 4: matchingPanel.loadEvents(); break;
                case 5: reservationPanel.loadReservations(); break;
                case 6: reportsPanel.loadSelectedReport(); break;
            }
        });

        add(topBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    public void updateUserHeader() {
        User u = authService.getCurrentUser();
        if (u != null) {
            lblUserProfile.setText("👤 " + u.getFullName() + " (" + u.getDepartment() + ")");
            lblUserRole.setText(" " + u.getRole() + " ");
            if (u.isAdmin()) {
                lblUserRole.setForeground(ModernUIUtils.COLOR_SUCCESS);
            } else {
                lblUserRole.setForeground(ModernUIUtils.COLOR_ACCENT);
            }
        }
    }

    public void showTab(int index) {
        if (index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    public void showMatchingPanelForEvent(String eventId) {
        tabbedPane.setSelectedIndex(4); // Smart Matching tab
        matchingPanel.selectEventById(eventId);
    }

    private void confirmLogout() {
        int conf = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out of EventLoop?", "Logout Confirmation", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            com.eventloop.main.EventLoopApp.restartLogin();
        }
    }
}
