package com.eventloop.ui.panels;

import com.eventloop.exception.ReservationConflictException;
import com.eventloop.exception.ResourceNotAvailableException;
import com.eventloop.exception.ValidationException;
import com.eventloop.exception.VerificationRequiredException;
import com.eventloop.model.CompatibilityMatch;
import com.eventloop.model.EventModel;
import com.eventloop.model.RequirementModel;
import com.eventloop.model.ReservationModel;
import com.eventloop.model.resources.AbstractResource;
import com.eventloop.service.AuthService;
import com.eventloop.service.EventService;
import com.eventloop.service.MatchingService;
import com.eventloop.service.ReservationService;
import com.eventloop.ui.components.ModernUIUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
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

public class RequirementMatchingPanel extends JPanel {
    private final EventService eventService = EventService.getInstance();
    private final MatchingService matchingService = MatchingService.getInstance();
    private final ReservationService reservationService = ReservationService.getInstance();
    private final AuthService authService = AuthService.getInstance();

    private JComboBox<EventModel> cmbEvents;
    private JComboBox<RequirementModel> cmbRequirements;
    private JLabel lblReqDetails;

    private JTable tblMatches;
    private DefaultTableModel matchTableModel;
    private List<CompatibilityMatch> currentMatches = new ArrayList<>();

    public RequirementMatchingPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(ModernUIUtils.COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadEvents();
    }

    private void initComponents() {
        // Top Selection Card
        JPanel topCard = ModernUIUtils.createCard();
        topCard.setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.setOpaque(false);
        JLabel lblTitle = new JLabel("Smart Inventory Matching & Pre-Purchase Decision Engine");
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_PRIMARY_DARK);

        JLabel lblSub = new JLabel("Checks category, name similarity, quantity availability, verification status, physical condition, and reservation conflicts.");
        lblSub.setFont(ModernUIUtils.FONT_REGULAR);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        header.add(lblTitle, BorderLayout.NORTH);
        header.add(lblSub, BorderLayout.SOUTH);

        // Selector controls
        JPanel selectors = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        selectors.setOpaque(false);

        selectors.add(new JLabel("Select Event:"));
        cmbEvents = new JComboBox<>();
        cmbEvents.setPreferredSize(new Dimension(280, 32));
        cmbEvents.addActionListener(e -> onEventSelected());
        selectors.add(cmbEvents);

        selectors.add(new JLabel("Select Requirement:"));
        cmbRequirements = new JComboBox<>();
        cmbRequirements.setPreferredSize(new Dimension(280, 32));
        cmbRequirements.addActionListener(e -> runMatching());
        selectors.add(cmbRequirements);

        JButton btnRun = ModernUIUtils.createPrimaryButton("Run Matching Algorithm");
        btnRun.addActionListener(e -> runMatching());
        selectors.add(btnRun);

        // Details banner
        lblReqDetails = new JLabel("Select an event and requirement to see intelligent inventory matches.");
        lblReqDetails.setFont(ModernUIUtils.FONT_BOLD);
        lblReqDetails.setForeground(ModernUIUtils.COLOR_PRIMARY);

        JPanel middlePanel = new JPanel(new BorderLayout(5, 5));
        middlePanel.setOpaque(false);
        middlePanel.add(selectors, BorderLayout.NORTH);
        middlePanel.add(lblReqDetails, BorderLayout.SOUTH);

        topCard.add(header, BorderLayout.NORTH);
        topCard.add(middlePanel, BorderLayout.CENTER);

        // Table
        String[] cols = {"Match Result", "Compatibility Score", "Resource ID", "Resource Name", "Available Qty", "Condition", "Status", "Verification", "Match Reasoning"};
        matchTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblMatches = new JTable(matchTableModel);
        ModernUIUtils.styleTable(tblMatches);
        tblMatches.getColumnModel().getColumn(0).setCellRenderer(ModernUIUtils.getStatusBadgeRenderer());
        tblMatches.getColumnModel().getColumn(6).setCellRenderer(ModernUIUtils.getStatusBadgeRenderer());
        tblMatches.getColumnModel().getColumn(7).setCellRenderer(ModernUIUtils.getStatusBadgeRenderer());

        JPanel tableCard = ModernUIUtils.createCard();
        tableCard.setLayout(new BorderLayout(5, 5));
        JLabel lblMatchesHeading = new JLabel("Matched Campus Resources & Verification Audit Status");
        lblMatchesHeading.setFont(ModernUIUtils.FONT_HEADER);
        lblMatchesHeading.setForeground(ModernUIUtils.COLOR_PRIMARY);
        tableCard.add(lblMatchesHeading, BorderLayout.NORTH);
        tableCard.add(new JScrollPane(tblMatches), BorderLayout.CENTER);

        // Bottom Action Toolbar
        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottomActions.setOpaque(false);

        JButton btnCompare = ModernUIUtils.createButton("⚖ Pre-Purchase Decision & Cost Comparison", ModernUIUtils.COLOR_PURPLE, java.awt.Color.WHITE);
        btnCompare.addActionListener(e -> openCostComparison());

        JButton btnReserve = ModernUIUtils.createSuccessButton("✓ Reserve This Resource");
        btnReserve.addActionListener(e -> reserveSelectedMatch());

        bottomActions.add(btnCompare);
        bottomActions.add(btnReserve);

        add(topCard, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
        add(bottomActions, BorderLayout.SOUTH);
    }

