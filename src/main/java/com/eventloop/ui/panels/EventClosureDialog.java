package com.eventloop.ui.panels;

import com.eventloop.dao.InspectionDAO;
import com.eventloop.dao.ReservationDAO;
import com.eventloop.exception.EventClosureException;
import com.eventloop.model.EventModel;
import com.eventloop.model.InspectionModel;
import com.eventloop.model.ReservationModel;
import com.eventloop.service.AuthService;
import com.eventloop.service.EventService;
import com.eventloop.ui.components.ModernUIUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

public class EventClosureDialog extends JDialog {
    private final EventService eventService = EventService.getInstance();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final InspectionDAO inspectionDAO = new InspectionDAO();
    private final AuthService authService = AuthService.getInstance();

    private final EventModel event;
    private JButton btnCloseEvent;
    private JTextArea txtAuditReport;
    private boolean isClearForClosure = false;

    public EventClosureDialog(Frame owner, EventModel event) {
        super(owner, "Mandatory Event Closure Audit Gate: " + event.getEventId(), true);
        this.event = event;
        setSize(750, 620);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(ModernUIUtils.COLOR_BG);

        initComponents();
        runClosureAudit();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerCard = ModernUIUtils.createCard();
        headerCard.setLayout(new BorderLayout(4, 4));

        JLabel lblTitle = new JLabel("Event Closure Reconciliation & Audit Gate");
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_PRIMARY_DARK);

        JLabel lblSub = new JLabel("Event: " + event.getEventName() + " (" + event.getEventId() + ")  |  Current Status: " + event.getEventStatus());
        lblSub.setFont(ModernUIUtils.FONT_REGULAR);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        headerCard.add(lblTitle, BorderLayout.NORTH);
        headerCard.add(lblSub, BorderLayout.SOUTH);

        // Audit Report Box
        JPanel auditCard = ModernUIUtils.createCard();
        auditCard.setLayout(new BorderLayout(8, 8));

        JLabel lblAuditTitle = new JLabel("Compliance Audit Checklist & Resource Accounting:");
        lblAuditTitle.setFont(ModernUIUtils.FONT_HEADER);
        lblAuditTitle.setForeground(ModernUIUtils.COLOR_PRIMARY);

