package com.eventloop.ui.panels;

import com.eventloop.exception.ValidationException;
import com.eventloop.model.resources.AbstractResource;
import com.eventloop.service.AuthService;
import com.eventloop.service.ResourceService;
import com.eventloop.ui.components.ModernUIUtils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class ResourceCatalogPanel extends JPanel {
    private final ResourceService resourceService = ResourceService.getInstance();
    private final AuthService authService = AuthService.getInstance();

    private JTextField txtSearch;
    private JComboBox<String> cmbCategory;
    private JComboBox<String> cmbStatus;
    private JComboBox<String> cmbVerification;

    private JTable tblResources;
    private DefaultTableModel tableModel;

    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDetails;
    private JButton btnVerify;
    private JButton btnRepair;
    private JButton btnHistory;

    public ResourceCatalogPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(ModernUIUtils.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initComponents();
        loadResources();
    }

    private void initComponents() {
        // Filter Panel
        JPanel filterCard = ModernUIUtils.createCard();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

        filterCard.add(new JLabel("Search:"));
        txtSearch = new JTextField(14);
        filterCard.add(txtSearch);

        filterCard.add(new JLabel("Category:"));
        cmbCategory = new JComboBox<>(new String[]{
                "All Categories", "Audio/Visual", "Electrical", "Furniture", 
                "Equipment", "Decoration", "Stationery", "Sports", "Other"
        });
        filterCard.add(cmbCategory);

        filterCard.add(new JLabel("Status:"));
        cmbStatus = new JComboBox<>(new String[]{
                "All Statuses", "AVAILABLE", "RESERVED", "IN_USE", "UNDER_REPAIR", "REPURPOSE_REQUIRED", "MISSING", "RETIRED"
        });
        filterCard.add(cmbStatus);

        filterCard.add(new JLabel("Verification:"));
        cmbVerification = new JComboBox<>(new String[]{"All", "VERIFIED", "VERIFICATION_REQUIRED", "EXPIRED"});
        filterCard.add(cmbVerification);

        JButton btnFilter = ModernUIUtils.createPrimaryButton("Filter");
        btnFilter.addActionListener(e -> loadResources());
        filterCard.add(btnFilter);

        JButton btnReset = ModernUIUtils.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbCategory.setSelectedIndex(0);
            cmbStatus.setSelectedIndex(0);
            cmbVerification.setSelectedIndex(0);
            loadResources();
        });
        filterCard.add(btnReset);

        // Action Toolbar
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        btnDetails = ModernUIUtils.createSecondaryButton("View Details");
        btnDetails.addActionListener(e -> openDetailsDialog());

        btnAdd = ModernUIUtils.createPrimaryButton("+ Add Resource");
        btnAdd.addActionListener(e -> openAddEditDialog(null));

        btnEdit = ModernUIUtils.createSecondaryButton("Edit");
        btnEdit.addActionListener(e -> {
            AbstractResource sel = getSelectedResource();
            if (sel != null) openAddEditDialog(sel);
        });

        btnVerify = ModernUIUtils.createSuccessButton("Verify");
        btnVerify.addActionListener(e -> openVerificationAction());

        btnRepair = ModernUIUtils.createDangerButton("Mark Repair");
        btnRepair.addActionListener(e -> openRepairAction());

        btnHistory = ModernUIUtils.createSecondaryButton("Audit History");
        btnHistory.addActionListener(e -> openHistoryDialog());

        actionPanel.add(btnDetails);
        actionPanel.add(btnHistory);
        actionPanel.add(btnRepair);
        actionPanel.add(btnVerify);
        actionPanel.add(btnEdit);
        actionPanel.add(btnAdd);

        JPanel topContainer = new JPanel(new BorderLayout(5, 8));
        topContainer.setOpaque(false);
        topContainer.add(filterCard, BorderLayout.CENTER);
        topContainer.add(actionPanel, BorderLayout.SOUTH);

        // Table
        String[] cols = {"Resource ID", "Resource Name", "Category", "Available", "Condition", "Status", "Verification", "Location", "Purchase Cost"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblResources = new JTable(tableModel);
        ModernUIUtils.styleTable(tblResources);
        tblResources.getColumnModel().getColumn(5).setCellRenderer(ModernUIUtils.getStatusBadgeRenderer());
        tblResources.getColumnModel().getColumn(6).setCellRenderer(ModernUIUtils.getStatusBadgeRenderer());

        tblResources.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    openDetailsDialog();
                }
            }
        });

        JPanel tableCard = ModernUIUtils.createCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(new JScrollPane(tblResources), BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    public void loadResources() {
        try {
            String kw = txtSearch.getText().trim();
            String cat = (String) cmbCategory.getSelectedItem();
            String stat = (String) cmbStatus.getSelectedItem();
            String ver = (String) cmbVerification.getSelectedItem();

            List<AbstractResource> list = resourceService.searchResources(kw, cat, stat, ver);
            tableModel.setRowCount(0);

            for (AbstractResource r : list) {
                tableModel.addRow(new Object[]{
                        r.getResourceId(),
                        r.getResourceName(),
                        r.getCategory(),
                        r.getAvailableQuantity() + " / " + r.getQuantity() + " " + r.getUnit(),
                        r.getConditionRating() + " / 5",
                        r.getCurrentStatus(),
                        r.getVerificationStatus(),
                        r.getFullLocation(),
                        "₹" + String.format("%,.2f", r.getPurchaseCost())
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading resources: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private AbstractResource getSelectedResource() {
        int row = tblResources.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a resource from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String resId = (String) tableModel.getValueAt(row, 0);
        try {
            return resourceService.getById(resId);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching resource: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void openDetailsDialog() {
        AbstractResource r = getSelectedResource();
        if (r != null) {
            ResourceDetailsDialog dlg = new ResourceDetailsDialog(null, r);
            dlg.setVisible(true);
        }
    }

    private void openAddEditDialog(AbstractResource r) {
        if (!authService.isAdmin() && r != null) {
            JOptionPane.showMessageDialog(this, "Only Administrators / Store Managers can modify inventory resources.", "Access Restricted", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AddEditResourceDialog dlg = new AddEditResourceDialog(null, r);
        dlg.setVisible(true);
        if (dlg.isSaved()) {
            loadResources();
        }
    }

    private void openVerificationAction() {
        AbstractResource r = getSelectedResource();
        if (r == null) return;
        if (!authService.isAdmin()) {
            JOptionPane.showMessageDialog(this, "Only Store Managers / Admin can formally verify resources.", "Access Restricted", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String notes = JOptionPane.showInputDialog(this, "Enter verification inspection notes for " + r.getResourceName() + ":", "Verify Resource", JOptionPane.PLAIN_MESSAGE);
        if (notes != null) {
            try {
                resourceService.verifyResource(r.getResourceId(), authService.getCurrentUser().getFullName(), notes, null);
                JOptionPane.showMessageDialog(this, "Resource '" + r.getResourceName() + "' verified successfully!", "Verification Success", JOptionPane.INFORMATION_MESSAGE);
                loadResources();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openRepairAction() {
        AbstractResource r = getSelectedResource();
        if (r == null) return;
        String reason = JOptionPane.showInputDialog(this, "Enter defect or fault description for repair:", "Send to Repair", JOptionPane.WARNING_MESSAGE);
        if (reason != null && !reason.trim().isEmpty()) {
            try {
                resourceService.markUnderRepair(r.getResourceId(), authService.getCurrentUser().getFullName(), reason, r.getEstimatedRepairCost());
                JOptionPane.showMessageDialog(this, "Resource '" + r.getResourceName() + "' marked UNDER_REPAIR.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                loadResources();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openHistoryDialog() {
        AbstractResource r = getSelectedResource();
        if (r != null) {
            ResourceHistoryDialog dlg = new ResourceHistoryDialog(null, r.getResourceId(), r.getResourceName());
            dlg.setVisible(true);
        }
    }
}
