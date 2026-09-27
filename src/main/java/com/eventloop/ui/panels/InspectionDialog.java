package com.eventloop.ui.panels;

import com.eventloop.model.InspectionModel;
import com.eventloop.model.ReservationModel;
import com.eventloop.service.AuthService;
import com.eventloop.service.InspectionService;
import com.eventloop.ui.components.ModernUIUtils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class InspectionDialog extends JDialog {
    private final InspectionService inspectionService = InspectionService.getInstance();
    private final AuthService authService = AuthService.getInstance();
    private final ReservationModel reservation;
    private final String type; // PRE_USE or POST_USE
    private boolean completed = false;
    private int returnedQty = 0;

    private JSpinner spnCondition;
    private JSpinner spnQtyChecked;
    private JSpinner spnQtyReturned;
    private JCheckBox chkDamage;
    private JCheckBox chkCleaning;
    private JCheckBox chkRepair;
    private JComboBox<String> cmbOutcome;
    private JTextArea txtNotes;

    public InspectionDialog(Frame owner, ReservationModel reservation, String type) {
        super(owner, (type.equals("PRE_USE") ? "Pre-Use Inspection Checklist (Handover)" : "Post-Use Inspection & Damage Clearance"), true);
        this.reservation = reservation;
        this.type = type;
        this.returnedQty = reservation.getQuantity();

        setSize(580, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ModernUIUtils.COLOR_BG);

        initComponents();
    }

    private void initComponents() {
        JPanel form = ModernUIUtils.createCard();
        form.setLayout(new GridLayout(type.equals("POST_USE") ? 8 : 5, 2, 8, 8));

        spnCondition = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        spnQtyChecked = new JSpinner(new SpinnerNumberModel(reservation.getQuantity(), 1, reservation.getQuantity(), 1));
        spnQtyReturned = new JSpinner(new SpinnerNumberModel(reservation.getQuantity(), 0, reservation.getQuantity(), 1));

        chkDamage = new JCheckBox("Visible damage or structural crack");
        chkCleaning = new JCheckBox("Deep cleaning or sanitization required");
        chkRepair = new JCheckBox("Technician repair required");

        cmbOutcome = new JComboBox<>(new String[]{
                "AVAILABLE", "UNDER_REPAIR", "MISSING", "RETIRED", "REPURPOSE_REQUIRED"
        });

        txtNotes = new JTextArea(3, 20);

        addField(form, "Event ID / Resource:", new JLabel(reservation.getEventId() + " / " + reservation.getResourceName()));
        addField(form, "Inspection Stage:", new JLabel(type.equals("PRE_USE") ? "PRE-USE DISPATCH AUDIT" : "POST-USE RETURN AUDIT"));
        addField(form, "Verified Condition (1-5):*", spnCondition);

        if (type.equals("PRE_USE")) {
            addField(form, "Quantity Dispatched:*", spnQtyChecked);
            addField(form, "Checklist Status:", new JLabel("Power cables, accessories & optics OK"));
        } else {
            addField(form, "Total Issued Quantity:", new JLabel(String.valueOf(reservation.getQuantity())));
            addField(form, "Quantity Returned Now:*", spnQtyReturned);
            addField(form, "Incident Flags:", createFlagsPanel());
            addField(form, "Post-Use Destination Status:*", cmbOutcome);
        }

        JPanel notesPanel = new JPanel(new BorderLayout(4, 4));
        notesPanel.setOpaque(false);
        notesPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 10, 16));
        notesPanel.add(new JLabel("Inspection Findings / Checklist Details:"), BorderLayout.NORTH);
        notesPanel.add(new JScrollPane(txtNotes), BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(form, BorderLayout.NORTH);
        centerPanel.add(notesPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        JButton btnCancel = ModernUIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = ModernUIUtils.createPrimaryButton(type.equals("PRE_USE") ? "Certify Pre-Use & Dispatch" : "Complete Return Inspection");
        btnSubmit.addActionListener(e -> submitInspection());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);

        add(centerPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JPanel createFlagsPanel() {
        JPanel p = new JPanel(new GridLayout(3, 1, 2, 2));
        p.setOpaque(false);
        p.add(chkDamage);
        p.add(chkCleaning);
        p.add(chkRepair);
        return p;
    }

    private void addField(JPanel pnl, String label, java.awt.Component comp) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(ModernUIUtils.FONT_BOLD);
        pnl.add(lbl);
        pnl.add(comp);
    }

    private void submitInspection() {
        try {
            InspectionModel ins = new InspectionModel();
            ins.setReservationId(reservation.getReservationId());
            ins.setResourceId(reservation.getResourceId());
            ins.setEventId(reservation.getEventId());
            ins.setInspectionType(type);
            ins.setInspectorName(authService.isLoggedIn() ? authService.getCurrentUser().getFullName() : "Store Auditor");
            ins.setConditionRating((Integer) spnCondition.getValue());
            ins.setNotes(txtNotes.getText().trim());

            if (type.equals("PRE_USE")) {
                ins.setQuantityChecked((Integer) spnQtyChecked.getValue());
                ins.setQuantityReturned(0);
                ins.setMissingQuantity(0);
                ins.setOutcomeStatus("AVAILABLE");
                inspectionService.recordPreUseInspection(ins);
            } else {
                int returned = (Integer) spnQtyReturned.getValue();
                this.returnedQty = returned;
                int missing = reservation.getQuantity() - returned;

                ins.setQuantityChecked(reservation.getQuantity());
                ins.setQuantityReturned(returned);
                ins.setMissingQuantity(missing);
                ins.setDamageReported(chkDamage.isSelected());
                ins.setCleaningRequired(chkCleaning.isSelected());
                ins.setRepairRequired(chkRepair.isSelected());
                ins.setOutcomeStatus((String) cmbOutcome.getSelectedItem());

                inspectionService.recordPostUseInspection(ins);
            }

            completed = true;
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error logging inspection: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getReturnedQty() {
        return returnedQty;
    }
}