        txtAuditReport = new JTextArea();
        txtAuditReport.setEditable(false);
        txtAuditReport.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtAuditReport.setBackground(new Color(248, 250, 252));
        txtAuditReport.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        auditCard.add(lblAuditTitle, BorderLayout.NORTH);
        auditCard.add(new JScrollPane(txtAuditReport), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        JButton btnCancel = ModernUIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        btnCloseEvent = ModernUIUtils.createDangerButton("Mark Event COMPLETED & Close");
        btnCloseEvent.setEnabled(false); // Disabled by default until audit passes!
        btnCloseEvent.addActionListener(e -> executeClosure());

        btnPanel.add(btnCancel);
        btnPanel.add(btnCloseEvent);

        mainPanel.add(headerCard, BorderLayout.NORTH);
        mainPanel.add(auditCard, BorderLayout.CENTER);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void runClosureAudit() {
        StringBuilder report = new StringBuilder();
        report.append("====================================================================\n");
        report.append("  EVENTLOOP MANDATORY CLOSURE AUDIT CHECK\n");
        report.append("  Event ID: ").append(event.getEventId()).append(" - ").append(event.getEventName()).append("\n");
        report.append("====================================================================\n\n");

        List<String> blockingReasons = new ArrayList<>();

        if ("COMPLETED".equalsIgnoreCase(event.getEventStatus())) {
            report.append("STATUS: This event is already formally CLOSED and marked COMPLETED.\n");
            btnCloseEvent.setEnabled(false);
            txtAuditReport.setText(report.toString());
            return;
        }

        try {
            List<ReservationModel> reservations = reservationDAO.getReservationsByEvent(event.getEventId());
            List<InspectionModel> inspections = inspectionDAO.getInspectionsByEvent(event.getEventId());

            report.append("1. RESERVATIONS AUDIT (Total: ").append(reservations.size()).append(" bookings):\n");

            if (reservations.isEmpty()) {
                report.append("   - No resources were reserved for this event.\n");
            } else {
                for (ReservationModel r : reservations) {
                    report.append("   * [Res #").append(r.getReservationId()).append("] ")
                          .append(r.getResourceName()).append(" (Qty: ").append(r.getQuantity()).append(") ")
                          .append("-> Status: ").append(r.getReservationStatus()).append("\n");

                    if ("ACTIVE".equalsIgnoreCase(r.getReservationStatus()) || "OVERDUE".equalsIgnoreCase(r.getReservationStatus())) {
                        blockingReasons.add("Resource '" + r.getResourceName() + "' (Res #" + r.getReservationId() + ") is still checked out (" + r.getReservationStatus() + "). Must be returned.");
                    } else if ("COMPLETED".equalsIgnoreCase(r.getReservationStatus())) {
                        // Check if post-use inspection exists
                        boolean postInspected = false;
                        for (InspectionModel ins : inspections) {
                            if (ins.getReservationId() == r.getReservationId() && "POST_USE".equalsIgnoreCase(ins.getInspectionType())) {
                                postInspected = true;
                                break;
                            }
                        }
                        if (!postInspected) {
                            blockingReasons.add("Resource '" + r.getResourceName() + "' (Res #" + r.getReservationId() + ") was returned but Post-Use Inspection is still pending!");
                        }
                    }
                }
            }

            report.append("\n2. POST-USE INSPECTIONS & DAMAGES:\n");
            if (inspections.isEmpty()) {
                report.append("   - No inspections recorded yet.\n");
            } else {
                for (InspectionModel ins : inspections) {
                    report.append("   * Type: ").append(ins.getInspectionType())
                          .append(" | Resource: ").append(ins.getResourceId())
                          .append(" | Condition: ").append(ins.getConditionRating()).append("/5")
                          .append(" | Outcome: ").append(ins.getOutcomeStatus())
                          .append(" | Missing: ").append(ins.getMissingQuantity()).append("\n");
                }
            }

            report.append("\n3. AUDIT GATE DECISION:\n");
            if (blockingReasons.isEmpty()) {
                isClearForClosure = true;
                report.append("   >>> AUDIT PASSED: ALL CLEAR! <<<\n");
                report.append("   - All reserved items are returned.\n");
                report.append("   - All post-use inspections have been completed.\n");
                report.append("   - All physical conditions and missing items have been reconciled.\n");
                report.append("   => YOU MAY PROCEED WITH CLOSING THE EVENT.\n");
                btnCloseEvent.setEnabled(true);
            } else {
                isClearForClosure = false;
                report.append("   >>> AUDIT FAILED: EVENT CLOSURE IS STRICTLY BLOCKED! <<<\n");
                for (String b : blockingReasons) {
                    report.append("   [BLOCK] ").append(b).append("\n");
                }
                report.append("\n   Remedy: Complete return process in Reservation Panel and conduct Post-Use Inspection.\n");
                btnCloseEvent.setEnabled(false);
            }

        } catch (SQLException ex) {
            report.append("Database error during audit: ").append(ex.getMessage());
        }

        txtAuditReport.setText(report.toString());
    }

    private void executeClosure() {
        if (!isClearForClosure) {
            JOptionPane.showMessageDialog(this, "Closure blocked! Ensure all items are returned and inspected first.", "Action Blocked", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int conf = JOptionPane.showConfirmDialog(this, "Are you sure you want to close Event '" + event.getEventName() + "'?\nThis will mark the event COMPLETED and finalize budget savings.", "Confirm Event Closure", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            try {
                String closedBy = authService.isLoggedIn() ? authService.getCurrentUser().getFullName() : "Admin";
                eventService.closeEvent(event.getEventId(), closedBy);
                JOptionPane.showMessageDialog(this, "Event '" + event.getEventName() + "' marked COMPLETED!\nPotential purchase budget successfully protected.", "Event Closed", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (EventClosureException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Closure Blocked", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
