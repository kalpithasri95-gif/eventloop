package com.eventloop.ui.panels;

import com.eventloop.exception.InvalidEventDateException;
import com.eventloop.exception.ValidationException;
import com.eventloop.model.EventModel;
import com.eventloop.service.AuthService;
import com.eventloop.service.EventService;
import com.eventloop.ui.components.ModernUIUtils;
import com.eventloop.util.DateUtil;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class CreateEventDialog extends JDialog {
    private final EventService eventService = EventService.getInstance();
    private final AuthService authService = AuthService.getInstance();
    private boolean saved = false;

    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtOrganizer;
    private JTextField txtDept;
    private JComboBox<String> cmbType;
    private JTextField txtDate;
    private JTextField txtStartTime;
    private JTextField txtEndTime;
    private JTextField txtLocation;
    private JSpinner spnParticipants;

    public CreateEventDialog(Frame owner) {
        super(owner, "Create New Campus Event", true);
        setSize(540, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ModernUIUtils.COLOR_BG);

        initComponents();
        generateDefaultId();
    }

    private void initComponents() {
        JPanel form = ModernUIUtils.createCard();
        form.setLayout(new GridLayout(10, 2, 8, 8));

        txtId = new JTextField();
        txtName = new JTextField();
        txtOrganizer = new JTextField(authService.isLoggedIn() ? authService.getCurrentUser().getFullName() : "Event Committee");
        txtDept = new JTextField(authService.isLoggedIn() ? authService.getCurrentUser().getDepartment() : "Student Affairs");
        cmbType = new JComboBox<>(new String[]{
                "Technical", "Cultural", "Sports", "Workshop", "Seminar", "Conference", "Club Activity", "Other"
        });
        txtDate = new JTextField(LocalDate.now().plusDays(7).toString());
        txtStartTime = new JTextField("09:00");
        txtEndTime = new JTextField("17:00");
        txtLocation = new JTextField("Main Auditorium");
        spnParticipants = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 10));

        addField(form, "Event ID (e.g. EVT-2026-004):*", txtId);
        addField(form, "Event Name:*", txtName);
        addField(form, "Organizer Name:*", txtOrganizer);
        addField(form, "Department / Club:*", txtDept);
        addField(form, "Event Type:*", cmbType);
        addField(form, "Event Date (YYYY-MM-DD):*", txtDate);
        addField(form, "Start Time (HH:MM):*", txtStartTime);
        addField(form, "End Time (HH:MM):*", txtEndTime);
        addField(form, "Campus Venue / Location:*", txtLocation);
        addField(form, "Expected Participants:", spnParticipants);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);
        JButton btnCancel = ModernUIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = ModernUIUtils.createPrimaryButton("Create Event");
        btnSave.addActionListener(e -> saveEvent());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addField(JPanel pnl, String label, java.awt.Component comp) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(ModernUIUtils.FONT_BOLD);
        pnl.add(lbl);
        pnl.add(comp);
    }

    private void generateDefaultId() {
        int rand = (int) (Math.random() * 900) + 100;
        txtId.setText("EVT-2026-" + rand);
    }

    private void saveEvent() {
        try {
            EventModel e = new EventModel(
                    txtId.getText().trim(),
                    txtName.getText().trim(),
                    txtOrganizer.getText().trim(),
                    authService.isLoggedIn() ? authService.getCurrentUser().getUserId() : 1,
                    txtDept.getText().trim(),
                    (String) cmbType.getSelectedItem(),
                    txtDate.getText().trim(),
                    txtStartTime.getText().trim(),
                    txtEndTime.getText().trim(),
                    txtLocation.getText().trim(),
                    (Integer) spnParticipants.getValue()
            );

            eventService.createEvent(e);
            JOptionPane.showMessageDialog(this, "Event '" + e.getEventName() + "' created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            saved = true;
            dispose();
        } catch (ValidationException | InvalidEventDateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Validation", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
