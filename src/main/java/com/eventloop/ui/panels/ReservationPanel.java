package com.eventloop.ui.panels;

import com.eventloop.exception.ResourceNotAvailableException;
import com.eventloop.model.ReservationModel;
import com.eventloop.service.AuthService;
import com.eventloop.service.ReservationService;
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
import javax.swing.table.DefaultTableModel;

public class ReservationPanel extends JPanel {
    private final ReservationService reservationService = ReservationService.getInstance();
    private final AuthService authService = AuthService.getInstance();

    private JComboBox<String> cmbStatusFilter;
    private JTable tblReservations;
    private DefaultTableModel model;

    public ReservationPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernUIUtils.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadReservations();
    }

    private void initComponents() {
        // Top Toolbar
        JPanel topCard = ModernUIUtils.createCard();
        topCard.setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.setOpaque(false);
        JLabel lblTitle = new JLabel("Reservation, Checkout & Return Operations");
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_PRIMARY_DARK);

        JLabel lblSub = new JLabel("Manage time-based bookings, approve requests, perform pre-use checkouts, and process post-use returns.");
        lblSub.setFont(ModernUIUtils.FONT_REGULAR);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        header.add(lblTitle, BorderLayout.NORTH);
        header.add(lblSub, BorderLayout.SOUTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        controls.setOpaque(false);

        controls.add(new JLabel("Status Filter:"));
        cmbStatusFilter = new JComboBox<>(new String[]{"All", "REQUESTED", "APPROVED", "ACTIVE", "COMPLETED", "OVERDUE", "REJECTED"});
        cmbStatusFilter.addActionListener(e -> loadReservations());
        controls.add(cmbStatusFilter);

        JButton btnApprove = ModernUIUtils.createSuccessButton("Approve");
        btnApprove.addActionListener(e -> approveSelected());

        JButton btnReject = ModernUIUtils.createDangerButton("Reject");
        btnReject.addActionListener(e -> rejectSelected());

        JButton btnCheckout = ModernUIUtils.createPrimaryButton("Checkout (Pre-Use)");
        btnCheckout.addActionListener(e -> checkoutSelected());

        JButton btnReturn = ModernUIUtils.createButton("Return & Inspect", ModernUIUtils.COLOR_PURPLE, java.awt.Color.WHITE);
        btnReturn.addActionListener(e -> returnSelected());

        JButton btnRefresh = ModernUIUtils.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadReservations());

        controls.add(btnRefresh);
        controls.add(btnApprove);
        controls.add(btnReject);
        controls.add(btnCheckout);
        controls.add(btnReturn);

        topCard.add(header, BorderLayout.CENTER);
        topCard.add(controls, BorderLayout.SOUTH);

        // Table
        String[] cols = {"Res ID", "Event ID", "Resource ID", "Resource Name", "Qty", "Start Time", "End Time", "Return Deadline", "Status", "Approved By", "Remarks"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblReservations = new JTable(model);
        ModernUIUtils.styleTable(tblReservations);
        tblReservations.getColumnModel().getColumn(8).setCellRenderer(ModernUIUtils.getStatusBadgeRenderer());

        JPanel tableCard = ModernUIUtils.createCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(new JScrollPane(tblReservations), BorderLayout.CENTER);

        add(topCard, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    public void loadReservations() {
        try {
            List<ReservationModel> list = reservationService.getAllReservations();
            String filter = (String) cmbStatusFilter.getSelectedItem();
            model.setRowCount(0);

            for (ReservationModel r : list) {
                if (filter != null && !filter.equalsIgnoreCase("All") && !r.getReservationStatus().equalsIgnoreCase(filter)) {
                    continue;
                }
                model.addRow(new Object[]{
                        r.getReservationId(),
                        r.getEventId(),
                        r.getResourceId(),
                        r.getResourceName(),
                        r.getQuantity(),
                        r.getStartDateTime(),
                        r.getEndDateTime(),
                        r.getReturnDeadline(),
                        r.getReservationStatus(),
                        r.getApprovedBy() != null ? r.getApprovedBy() : "-",
                        r.getRemarks() != null ? r.getRemarks() : "-"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching reservations: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ReservationModel getSelectedReservation() {
        int row = tblReservations.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a reservation row from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int resId = (Integer) model.getValueAt(row, 0);
        try {
            List<ReservationModel> all = reservationService.getAllReservations();
            for (ReservationModel r : all) {
                if (r.getReservationId() == resId) return r;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void approveSelected() {
        ReservationModel res = getSelectedReservation();
        if (res == null) return;
        if (!authService.isAdmin()) {
            JOptionPane.showMessageDialog(this, "Only Administrators can approve reservations.", "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            reservationService.approveReservation(res.getReservationId(), authService.getCurrentUser().getFullName());
            JOptionPane.showMessageDialog(this, "Reservation #" + res.getReservationId() + " approved!", "Approved", JOptionPane.INFORMATION_MESSAGE);
            loadReservations();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectSelected() {
        ReservationModel res = getSelectedReservation();
        if (res == null) return;
        if (!authService.isAdmin()) {
            JOptionPane.showMessageDialog(this, "Only Administrators can reject reservations.", "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String reason = JOptionPane.showInputDialog(this, "Enter reason for rejection:", "Reject Reservation", JOptionPane.PLAIN_MESSAGE);
        if (reason != null && !reason.trim().isEmpty()) {
            try {
                reservationService.rejectReservation(res.getReservationId(), authService.getCurrentUser().getFullName(), reason);
                JOptionPane.showMessageDialog(this, "Reservation #" + res.getReservationId() + " rejected.", "Rejected", JOptionPane.INFORMATION_MESSAGE);
                loadReservations();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void checkoutSelected() {
        ReservationModel res = getSelectedReservation();
        if (res == null) return;

        if (!"APPROVED".equalsIgnoreCase(res.getReservationStatus()) && !"REQUESTED".equalsIgnoreCase(res.getReservationStatus())) {
            JOptionPane.showMessageDialog(this, "Cannot checkout: Reservation is currently '" + res.getReservationStatus() + "'. Only APPROVED or REQUESTED reservations can be dispatched.", "Action Blocked", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Open Pre-use Inspection Dialog before handover
        InspectionDialog dlg = new InspectionDialog(null, res, "PRE_USE");
        dlg.setVisible(true);
        if (dlg.isCompleted()) {
            try {
                String handler = authService.isLoggedIn() ? authService.getCurrentUser().getFullName() : "Store Manager";
                reservationService.checkoutResource(res.getReservationId(), handler);
                JOptionPane.showMessageDialog(this, "Resource '" + res.getResourceName() + "' dispatched!\nPre-use inspection recorded and physical quantity deducted from available inventory.", "Checkout Successful", JOptionPane.INFORMATION_MESSAGE);
                loadReservations();
            } catch (ResourceNotAvailableException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Checkout Blocked", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void returnSelected() {
        ReservationModel res = getSelectedReservation();
        if (res == null) return;

        if (!"ACTIVE".equalsIgnoreCase(res.getReservationStatus()) && !"OVERDUE".equalsIgnoreCase(res.getReservationStatus())) {
            JOptionPane.showMessageDialog(this, "Cannot return: Reservation status is '" + res.getReservationStatus() + "'. Only ACTIVE or OVERDUE items can be returned.", "Action Blocked", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Open Post-use Inspection Dialog
        InspectionDialog dlg = new InspectionDialog(null, res, "POST_USE");
        dlg.setVisible(true);
        if (dlg.isCompleted()) {
            try {
                String handler = authService.isLoggedIn() ? authService.getCurrentUser().getFullName() : "Store Manager";
                reservationService.returnResource(res.getReservationId(), dlg.getReturnedQty(), handler);
                JOptionPane.showMessageDialog(this, "Resource returned and post-use inspection certified!\nStatus updated and returned stock reconciled.", "Return Complete", JOptionPane.INFORMATION_MESSAGE);
                loadReservations();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
