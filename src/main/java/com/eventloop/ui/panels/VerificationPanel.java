package com.eventloop.ui.panels;

import com.eventloop.model.resources.AbstractResource;
import com.eventloop.service.AuthService;
import com.eventloop.service.ResourceService;
import com.eventloop.ui.components.ModernUIUtils;
import com.eventloop.util.DateUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class VerificationPanel extends JPanel {
    private final ResourceService resourceService = ResourceService.getInstance();
    private final AuthService authService = AuthService.getInstance();

    private JTable tblPendingVerifications;
    private DefaultTableModel pendingModel;
    private JTextField txtNextDate;
    private JTextField txtNotes;

    public VerificationPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernUIUtils.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadData();
    }

    private void initComponents() {
        // Top Warning Banner
        JPanel bannerCard = ModernUIUtils.createCard();
        bannerCard.setLayout(new BorderLayout(10, 5));

        JLabel lblTitle = new JLabel("Resource Verification & Physical Audit Gate");
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_PRIMARY_DARK);

        JLabel lblWarning = new JLabel("<html><b>Verification Required Policy:</b> Any resource whose periodic verification has expired or is unverified is strictly <b>blocked from reservations</b> until physically audited and recertified by the Store Manager.</html>");
        lblWarning.setFont(ModernUIUtils.FONT_REGULAR);
        lblWarning.setForeground(ModernUIUtils.COLOR_WARNING);

        bannerCard.add(lblTitle, BorderLayout.NORTH);
        bannerCard.add(lblWarning, BorderLayout.SOUTH);

        // Verification Form & Action Bar
        JPanel actionCard = ModernUIUtils.createCard();
        actionCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));

        actionCard.add(new JLabel("Next Verification Date (YYYY-MM-DD):"));
        txtNextDate = new JTextField(LocalDate.now().plusMonths(3).toString(), 10);
        actionCard.add(txtNextDate);

        actionCard.add(new JLabel("Inspection Findings / Notes:"));
        txtNotes = new JTextField("Physical condition and functionality certified.", 25);
        actionCard.add(txtNotes);

        JButton btnVerify = ModernUIUtils.createSuccessButton("Verify Selected Resource");
        btnVerify.addActionListener(e -> executeVerification());
        actionCard.add(btnVerify);

        JButton btnRefresh = ModernUIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadData());
        actionCard.add(btnRefresh);

        // Table
        String[] cols = {"Resource ID", "Resource Name", "Category", "Status", "Verification Status", "Last Verified", "Next Due Date", "Location"};
        pendingModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPendingVerifications = new JTable(pendingModel);
        ModernUIUtils.styleTable(tblPendingVerifications);
        tblPendingVerifications.getColumnModel().getColumn(4).setCellRenderer(ModernUIUtils.getStatusBadgeRenderer());

        JPanel tableCard = ModernUIUtils.createCard();
        tableCard.setLayout(new BorderLayout(5, 5));
        JLabel lblTableHeading = new JLabel("Resources Pending Verification / Expired Audit");
        lblTableHeading.setFont(ModernUIUtils.FONT_HEADER);
        lblTableHeading.setForeground(ModernUIUtils.COLOR_PRIMARY);
        tableCard.add(lblTableHeading, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(tblPendingVerifications), BorderLayout.CENTER);

        JPanel topContainer = new JPanel(new BorderLayout(10, 10));
        topContainer.setOpaque(false);
        topContainer.add(bannerCard, BorderLayout.NORTH);
        topContainer.add(actionCard, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    public void loadData() {
        try {
            List<AbstractResource> all = resourceService.getAllResources();
            pendingModel.setRowCount(0);

            for (AbstractResource r : all) {
                boolean isExpired = DateUtil.isExpired(r.getNextVerificationDate());
                boolean isPending = "VERIFICATION_REQUIRED".equalsIgnoreCase(r.getVerificationStatus()) || "EXPIRED".equalsIgnoreCase(r.getVerificationStatus()) || isExpired;

                if (isPending) {
                    pendingModel.addRow(new Object[]{
                            r.getResourceId(),
                            r.getResourceName(),
                            r.getCategory(),
                            r.getCurrentStatus(),
                            isExpired ? "EXPIRED" : r.getVerificationStatus(),
                            r.getLastVerifiedDate() != null ? r.getLastVerifiedDate() : "Never",
                            r.getNextVerificationDate() != null ? r.getNextVerificationDate() : "Immediate",
                            r.getFullLocation()
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching verification queue: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeVerification() {
        int row = tblPendingVerifications.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a resource from the list to verify.", "Select Resource", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!authService.isAdmin()) {
            JOptionPane.showMessageDialog(this, "Only Administrators / Store Managers can certify resource verification.", "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String resId = (String) pendingModel.getValueAt(row, 0);
        String name = (String) pendingModel.getValueAt(row, 1);
        String nextDate = txtNextDate.getText().trim();
        String notes = txtNotes.getText().trim();

        try {
            String verifier = authService.getCurrentUser().getFullName();
            resourceService.verifyResource(resId, verifier, notes, nextDate);
            JOptionPane.showMessageDialog(this, "Resource '" + name + "' [" + resId + "] successfully verified!\nIt is now unlocked and available for event reservations.", "Verification Confirmed", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error during verification: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