    public void loadEvents() {
        try {
            List<EventModel> events = eventService.getAllEvents();
            cmbEvents.removeAllItems();
            for (EventModel e : events) {
                cmbEvents.addItem(e);
            }
            if (cmbEvents.getItemCount() > 0) {
                cmbEvents.setSelectedIndex(0);
                onEventSelected();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void selectEventById(String eventId) {
        for (int i = 0; i < cmbEvents.getItemCount(); i++) {
            EventModel em = cmbEvents.getItemAt(i);
            if (em.getEventId().equalsIgnoreCase(eventId)) {
                cmbEvents.setSelectedIndex(i);
                onEventSelected();
                break;
            }
        }
    }

    private void onEventSelected() {
        EventModel e = (EventModel) cmbEvents.getSelectedItem();
        if (e == null) return;
        try {
            List<RequirementModel> reqs = eventService.getRequirementsForEvent(e.getEventId());
            cmbRequirements.removeAllItems();
            for (RequirementModel r : reqs) {
                cmbRequirements.addItem(r);
            }
            if (cmbRequirements.getItemCount() > 0) {
                cmbRequirements.setSelectedIndex(0);
                runMatching();
            } else {
                lblReqDetails.setText("No requirements found for " + e.getEventId() + ". Please click '+ Add Requirements' in Event Management.");
                matchTableModel.setRowCount(0);
                currentMatches.clear();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void runMatching() {
        RequirementModel req = (RequirementModel) cmbRequirements.getSelectedItem();
        if (req == null) return;

        lblReqDetails.setText("Requirement: " + req.getQuantity() + "x " + req.getResourceName() + " (" + req.getCategory() + 
                ") | Min Condition: " + req.getRequiredCondition() + "/5 | Required on: " + req.getRequiredDate() + " " + req.getStartTime() + " - " + req.getEndTime());

        try {
            currentMatches = matchingService.findMatches(req);
            matchTableModel.setRowCount(0);

            for (CompatibilityMatch m : currentMatches) {
                AbstractResource r = m.getResource();
                matchTableModel.addRow(new Object[]{
                        m.getMatchType(),
                        m.getCompatibilityScore() + "%",
                        r.getResourceId(),
                        r.getResourceName(),
                        r.getAvailableQuantity() + " " + r.getUnit(),
                        r.getConditionRating() + "/5",
                        r.getCurrentStatus(),
                        r.getVerificationStatus(),
                        m.getExplanation()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Matching error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private CompatibilityMatch getSelectedMatch() {
        int row = tblMatches.getSelectedRow();
        if (row < 0 || row >= currentMatches.size()) {
            JOptionPane.showMessageDialog(this, "Please select a matched resource from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return currentMatches.get(row);
    }

    private void openCostComparison() {
        RequirementModel req = (RequirementModel) cmbRequirements.getSelectedItem();
        if (req == null) {
            JOptionPane.showMessageDialog(this, "Please select an event requirement first.", "Select Requirement", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AbstractResource resource = null;
        int row = tblMatches.getSelectedRow();
        if (row >= 0 && row < currentMatches.size()) {
            resource = currentMatches.get(row).getResource();
        }

        CostComparisonDialog dlg = new CostComparisonDialog(null, req, resource);
        dlg.setVisible(true);
    }

    private void reserveSelectedMatch() {
        CompatibilityMatch match = getSelectedMatch();
        if (match == null) return;

        RequirementModel req = (RequirementModel) cmbRequirements.getSelectedItem();
        AbstractResource r = match.getResource();

        // Check if unverified or expired
        if ("VERIFICATION_REQUIRED".equalsIgnoreCase(match.getMatchType())) {
            JOptionPane.showMessageDialog(this, "Verification Required – This resource cannot be reserved until it is verified by the Store Manager.", "Verification Gate Block", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("RESERVATION_CONFLICT".equalsIgnoreCase(match.getMatchType())) {
            JOptionPane.showMessageDialog(this, "Reservation Conflict – This resource is already reserved during the selected time.\nPlease pick another compatible resource or reschedule the requirement.", "Conflict Detected", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("NOT_AVAILABLE".equalsIgnoreCase(match.getMatchType()) || "REPAIR_REQUIRED".equalsIgnoreCase(match.getMatchType())) {
            int conf = JOptionPane.showConfirmDialog(this, "Resource status is '" + r.getCurrentStatus() + "'. Are you sure you want to request a conditional reservation?", "Confirm Request", JOptionPane.YES_NO_OPTION);
            if (conf != JOptionPane.YES_OPTION) return;
        }

        String startDT = req.getRequiredDate() + " " + (req.getStartTime() != null ? req.getStartTime() : "09:00");
        String endDT = req.getRequiredDate() + " " + (req.getEndTime() != null ? req.getEndTime() : "17:00");

        ReservationModel res = new ReservationModel(
                0,
                req.getEventId(),
                r.getResourceId(),
                r.getResourceName(),
                req.getQuantity(),
                startDT,
                endDT,
                endDT,
                "Requested via Smart Matching Engine"
        );

        try {
            String requester = authService.isLoggedIn() ? authService.getCurrentUser().getFullName() : "Organizer";
            reservationService.requestReservation(res, requester);

            // Update requirement status to RESERVED
            eventService.updateRequirementStatus(req.getRequirementId(), "RESERVED");

            double avoided = req.getQuantity() * r.getPurchaseCost();
            JOptionPane.showMessageDialog(this, "Reservation successfully created for Event " + req.getEventId() + "!\n" +
                    "Resource: " + r.getResourceName() + " (" + req.getQuantity() + " " + r.getUnit() + ")\n" +
                    "Potential Purchase Avoided: ₹" + String.format("%,.2f", avoided), "Reservation Confirmed", JOptionPane.INFORMATION_MESSAGE);

            runMatching();
        } catch (ValidationException | VerificationRequiredException | ResourceNotAvailableException | ReservationConflictException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Reservation Blocked", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
