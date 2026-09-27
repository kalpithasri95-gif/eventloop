package com.eventloop.ui;

import com.eventloop.exception.ValidationException;
import com.eventloop.service.AuthService;
import com.eventloop.ui.components.ModernUIUtils;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class RegisterDialog extends JDialog {
    private final AuthService authService = AuthService.getInstance();

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JComboBox<String> cmbRole;
    private JTextField txtDept;

    public RegisterDialog(JDialog owner) {
        super(owner, "Register EventLoop Account", true);
        setSize(460, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ModernUIUtils.COLOR_BG);

        initComponents();
    }

    private void initComponents() {
        JPanel form = ModernUIUtils.createCard();
        form.setLayout(new GridLayout(6, 2, 8, 8));

        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        txtFullName = new JTextField();
        txtEmail = new JTextField();
        cmbRole = new JComboBox<>(new String[]{"ORGANIZER", "ADMIN"});
        txtDept = new JTextField("Student Technical Council");

        addField(form, "Full Name:*", txtFullName);
        addField(form, "Institutional Email:*", txtEmail);
        addField(form, "Username:*", txtUsername);
        addField(form, "Password:*", txtPassword);
        addField(form, "User Role:*", cmbRole);
        addField(form, "Department / Club / Unit:*", txtDept);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setOpaque(false);

        JButton btnCancel = ModernUIUtils.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSubmit = ModernUIUtils.createPrimaryButton("Create Account");
        btnSubmit.addActionListener(e -> executeRegistration());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);

        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addField(JPanel pnl, String label, java.awt.Component comp) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(ModernUIUtils.FONT_BOLD);
        pnl.add(lbl);
        pnl.add(comp);
    }

    private void executeRegistration() {
        String u = txtUsername.getText().trim();
        String p = new String(txtPassword.getPassword());
        String fn = txtFullName.getText().trim();
        String em = txtEmail.getText().trim();
        String role = (String) cmbRole.getSelectedItem();
        String dept = txtDept.getText().trim();

        try {
            authService.register(u, p, fn, em, role, dept);
            JOptionPane.showMessageDialog(this, "Account registered successfully for " + fn + "!\nYou can now sign in.", "Registration Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
