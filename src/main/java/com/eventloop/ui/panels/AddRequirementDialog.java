package com.eventloop.ui.panels;

import com.eventloop.exception.ValidationException;
import com.eventloop.model.EventModel;
import com.eventloop.model.RequirementModel;
import com.eventloop.service.EventService;
import com.eventloop.ui.components.ModernUIUtils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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

public class AddRequirementDialog extends JDialog {
    private final EventService eventService = EventService.getInstance();
    private final String eventId;
    private boolean saved = false;

    private JComboBox<String> cmbCategory;
    private JTextField txtName;
    private JSpinner spnQuantity;
    private JSpinner spnCondition;
    private JComboBox<String> cmbIndoorOutdoor;
    private JTextField txtRequiredDate;
    private JTextField txtStartTime;
    private JTextField txtEndTime;
    private JTextArea txtSpecs;
    private JTextArea txtNotes;

    public AddRequirementDialog(Frame owner, String eventId) {
        super(owner, "Add Resource Requirement for Event: " + eventId, true);
        this.eventId = eventId;
        setSize(560, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ModernUIUtils.COLOR_BG);

        initComponents();
        loadEventDefaults();
    }

    private void initComponents() {
        JPanel form = ModernUIUtils.createCard();
        form.setLayout(new GridLayout(9, 2, 8, 8));

        cmbCategory = new JComboBox<>(new String[]{
                "Audio/Visual", "Electrical", "Furniture", "Equipment", 
                "Decoration", "Stationery", "Sports", "Registration Material", "Other"
        });
        txtName = new JTextField("HD Projector");
        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spnCondition = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        cmbIndoorOutdoor = new JComboBox<>(new String[]{"Indoor", "Outdoor", "Both"});
        txtRequiredDate = new JTextField();
        txtStartTime = new JTextField("09:00");
        txtEndTime = new JTextField("17:00");

        txtSpecs = new JTextArea(2, 20);
        txtNotes = new JTextArea(2, 20);

        addField(form, "Resource Category:*", cmbCategory);
        addField(form, "Resource Name:*", txtName);
        addField(form, "Required Quantity:*", spnQuantity);
        addField(form, "Minimum Acceptable Condition (1-5):*", spnCondition);
        addField(form, "Usage Environment:*", cmbIndoorOutdoor);
        addField(form, "Required Date (YYYY-MM-DD):*", txtRequiredDate);
        addField(form, "Start Time (HH:MM):*", txtStartTime);
        addField(form, "End Time (HH:MM):*", txtEndTime);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 10, 16));

        JPanel specsPnl = new JPanel(new BorderLayout(4, 4));
        specsPnl.setOpaque(false);
        specsPnl.add(new JLabel("Technical Specs (e.g. HDMI, 4K, 15m, 16A, 6ft):"), BorderLayout.NORTH);
        specsPnl.add(new JScrollPane(txtSpecs), BorderLayout.CENTER);

        JPanel notesPnl = new JPanel(new BorderLayout(4, 4));
        notesPnl.setOpaque(false);
        notesPnl.add(new JLabel("Special Compatibility Notes:"), BorderLayout.NORTH);
        notesPnl.add(new JScrollPane(txtNotes), BorderLayout.CENTER);

        bottomPanel.add(specsPnl);
        bottomPanel.add(notesPnl);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(form, BorderLayout.NORTH);
        centerPanel.add(bottomPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);
        JButton btnCancel = ModernUIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = ModernUIUtils.createPrimaryButton("Save Requirement");
        btnSave.addActionListener(e -> saveRequirement());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        add(centerPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addField(JPanel pnl, String label, java.awt.Component comp) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(ModernUIUtils.FONT_BOLD);
        pnl.add(lbl);
        pnl.add(comp);
    }

    private void loadEventDefaults() {
        try {
            EventModel e = eventService.getById(eventId);
            if (e != null) {
                txtRequiredDate.setText(e.getEventDate());
                txtStartTime.setText(e.getStartTime());
                txtEndTime.setText(e.getEndTime());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void saveRequirement() {
        try {
            RequirementModel req = new RequirementModel(
                    0,
                    eventId,
                    (String) cmbCategory.getSelectedItem(),
                    txtName.getText().trim(),
                    (Integer) spnQuantity.getValue(),
                    (Integer) spnCondition.getValue(),
                    txtSpecs.getText().trim(),
                    txtRequiredDate.getText().trim(),
                    txtStartTime.getText().trim(),
                    txtEndTime.getText().trim(),
                    (String) cmbIndoorOutdoor.getSelectedItem(),
                    txtNotes.getText().trim()
            );

            eventService.addRequirement(req);
            JOptionPane.showMessageDialog(this, "Requirement saved! You can now run smart matching against campus inventory.", "Saved", JOptionPane.INFORMATION_MESSAGE);
            saved = true;
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Warning", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
